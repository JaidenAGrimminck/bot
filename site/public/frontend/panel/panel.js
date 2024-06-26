
class InfoPanel extends HTMLElement {
    constructor() {
        super();
    }

    async connectedCallback() {
        let type = this.getAttribute("type");
        let size = this.getAttribute("size");

        if (!!type) {
            console.warn("Type attribute on ", this, " is not yet implemented");
            type = "error";
        }

        if (!!size && !size.includes("x")) {
            size = "1x1";
            console.warn("Invalid size attribute on ", this, ", defaulting to 1x1")
        }

        const width = Number.parseInt(size.split("x")[0]);
        const height = Number.parseInt(size.split("x")[1]);

        this.innerHTML = (await RawElement("panel", false));
    }
}

customElements.define("info-panel", InfoPanel);