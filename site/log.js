
class Log {
    constructor() {
        this.__log = [];

        this.socket_updates = [];
    }
    log(timestamp, msg) {
        this.__log.push({timestamp, msg});
    }

    getLog() {
        return this.__log;
    }

    addSocketListener(socket, sendHistory=true) {
        this.socket_updates.push(socket);

        if (sendHistory) {
            socket.emit('log-update', {
                log: this.getLog()
            });
        }
    }

    removeSocketListener(socket) {
        this.socket_updates = this.socket_updates.filter(s => s != socket);
    }

    sendUpdateToSockets() {
        for (let s of this.socket_updates) {
            s.emit('log-update', {

            });
        }
    }
}

module.exports = Log;