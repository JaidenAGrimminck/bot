
const graphs = {};

function updateGraph(tagname) {

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

        console.log(this.getAttribute("width"), this.getAttribute("height"));

        this.canvas.setAttribute("width", this.getAttribute("width") || "100px");
        this.canvas.setAttribute("height", this.getAttribute("height") || "100px");

        addToDrawQueue(() => {
            this.ctx.fillStyle = "black";
            this.ctx.fillRect(0, 0, this.canvas.width, this.canvas.height);
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