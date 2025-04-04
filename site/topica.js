const WebSocket = require('ws');
const { pack, unpack } = require("./struct.js")

const GET_METHOD = 0b0001;
const SET_METHOD = 0b0010;
const SUBSCRIBE_METHOD = 0b0011;

class TopicaServer {
    static Type = {
        BYTE: 0x01,
        INT16: 0x02,
        SHORT: 0x02,
        INT32: 0x03,
        INT: 0x03,
        INT64: 0x04,
        LONG: 0x04,
        FLOAT: 0x05,
        FLOAT32: 0x05,
        DOUBLE: 0x06,
        FLOAT64: 0x06,
        STRING: 0x07,
        BOOL: 0x08,
        BOOLEAN: 0x08
    }

    constructor(host, port, verbose=true, reconnect=true, reconnectTimeout=5000) {
        this.host = host;
        this.port = port;
        this.url = `ws://${host}:${port}`;

        this.ws = null;
        this.createConnection();

        this.verbose = verbose;
        this.reconnect = reconnect;
        this.reconnectTimeout = reconnectTimeout;

        this.get_callbacks = {};
        this.subscribe_callbacks = {};

        this.state = {};

        this.internalEvents = {
            "error": [],
            "open": [],
            "close": [],
            "reconnect": [],
            "newtopic": []
        };

        this.connectedOnce = false;

        this.open = false;

        this.reconnectionInterval = null;
    }

    createConnection() {
        if (this.ws != null) {
            this.ws.onopen = () => {};
            this.ws.onclose = () => {};
            this.ws.onerror = () => {};
            this.ws.onmessage = () => {};
            this.ws.close();
            this.ws.terminate();
        }

        this.ws = new WebSocket(this.url);
        this.ws.onopen = this.onOpen.bind(this);
        this.ws.onclose = this.onClose.bind(this);
        this.ws.onerror = this.onError.bind(this);
        this.ws.onmessage = this.onMessage.bind(this);

        this.onEvent = this.onEvent.bind(this);
        this.get = this.get.bind(this);
        this.getPromise = this.getPromise.bind(this);
        this.subscribe = this.subscribe.bind(this);
        this.set = this.set.bind(this);
        this.getTopics = this.getTopics.bind(this);
    }

    /**
     * Called when a message is received from the server.
     * @param {MessageEvent} event
     */
    onMessage(event) {
        const message = event.data;

        if (message.length <= 2) {
            return;
        }

        const firstByte = message[0];
        const method = firstByte >> 4;
        const path_len = ((firstByte & 0b00001111) << 8) | (message[1]);

        if (method == 0b1000) {
            const path = message.slice(2, 2 + path_len).toString();
            const dataType = message[2 + path_len];
            const dataLength = unpack(">I", message.slice(3 + path_len, 7 + path_len));
            const data = message.slice(7 + path_len, 7 + path_len + dataLength);

            let value = null;

            switch (dataType) {
                case 0x01:
                    value = data
                    break
                case 0x02:
                    value = unpack('>h', data)
                    break
                case 0x03:
                    value = unpack('>i', data)
                    break
                case 0x04:
                    value = unpack('>q', data)
                    break
                case 0x05:
                    value = unpack('>f', data)
                    break
                case 0x06:
                    value = unpack('>d', data)
                    break
                case 0x07:
                    value = data.toString()
                    break
                case 0x08:
                    value = unpack('>?', data)
                    break
                default:
                    throw new Error("Unsupported data type")
            }

            if (Object.keys(this.get_callbacks).includes(path)) {
                this.get_callbacks[path](value, dataType);

                delete this.get_callbacks[path];
            }

            for (let topic of Object.keys(this.subscribe_callbacks)) {
                if (path.startsWith(topic)) {
                    this.subscribe_callbacks[topic](path, value, dataType);
                }
            }

            this.state[path] = value;
        }
    }
    
    onError(event, error) {
        if (this.verbose) {
            console.error(`Error occured with host Topica server!`, error);
        }

        for (let callback of Object.values(this.internalEvents["error"])) {
            callback(event);
        }
    }

    onClose(event) {
        if (this.verbose) {
            console.error(`Connection to host Topica server closed!`);
        }

        this.open = false;

        for (let callback of Object.values(this.internalEvents["close"])) {
            callback(event);
        }

        if (this.reconnect) {
            if (this.reconnectionInterval != null) {
                clearInterval(this.reconnectionInterval);
            }

            // begin reconnection
            this.reconnectionInterval = setInterval(this.createConnection.bind(this), this.reconnectTimeout)
        }
    }
    
