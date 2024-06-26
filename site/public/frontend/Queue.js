
const drawQueue = [];

const frameRate = 60;

function addToDrawQueue(func) {
    drawQueue.push(func);
}

(function() {
    setInterval(() => {
        for (const func of drawQueue) {
            func();
        }
    }, 1000 / frameRate);
})();