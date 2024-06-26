
const standardSize = 100;

class InfoPanel extends HTMLElement {
    static correctTypes = [
        "graph"
    ]

    constructor() {
        super();
        this.width = 1;
        this.height = 1;
    }

    async connectedCallback() {
        let type = this.getAttribute("type");
        let size = this.getAttribute("size");

        if (type == undefined || type == null || !InfoPanel.correctTypes.includes(type)) {
            console.warn("type attribute on ", this, " is not yet implemented");
            type = "error";
        }

        if (!!size && !size.includes("x")) {
            size = "1x1";
            console.warn("Invalid size attribute on ", this, ", defaulting to 1x1")
        }

        const width = Number.parseFloat(size.split("x")[0]);
        const height = Number.parseFloat(size.split("x")[1]);

        if (isNaN(width) || isNaN(height)) {
            width = isNaN(width) ? 1 : width;
            height = isNaN(height) ? 1 : height;

            console.warn("Invalid size attribute on ", this, ", defaulting to 1 per invalid value.")
        }

        this.innerHTML = (await RawElement("panel", false));

        this.style.width = `${width * standardSize}px`;
        this.style.height = `${height * standardSize}px`;
        
        this.children[0].style.width = `${width * standardSize}px`;
        this.children[0].style.height = `${height * standardSize}px`;

        this.width = width;
        this.height = height;

        if (type == "graph") {
            this.graphInitialize();
        }
    }

    graphInitialize() {
        let {graph, tagname} = generateGraph();
        let w = this.width * standardSize * 0.8;
        let h = this.height * standardSize * 0.8;
        graph.setAttribute("width", (Math.round(w) + (Math.round(w) % 2 == 1 ? 1 : 0)) + "px");
        graph.setAttribute("height", (Math.round(h) + (Math.round(h) % 2 == 1 ? 1 : 0)) + "px");
        this.children[0].appendChild(graph);
    }
} 

customElements.define("info-panel", InfoPanel);