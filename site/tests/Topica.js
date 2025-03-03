const { TopicaServer } = require("./../topica.js");

const server = new TopicaServer("localhost", 5443);

const onConnection = () => {
    server.get("/me/nickname", (data) => {
        console.log("Registered under nickname:", data)
    })

    server.subscribe("/robot/selected", 1000, (data) => {
        console.log("Robot Selected:", data)
    })
};

server.on("open", onConnection);
server.on("reconnect", onConnection);