class ViewPlot extends HTMLElement {
    constructor() {
        super();
    }

    async connectedCallback() {
        this.innerHTML = (await RawElement("view-plot", false));
    }
}

customElements.define("view-plot", ViewPlot);