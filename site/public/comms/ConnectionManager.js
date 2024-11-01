
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