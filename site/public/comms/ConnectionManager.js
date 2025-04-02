
let robotInfo = {
    ip: "",
    port: "",

    status: {
        connected: false,
        up: 0,
        down: 0
    }
}

window.robotInfo = robotInfo;

let backendInfo = {
    status: {
        connected: false,
        up: 0,
        down: 0
    }
}

window.backendInfo = backendInfo;

let ros = {};

const connectionUpdateListeners = [];

function setRobotInfo(ip, port) {
    robotInfo.ip = ip;
    robotInfo.port = port;
}

function connectToRobot() {
    socket.emit("robot-connection", {
        host: robotInfo.ip,
        port: robotInfo.port,
        https: false
    })
}

function sendPayload(payload=[]) {
    socket.emit('robot-payload', {
        payload
    })
}

function addConnectionUpdateListener(listener) {
    connectionUpdateListeners.push(listener);
}

function doubleCheckIfROS(path) {
    let fil = path;
    if (path.startsWith("ros//")) {
        fil = path.replace("ros//", "");
    }

    fil = fil.split(".")

    let found = false;

    let monitorVar = ros;

    for (let p of fil) {
        if (monitorVar[p] == undefined) {
            found = false;
            break;
        }

        monitorVar = monitorVar[p];
        found = true;
        return;
    }

    //todo: when on raspi, then go to backend and subscribe, then feed to ros object.    
}

async function checkConnection() {
    let robot = { connected: false, up: 0, down: 0 }; //await checkRobotConnection();
    let backend = await checkBackendConnection();

    if (robot.connected) {
        //todo
    }

    if (backend.connected) {
        backendInfo.status = backend;
    }

    connectionUpdateListeners.forEach((listener) => {
        listener({
            robot,
            backend
        });
    });
}

async function checkRobotConnection() {
    //todo: implement.
    return { connected: false, up: 0, down: 0 };
}

let hr = null;

socket.on("heartbeat", (data) => {
    if (hr == null) return;
    
    hr({ time: data.time });
});

window.robot_status = {};
window.robot_classes = {};

let robot_classes_listeners = [];

socket.on("robot-status", (data) => {
    window.robot_status = data;
})

socket.on("robot-classes", (data) => {
    window.robot_classes = data;
    robot_classes_listeners.forEach((listener) => {
        listener(data);
    });
});

function waitForRobotClasses(callback) {
    robot_classes_listeners.push(callback);
    if (window.robot_classes.length > 0) {
        callback(window.robot_classes);
    }
}

window.telemetry = [];

let telemetryUpdateListeners = [];

socket.on("telemetry-starter", (data) => {
    telemetry = data;
    telemetryUpdateListeners.forEach((listener) => {
        for (let d of data) {
            listener(d);
        }
    });
});

socket.on("telemetry-update", (data) => {
    //add to telemetry.
    telemetry.push(data);
    telemetryUpdateListeners.forEach((listener) => {
        listener(data);
    });
});

function addTelemetryUpdateListener(listener) {
    telemetryUpdateListeners.push(listener);
    if (telemetry.length > 0) {
        listener(telemetry);
    }
}

let lastLidarUpdate = [];

socket.on('lidar-update', (data) => {
    lastLidarUpdate = data;
})

async function checkBackendConnection() {
    let t1 = Date.now();

    let j = await new Promise((resolve) => {
        hr = resolve;

        socket.emit("heartbeat");
    });

    hr = null;

    let t2 = Date.now();

    let up = j.time - t1;
    let down = t2 - j.time;

    return { connected: true, up, down }
}

const ROBOT_STATES = {
    START: "start",
    STOP: "stop",
    PAUSE: "pause",
    RESUME: "resume"
}

function setRobotState(robotClass, state) {
    if (Object.values(ROBOT_STATES).indexOf(state) == -1) {
        throw new Error("Invalid state");
    }

    socket.emit("robot-state-update", {
        robotClass,
        state
    });
}

let heartbeatStarted = false;

async function beginHeartbeatInterval() {
    heartbeatStarted = true;

    setInterval(checkConnection, 1000);
}

window.addEventListener("load", () => {
    if (heartbeatStarted) return;
    beginHeartbeatInterval();
})

setTimeout(() => {
    if (!heartbeatStarted) {
        console.warn("Heartbeat not started? Performing resurrection via fallback.");
        beginHeartbeatInterval();
    }
}, 100)


// topica manager

class TopicaMiddleman {
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
    
    constructor() {
        this.paths = {};

        this.getCallbacks = {};
        this.subCallbacks = {};

        socket.on("topica-update", (data) => {
            this.update(data);
        });

        socket.on("topica-topics", (data) => {
            for (let topic of data.topics) {
                if (topic.path in this.paths) {
                    continue;
                }

                this.paths[topic.path] = null;
            }
        })
    }

    update(data) {
        this.paths[data.path] = data.value;

        if (data.subUpdate) {
            if (this.subCallbacks[data.path]) {
                this.subCallbacks[data.path](data.value);
            }
        } else {
            if (this.getCallbacks[data.path]) {
                this.getCallbacks[data.path](data.value);
                delete this.getCallbacks[data.path];
            }
        }
    }

    set(path, value, type) {
        socket.emit("topica-set", {
            path,
            value,
            value_byte: type
        });
    }
    
    get(path, callback) {
        this.getCallbacks[path] = callback;

        socket.emit("topica-get", {
            path
        });
    }

    subscribe(path, interval, callback) {
        this.subCallbacks[path] = callback;

        socket.emit("topica-subscribe", {
            path,
            interval
        });
    }

    //TODO: implement
    // unsubscribe(path) {
    //     delete this.subCallbacks[path];

    //     socket.emit("topica-unsubscribe", {
    //         path
    //     });
    // }
    
}