const WebSocket = require('ws');

const verboseConnection = false;
const reconnect = false;

class RobotConnection {
    constructor(ip="localhost", port=8080, https=false, restartTimeout=5000) {     
           
        this.open = false;

        this.ip = ip;
        this.port = port;
        this.https = https;

        this.restartTimeout = restartTimeout;

        this.openCallbacks = [];

        this.sensorData = {};

        this.sensorListeners = {};

        this.robotClasses = [];

        this.robotData = {
            clock: 0, // clock of the robot
            mode: "disabled", // play, pause, disabled
            robot: -1, //robot index
            editable: false, //editable
        };

        this.eventCallbacks = {
            "onRobotStatus": [],
            "onRobotClasses": [],
            "onTelemetryUpdate": [],
            "onTelemetryStart": [],
            "onRobotDisconnect": [],
        };

        this.wasConnected = true;
        this.connect();

    }

    connect() {
        try {
            this.ws = new WebSocket(`${this.https ? "wss" : "ws"}://${this.ip}:${this.port}`);
        } catch (e) {
            setTimeout(() => {
                this.register();
            }, this.restartTimeout);
            return;
        }

        this.register();
    }

    register() {
        this.ws.on('open', () => {
            this.wasConnected = true;
            this.onOpen.bind(this)();
        });
        this.ws.on('message', this.onMessage.bind(this));
        this.ws.on('error', (e) => {
            if ("" + e == "AggregateError") {
                //set a timeout to try to reconnect
                if (reconnect) {
                    setTimeout(() => {
                        this.connect();
                    }, this.restartTimeout);
                    if (verboseConnection) console.log("Error connecting to server, retrying in", this.restartTimeout, "ms");
                }
            }
            this.open = false;
        })
        this.ws.on('close', (e) => {
            if (this.wasConnected) {
                console.log("[WS] Connection closed", e);
                this.eventCallbacks["onRobotDisconnect"].forEach((a) => a());
            }
            
            this.open = false;

            if (!reconnect) return;

            setTimeout(() => {
                this.connect();
            }, this.restartTimeout);
            if (verboseConnection) console.log("Error connecting to server, retrying in", this.restartTimeout, "ms");
            this.wasConnected = false;
        });
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
    async onMessage(data) {
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
            } else if (data.at(0) == 0x6C) {
                let robotIndex = data.at(1);
                if (robotIndex == 0xFF) robotIndex = -1;

                // next 8 bytes is a long
                let robotClock = data.readBigUInt64BE(2);

                // convert to integer
                robotClock = parseInt(robotClock.toString().replace("n", ""));

                this.robotData.clock = robotClock;

                let tellers = data.at(10);

                const editable = (tellers & 0b10000000) >> 7;
                const mode = (tellers & 0b01000000) >> 6;

                this.robotData.mode = robotIndex == -1 ? "disabled" : (mode == 0 ? "playing" : "paused");
                this.robotData.editable = editable == 0x01;
                this.robotData.robot = robotIndex;

                for (let callback of this.eventCallbacks["onRobotStatus"]) {
                    callback(this.robotData);
                }

            } else if (data.at(0) == 0x4A) {
                let n_robots = data.at(1);
                this.robotClasses = new Array(n_robots);
                console.log("[WS] Detected", n_robots, "playable robot classes.");

                for (let i = 2; i < data.length; i += 2) {
                    let robotID = data.at(i);
                    let disabled = data.at(i + 1) == 0x00;
                    this.robotClasses[robotID] = {
                        disabled,
                        name: "",
                    }
                }

                const resp = await fetch(`http://${this.ip}:${this.port + 1}/api/v1/robots`);
                const json = await resp.json();
                for (let i = 0; i < json.length; i++) {
                    this.robotClasses[i].name = json[i];
                }

                for (let callback of this.eventCallbacks["onRobotClasses"]) {
                    callback(this.robotClasses);
                }
            } else if (data.at(0) == 0x00) {} //todo: implement telemetry updates
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

        // subscribe to robot status
        this.requestRobotClasses();
        this.subscribeToRobotStatus();
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

    setRobotState(robotClass, state) {
        let robotIndex = parseInt(robotClass);
        let mode = 0x00;
        if (state == "disabled" || state == "stop") {
            mode = 0x02;
        } else if (state == "start") {
            mode = 0x01;
        } else if (state == "pause") {
            mode = 0x03;
        } else if (state == "resume") {
            mode = 0x04;
        }

        console.log("Changing robot state to", state, "for robot", robotIndex);

        this.ws.send([
            0x4B,
            robotIndex,
            mode
        ]);
    }

    /**
     * Subscribes to robot status
     * @param {boolean} unsubscribe Whether to unsubscribe or not
     */
    subscribeToRobotStatus(unsubscribe=false) {
        this.ws.send([
            0x02,
            0x4C,
            unsubscribe ? 0x00 : 0x01
        ]);
    }

    /**
     * Requests the robot classes 
     * */
    requestRobotClasses() {
        this.ws.send([
            0x4A,
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

    addEventListener(event, callback) {
        if (!this.eventCallbacks[event]) {
            this.eventCallbacks[event] = [];
        }
        this.eventCallbacks[event].push(callback);
    }

    removeEventListener(event, callback) {
        if (!this.eventCallbacks[event]) return;
        this.eventCallbacks[event] = this.eventCallbacks[event].filter((a) => a != callback);
    }
}

// new RobotConnection().onceOpen((conn) => {
//     //do stuff.
// });

module.exports = {
    RobotConnection
}