    onOpen(event) {
        if (this.verbose) {
            console.log(`Connected to host Topica server!`);
        }

        this.open = true;

        if (!this.connectedOnce) {
            for (let callback of Object.values(this.internalEvents["open"])) {
                callback(event);
            }
        } else {
            for (let callback of Object.values(this.internalEvents["reconnect"])) {
                callback(event);
            }
        }

        if (this.reconnectionInterval != null) {
            clearInterval(this.reconnectionInterval);
            this.reconnectionInterval = null;
        }

        this.connectedOnce = true;
    }

    encodePath(method, topic) {
        //encode the topic to utf-8 buffer
        const topicBuffer = Buffer.from(topic, 'utf-8');
        let pathLen = topicBuffer.length;

        pathLen = Math.min(pathLen, 0b0000111111111111)

        const pathLenFirstByte = pathLen >> 8;
        const pathLenSecondByte = pathLen & 0b11111111;

        const firstByte = (method << 4) | pathLenFirstByte;

        return [
            firstByte,
            pathLenSecondByte,
            ...topicBuffer
        ]
    }

    /**
     * Get a value from the Topica server.
     * @param {string} topic
     * @param {function} callback
     */
    get(topic, callback) {
        if (!this.open) {
            return;
        }

        this.get_callbacks[topic] = callback;

        this.ws.send(Buffer.from(
            this.encodePath(GET_METHOD, topic)
        ))
    }

    /**
     * Get a value from the Topica server (returns a promise).
     * @param {string} topic
     * @returns {Promise<*>}
     */
    async getPromise(topic) {
        return new Promise((resolve, reject) => {
            this.get(topic, (value) => {
                resolve(value);
            });
        });
    }

    /**
     * Subscribe to a topic on the Topica server.
     * @param {string} topic
     * @param {number} interval_ms
     * @param {function} callback
     */
    subscribe(topic, interval_ms, callback) {
        if (!this.open) {
            return;
        }

        this.subscribe_callbacks[topic] = callback;

        const prepayload = this.encodePath(SUBSCRIBE_METHOD, topic);
        const invervalBytes = pack(">I", interval_ms);

        this.ws.send(Buffer.from(
            [
                ...prepayload,
                ...invervalBytes
            ]
        ))
    }

    /**
     * Set a value to a topic
     * @param {string} topic
     * @param {*} value
     * @param {byte} type_byte Use the enum provided by TopicaServer.Type.
     */
    set(topic, value, type_byte) {
        if (!this.open) {
            return;
        }

        const prepayload = this.encodePath(SET_METHOD, topic);

        let raw_data = [];

        switch (type_byte) {
            case TopicaServer.Type.BYTE:
                raw_data = value;
                break;
            case TopicaServer.Type.INT16:
                raw_data = pack('>h', value)
                break
            case TopicaServer.Type.INT32:
                raw_data = pack('>i', value)
                break
            case TopicaServer.Type.INT64:
                raw_data = pack('>q', value)
                break
            case TopicaServer.Type.FLOAT:
                raw_data = pack('>f', value)
                break
            case TopicaServer.Type.DOUBLE:
                raw_data = pack('>d', value)
                break
            case TopicaServer.Type.STRING:
                raw_data = Buffer.from(value, "utf-8")
                break
            case TopicaServer.Type.BOOL:
                raw_data = pack('>?', value)
                break
            default:
                throw new Error("Unsupported data type")
        }

        const payload_length = pack(">I", raw_data.length)

        // Finally send the payload
        this.ws.send(Buffer.from([
            ...prepayload, type_byte, ...payload_length, ...raw_data
        ]));
    }

    /***
     * Called whenever an internal event occurs.
     * @param {string} event The event code. [open, close, error, reconnect, newtopic]
     * @param {function} callback The callback function
     */
    onEvent(event, callback) {
        if (Object.keys(this.internalEvents).includes(event)) {
            this.internalEvents[event].push(callback);
        }

        if (event == "newtopic" && this.internalEvents["newtopic"].length == 1) {
            this.subscribe("/topica/last_topic_created", 0, async (path, value) => {
                // get new topics
                const topics = await this.getTopics();

                for (let callback of Object.values(this.internalEvents["newtopic"])) {
                    callback({ topics });
                }
            })
        }
    }

    /**
     * Get all paths.
     * @returns {Promise<string[]>}
     */
    async getTopics() {
        const port = await this.getPromise("/topica/rest_port");

        const req = await fetch(`http://${this.host}:${port}/api/v2/topics`, {
            method: "GET",
            headers: {
                "Content-Type": "application/json"
            }
        });

        if (req.status != 200) {
            throw new Error("Failed to get topics");
        }

        const topics = await req.json(); // should be a list of strings

        return topics;
    }
}

if (module !== undefined && module !== null) {
    module.exports = {
        TopicaServer
    }
}