
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
        "status",
        "number"
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
        } else if (type == "number") {
            this.children[0].innerHTML = await RawElement("panel//number", false);
            this.numberInitialize();
        }
    }

    async throwAndShow() {
        this.setAttribute("type", "error");
        this.setAttribute("size", "4x2");

        console.log("Error thrown, showing error panel.")

        await this.connectedCallback();
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

        let monitoring = this.getAttribute("monitor");
        
        if (monitoring == undefined || monitoring == null || monitoring == "") {
            console.warn("No monitoring attribute found on ", this, "!");
            this.throwAndShow();
            return;
        }

        //copy bool
        let isFunction = !(!monitoring.startsWith("#"));
        monitoring = monitoring.replace("#", "");

        let monitor;

        let getValue = () => {
            let i = 0;
            let monitorVar = window;
            
            try {
                for (let p of monitoring.split(".")) {
                    if (i == monitoring.split(".").length - 1) {
                        return monitorVar[monitoring.split(".")[i]];
                    }

                    monitorVar = monitorVar[p];

                    i++;
                }
            } catch(e) {
                console.warn("Could not find variable ", monitoring, " in window!");
                this.throwAndShow();
            }

            console.warn("Could not find variable ", monitoring, " in window!");
            this.throwAndShow();

            return undefined;
        }

        //absolute .. fix but it'll do lmao
        setTimeout(() => {
            let gobj = getGraph(tagname);

            gobj.options = Object.assign(gobj.options, {
                type: "line",
                backgroundColor: "rgb(235, 235, 235)",
                lineColor: "black",
            });

            monitor = setInterval(() => {
                let gobj = getGraph(tagname);
                if (gobj == undefined) {
                    clearInterval(monitor);
                    return;
                }

                if (isFunction) {
                    let newData = getValue();

                    if (newData == undefined) {
                        return;
                    }

                    addToGraphData(tagname, newData);
                } else {
                    let newData = getValue();

                    if (newData == undefined) {
                        return;
                    }

                    if (typeof newData == "object") {
                        if (newData.length == undefined) {
                            console.warn("Object ", newData, " is not an array!");
                            this.throwAndShow();
                            return;
                        }
                        if (newData.length != 2) {
                            console.warn("Object ", newData, " is not a 2-tuple!");
                            this.throwAndShow();
                            return;
                        }

                        for (let d of newData) {
                            addToGraphData(tagname, d);
                        }
                    } else if (typeof newData == "number") {
                        addToGraphData(tagname, [performance.now(), newData]);
                    }
                }
            }, 100)
        }, 10)
    }

    async statusInitialize() {
        addConnectionUpdateListener((info) => {
            this.children[0].querySelector("#robot-status").innerHTML = (info.robot.connected) ? "🟢" : "🔴";
            this.children[0].querySelector("#backend-status").innerHTML = (info.backend.connected) ? "🟢" : "🔴";
            
            this.children[0].querySelector("#robot-up").innerHTML = info.robot.up;
            this.children[0].querySelector("#robot-down").innerHTML = info.robot.down;
            this.children[0].querySelector("#backend-up").innerHTML = info.backend.up;
            this.children[0].querySelector("#backend-down").innerHTML = info.backend.down;
        })
    }

    async numberInitialize() {
        let title = this.getAttribute("title") || "";
        let units = this.getAttribute("units") || "";

        this.children[0].querySelector("#panel-number-title").textContent = title;
        this.children[0].querySelector("#panel-number-units").textContent = units;

        let monitoring = this.getAttribute("monitor");

        if (monitoring == undefined || monitoring == null || monitoring == "") {
            console.warn("No monitoring attribute found on ", this, "!");
            this.throwAndShow();
            return;
        }

        monitoring = monitoring.replace("#", "");

        let monitor;

        let getValue = () => {
            let i = 0;
            let monitorVar = window;
            
            try {
                for (let p of monitoring.split(".")) {
                    if (i == monitoring.split(".").length - 1) {
                        return monitorVar[monitoring.split(".")[i]];
                    }

                    monitorVar = monitorVar[p];

                    i++;
                }
            } catch(e) {
                console.warn("Could not find variable ", monitoring, " in window!");
                this.throwAndShow();
            }

            console.warn("Could not find variable ", monitoring, " in window!");
            this.throwAndShow();

            return undefined;
        }

        setTimeout(() => {
            let obj = this.children[0].querySelector("#panel-number-value");

            monitor = setInterval(() => {
                let newData = getValue();

                if (newData == undefined) {
                    return;
                }

                obj.textContent = newData;
            });
        }, 100)
    }
} 

customElements.define("info-panel", InfoPanel);