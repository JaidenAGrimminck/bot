const express = require('express');
const path = require('path');
const { createServer } = require('node:http');

const { Server } = require("socket.io");

const frontendRouter = require('./routes/frontend/CustomElementManager.js');
const { handleROSRequest } = require("./routes/data/ROS.js");
const Log = require('./log.js');
const { RobotConnection } = require('./robot-connection.js');
const { TopicaServer } = require("./topica.js");

const port = process.env.PORT || 8123;

const app = express();
const server = createServer(app);
const io = new Server(server);

//"192.168.6.233" - controller
// 192.168.6.232 - one w/ lidar
const ROBOT_IP = "localhost"; //"172.16.130.160";
const robot = new RobotConnection(ROBOT_IP, 8080, false);
const topica = new TopicaServer(ROBOT_IP, 5443);

const log = new Log();

app.use(express.static(path.join(__dirname, 'public')));
app.use('/frontend', frontendRouter);

app.get('/', (req, res) => {
    res.sendFile(path.join(__dirname, "public/home/index.html"))
});

app.get('/require.js', (req, res) => {
    res.sendFile(path.join(__dirname, "public/Require.js"))
})

io.on('connection', (socket) => {

    let socketOpen = true;

    socket.on("topica-get", (data={
        path: "",
    }) => {
        if (!topica.open) {
            socket.emit("topica-update", {
                value: null,
                path: data.path,
                subUpdate: false
            })

            return;
        }

        topica.get(data.path, (value) => {
            if (socketOpen) {
                socket.emit("topica-update", {
                    value,
                    path: data.path,
                    subUpdate: false
                });
            }
        });
    });

    socket.on("topica-set", (data={
        path: "",
        value: "",
        value_byte: 0
    }) => {
        if (!topica.open) return;

        topica.set(data.path, data.value, data.value_byte);
    });

    socket.on("topica-subscribe", (data={
        path: "",
        interval: 1000
    }) => {
        if (!topica.open) return;

        topica.subscribe(data.path, data.interval, (value) => {
            if (socketOpen) {
                socket.emit("topica-update", {
                    value,
                    path: data.path,
                    subUpdate: true
                });
            }
        });
    });

    topica.onEvent("newtopic", async (data) => {
        const topics = await topica.getTopics();

        if (socketOpen) {
            socket.emit("topica-topics", {
                "topics": topics
            });
        }
    });

    // send over topics as a preliminary step.
    (async () => {
        if (!topica.open) return;

        const topics = await topica.getTopics();

        socket.emit("topica-topics", {
            "topics": topics
        });
    })();
        

    // --below is deprecated, but need to remove it later--

    let lastUpdate = Date.now();

    const robotStatusListener = (data) => {
        lastUpdate = Date.now();
        socket.emit('robot-status', Object.assign({
            last_update: lastUpdate
        }, data));
    }

    const robotClassesListener = (data) => {
        socket.emit('robot-classes', data);
    }

    const telemetryUpdateListener = (data) => {
        socket.emit('telemetry-update', data);
    }

    const telemetryStarterListener = (data) => {
        socket.emit('telemetry-starter', data);
    }

    const robotDisconnectListener = () => {
        socket.emit('robot_disconnect', {
            last_update: lastUpdate,
            clock: 0,
            mode: "disabled",
            editable: false
        });
    }

    robot.addEventListener('onRobotStatus', robotStatusListener);
    robot.addEventListener('onRobotDisconnect', robotDisconnectListener);
    robot.addEventListener('onRobotClasses', robotClassesListener);
    robot.addEventListener('onTelemetryUpdate', telemetryUpdateListener);
    robot.addEventListener('onTelemetryStart', telemetryStarterListener);

    setTimeout(() => {
        socket.emit('robot-classes', robot.robotClasses)

        //emit telemetry starter event.
        socket.emit('telemetry-starter', robot.telemetry)
    }, 1000)

    socket.on('heartbeat', () => {
        socket.emit('heartbeat', { time: Date.now() });
    });

    socket.on('robot-connection', (data={host: "localhost", port: 8080, https: false}) => {
        robot.connect(data.host, data.port, data.https);
    })

    socket.on('robot-payload', (data={payload: []}) => {
        robot.send(data.payload);
    });

    socket.on("robot-state-update", (data={robotClass: "", state: ""}) => {
        robot.setRobotState(data.robotClass, data.state);
    })

    socket.on('subscribe', (data={robotAddress: 0x00, sensorAddress: 0x00}) => {
        robot.subscribe(data.robotAddress, data.sensorAddress);
    });

    socket.on('subscribe-lidar', () => {
        console.log("[WS] Subscribing to lidar!");
        
        robot.subscribeToLidar();

        robot.listen((data) => {
            socket.emit('lidar-update', data);
        }, 0xA5, 0xA5, false, false)
    })

    //if ev starts with "ros", it's a ROS message, so send it over to ROS.js
    socket.on('ros', (data={data: {}, tag: ""}) => {
        try {
            handleROSRequest(data.tag, data.data);
        } catch (e) {
            console.error(e);
        }
    });
    
    socket.on('log-connect', () => {
        log.addSocketListener(socket);
    })

    socket.on('disconnect', () => {
        log.removeSocketListener(socket);

        robot.removeEventListener('onRobotStatus', robotStatusListener);
        robot.removeEventListener('onRobotClasses', robotClassesListener);

        socketOpen = false;
    });
});

server.listen(port, () => {
    console.log(`Server is running on port ${port}`);
});