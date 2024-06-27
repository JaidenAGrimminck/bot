const express = require('express');
const path = require('path');
const { createServer } = require('node:http');

const { Server } = require("socket.io");

const frontendRouter = require('./routes/frontend/CustomElementManager.js');
const { handleROSRequest } = require("./routes/data/ROS.js");

const port = process.env.PORT || 8080;

const app = express();
const server = createServer(app);
const io = new Server(server);

app.use(express.static(path.join(__dirname, 'public')));
app.use('/frontend', frontendRouter);

app.get('/', (req, res) => {
    res.sendFile(path.join(__dirname, "public/home/index.html"))
});

app.get('/require.js', (req, res) => {
    res.sendFile(path.join(__dirname, "public/Require.js"))
})

io.on('connection', (socket) => {
    socket.on('heartbeat', () => {
        socket.emit('heartbeat', { time: Date.now() });
    })

    //if ev starts with "ros", it's a ROS message, so send it over to ROS.js
    socket.on('ros', (data={data: {}, tag: ""}) => {
        try {
            handleROSRequest(data.tag, data.data);
        } catch (e) {
            console.error(e);
        }
    });

    socket.on('disconnect', () => {});
});

server.listen(port, () => {
    console.log(`Server is running on port ${port}`);
});