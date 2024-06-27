
const standardSize = 100;
//testing
function generateRandomDataset(l) {
    let a = [];
    for (let i = 0; i < l; i++) {
        a.push([i, Math.sin(i / 10 * Math.PI) + Math.random() - 0.5]);
    }

    return a;
}

class InfoPanel extends HTMLElement {
    static correctTypes = [
        "graph",
        "error",
        "status"
    ]

    constructor() {
        super();
        this.width = 1;
        this.height = 1;

        this.data = {};
    }

    async connectedCallback() {
        try {
            await this.postConnectedCallback();
        } catch (e) {
            this.setAttribute("type", "error");
            this.setAttribute("size", "4x2");
            await this.postConnectedCallback();
            console.error(e);
        }
    }

    async postConnectedCallback() {
        let type = this.getAttribute("type");
        let size = this.getAttribute("size");

        if (type == undefined || type == null || !InfoPanel.correctTypes.includes(type)) {
            console.warn("type attribute on ", this, " is not yet implemented");
            type = "error";
            size = "4x2";
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
        } else if (type == "error") {
            this.children[0].innerHTML = await RawElement("panel//error", false);
        } else if (type == "status") {
            this.children[0].innerHTML = await RawElement("panel//status", false);
            this.statusInitialize();
        }
    }

    async graphInitialize() {
        let {graph, tagname} = generateGraph();
        let w = this.width * standardSize * 0.8;
        let h = this.height * standardSize * 0.8;
        graph.setAttribute("width", (Math.round(w) + (Math.round(w) % 2 == 1 ? 1 : 0)) + "px");
        graph.setAttribute("height", (Math.round(h) + (Math.round(h) % 2 == 1 ? 1 : 0)) + "px");
        this.children[0].appendChild(graph);

        this.data = {
            tag: tagname,
        }

        //absolute .. fix but it'll do lmao
        setTimeout(() => {
            let gobj = getGraph(tagname);

            gobj.options = Object.assign(gobj.options, {
                type: "line",
                backgroundColor: "rgb(235, 235, 235)",
                lineColor: "black",
            });
            
            setGraphData(tagname, generateRandomDataset(100));
        }, 10)
    }

    async statusInitialize() {
        addConnectionUpdateListener((info) => {
            this.children[0].querySelector("#robot-status").innerHTML = (info.robot.connected) ? "🟢" : "🔴";
        })
    }
} 

customElements.define("info-panel", InfoPanel);