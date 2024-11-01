const WebSocket = require('ws');

class RobotConnection {
    constructor(ip="localhost", port=8080, https=false) {
        try {
            this.ws = new WebSocket(`${https ? "wss" : "ws"}://${ip}:${port}`);
        } catch (e) {
            return;
        }
        
        this.open = false;

        this.openCallbacks = [];

        this.sensorData = {};

        this.sensorListeners = {};

        this.register();
    }

    register() {
        this.ws.on('open', this.onOpen.bind(this));
        this.ws.on('message', this.onMessage.bind(this));
        this.ws.on('error', (e) => {
            this.wsLog("" + e);
            this.open = false;
        })
    }

    /**
     * Helper function to log messages with a prefix
     * @param {string} msg
     */
    wsLog(msg) {
        console.log("[WS]", msg);
    }

    /* -- Events -- */

    /**
     * Called when the connection is established to the server
     * Does a register handshake.
     */
    onOpen() {        
        this.ws.send([
            0xFF,
            0x03,
            0x00
        ])

        this.wsLog("Sent initial connection message.");
    }

    /**
     * Whenever data is recieved from the server, this function is called.
     * @param {Buffer} data
     */
    onMessage(data) {
        if (data instanceof Buffer) {
            if (data.length == 2) {
                if (data.at(0) == 0xFF && data.at(1) == 0xFF) {
                    this.onConnection();
                } else if (data.at(0) == 0xFF && data.at(1) == 0x00) {
                    ws.send([0xFF, 0x01]);
                }
            }

            if (data.length == 0) return;

            if (data.at(0) == 0xC0) {
                if (data.length == 1) return;
                let type = data.at(1);

                if (type == 0x01) { //data recieve
                    let robotAddress = data.at(2);
                    let sensorAddress = data.at(3);
                    let n_values = data.at(4);

                    const initial = 5;
                    
                    //update values
                    for (let i = 0; i < n_values; i++) {
                        let value = [];
                        for (let j = 0; j < 8; j++) {
                            value.push(data.at(initial + j + (i * 8)));
                        }

                        value = value.reverse(); //different endianness, same with arduino :/

                        //convert to double
                        let double = new Float64Array(new Uint8Array(value).buffer)[0];

                        if (!this.sensorData[robotAddress]) {
                            this.sensorData[robotAddress] = {};
                            this.wsLog("Connected to robot", robotAddress);
                        }
                        if (!this.sensorData[robotAddress][sensorAddress]) {
                            this.sensorData[robotAddress][sensorAddress] = Array(n_values).fill(0);
                            this.wsLog("Connected to sensor", sensorAddress, "on robot", robotAddress);
                        } else {
                            this.sensorData[robotAddress][sensorAddress][i] = double;
                        }
                    }
                    
                    //call listeners
                    if (this.sensorListeners[robotAddress]) {
                        if (this.sensorListeners[robotAddress][sensorAddress]) {
                            this.sensorListeners[robotAddress][sensorAddress].forEach(event => {
                                event(this.sensorData[robotAddress][sensorAddress]);
                            });
                        }
                    }

                }
            }
        }

    }

    /**
     * Called when handshake is complete and connection is established
     */
    onConnection() {
        this.wsLog("Connected to server");

        this.open = true;
        for (let callback of this.openCallbacks) {
            callback(this);
        }
    }

    /* -- API -- */

    /**
     * Calls a function once the connection is open. Will call immediately if already open.
     * @param {Function} a
     */
    onceOpen(a) {
        if (this.open) {
            a();
        } else {
            this.openCallbacks.push(a);
        }
    }

    /**
     * Subscribes to a sensor on a robot
     * @param {number} robotAddress
     * @param {number} sensorAddress
     * @param {boolean} unsubscribe
     */
    subscribe(robotAddress, sensorAddress, unsubscribe=false) {
        this.ws.send([
            0x02,
            0x11,
            robotAddress,
            sensorAddress,
            unsubscribe ? 0x00 : 0x01,
        ]);
    }

    /**
     * Listens for a sensor event
     * @param {Function} event
     * @param {number} robotAddress
     * @param {number} sensorAddress
     * @param {boolean} unsubscribe
     */
    listen(event=((list)=>{}), robotAddress=0, sensorAddress=0, unsubscribe=false) {
        if (unsubscribe) {
            if (this.sensorListeners[robotAddress]) {
                if (this.sensorListeners[robotAddress][sensorAddress]) {
                    this.sensorListeners[robotAddress][sensorAddress] = this.sensorListeners[robotAddress][sensorAddress].filter((a) => a[0] != robotAddress && a[1] != sensorAddress);
                }
            }
        } else {
            if (!this.sensorListeners[robotAddress]) {
                this.sensorListeners[robotAddress] = {};
            }
            if (!this.sensorListeners[robotAddress][sensorAddress]) {
                this.sensorListeners[robotAddress][sensorAddress] = [];
            }
            this.sensorListeners[robotAddress][sensorAddress].push(event);
        }

        this.subscribe(robotAddress, sensorAddress, unsubscribe);
    }

    send(payload=[]) {
        if (!this.open) return;
        
        this.ws.send(payload);
    }
}

// new RobotConnection().onceOpen((conn) => {
//     //do stuff.
// });

module.exports = {
    RobotConnection
}