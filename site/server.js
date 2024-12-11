const express = require('express');
const path = require('path');
const { createServer } = require('node:http');

const { Server } = require("socket.io");

const frontendRouter = require('./routes/frontend/CustomElementManager.js');
const { handleROSRequest } = require("./routes/data/ROS.js");
const Log = require('./log.js');
const { RobotConnection } = require('./robot-connection.js');

const port = process.env.PORT || 8123;

const app = express();
const server = createServer(app);
const io = new Server(server);

//"192.168.6.233" - controller
// 192.168.6.232 - one w/ lidar
const robot = new RobotConnection("192.168.6.232", 8080, false);

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
    });
});

server.listen(port, () => {
    console.log(`Server is running on port ${port}`);
});