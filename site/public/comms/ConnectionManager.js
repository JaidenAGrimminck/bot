
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

const connectionUpdateListeners = [];

function setRobotInfo(ip, port) {
    robotInfo.ip = ip;
    robotInfo.port = port;
}

function addConnectionUpdateListener(listener) {
    connectionUpdateListeners.push(listener);
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

async function checkBackendConnection() {
    let t1 = Date.now();

    let r;

    try {
        r = await fetch("/heartbeat");
    } catch (e) {
        return { connected: false };
    }

    if (!r.ok) {
        return { connected: false };
    }

    let j = await r.json();

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
})