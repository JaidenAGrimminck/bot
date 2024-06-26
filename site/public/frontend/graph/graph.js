
const graphs = {};

function getGraph(tagname) {
    return graphs[tagname];
}

function setGraphData(tagname, data=[]) {
    if (graphs[tagname] === undefined) {
        console.error("Graph with tag " + tagname + " does not exist!");
        return;
    }

    graphs[tagname].data = data;
}

function addToGraphData(tagname, data=[]) {
    if (graphs[tagname] === undefined) {
        console.error("Graph with tag " + tagname + " does not exist!");
        return;
    }

    graphs[tagname].data = graphs[tagname].data.concat(data);
}

function randomTagName() {
    let randomTag;
    do {
        randomTag = createUUID();
    } while (graphs[randomTag] !== undefined);

    return randomTag;
}

function generateGraph(tagname=randomTagName()) {
    const graph = document.createElement("c-graph");
    graph.setAttribute("tag", tagname);

    return {graph, tagname};
}

class Graph extends HTMLElement {
    constructor() {
        super();

        this.canvas = null;
        this.ctx = null;

        //a n x 2 matrix
        this.data = [];

        this.options = {
            type: "line",
            backgroundColor: "black",
            lineColor: "white",
            font: "Inconsolata",
            fontSize: "12px",
            xDecimals: 1,
            yDecimals: 1,
        }
    }

    async connectedCallback() {
        this.innerHTML = (await RawElement("graph", false));

        if (this.getAttribute("tag") === null) {
            console.error("Graph element requires a tag attribute for reference!");
            return;
        }

        if (graphs[this.getAttribute("tag")] !== undefined) {
            console.error("Graph element with tag " + this.getAttribute("tag") + " already exists!");
            return;
        }

        graphs[this.getAttribute("tag")] = this;

        this.canvas = this.querySelector("canvas");
        this.ctx = this.canvas.getContext("2d");

        this.canvas.setAttribute("width", this.getAttribute("width") || "100px");
        this.canvas.setAttribute("height", this.getAttribute("height") || "100px");

        addToDrawQueue(() => {
            this.ctx.fillStyle = this.options.backgroundColor;
            this.ctx.fillRect(0, 0, this.canvas.width, this.canvas.height);

            this.ctx.strokeStyle = this.options.lineColor;

            if (this.data.length < 2) {
                return;
            }

            let max = 0;
            let min = 0;

            for (let i = 0; i < this.data.length; i++) {
                if (this.data[i][1] > max) {
                    max = this.data[i][1];
                }

                if (this.data[i][1] < min) {
                    min = this.data[i][1];
                }
            }

            let minx = this.data[0][0];
            let maxx = this.data[this.data.length - 1][0];

            let calcX = (x) => {
                return (x - minx) / (maxx - minx) * this.canvas.width * 0.9 + this.canvas.width * 0.1;
            }

            let calcY = (y) => {
                return (y - min) / (max - min) * this.canvas.height * 0.8 + this.canvas.height * 0.1;
            }

            this.ctx.beginPath();
            
            this.ctx.moveTo(calcX(this.data[0][0]), calcY(this.data[0][1]));

            for (let i = 1; i < this.data.length; i++) {
                this.ctx.lineTo(calcX(this.data[i][0]), calcY(this.data[i][1]));
            }

            this.ctx.stroke();

            //create numbers for the y axis

            this.ctx.fillStyle = this.options.lineColor;
            this.ctx.fontSize = "12px " + this.options.font;

            let num = 5;
            let step = (max - min) / num;

            for (let i = 0; i <= num; i++) {
                this.ctx.fillText(Math.round((min + i * step) * Math.pow(10, this.options.yDecimals)) / Math.pow(10, this.options.yDecimals), 0, calcY(min + i * step));
            }

            //create numbers for the x axis
            num = 5;
            step = (maxx - minx) / num;

            for (let i = 0; i <= num; i++) {
                this.ctx.fillText(Math.round((minx + i * step) * Math.pow(10, this.options.xDecimals)) / Math.pow(10, this.options.xDecimals), calcX(minx + i * step), this.canvas.height);
            }
        })
    }

    attributeChangedCallback(name, oldValue, newValue) {
        if (name === "width") {
            this.canvas.setAttribute("width", newValue);
        } else if (name === "height") {
            this.canvas.setAttribute("height", newValue);
        }
    }
}

customElements.define("c-graph", Graph);