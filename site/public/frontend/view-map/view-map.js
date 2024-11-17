class ViewMap extends HTMLElement {
    constructor() {
        super();
    }

    async connectedCallback() {
        this.innerHTML = (await RawElement("view-map", false));
    }
}

customElements.define("view-map", ViewMap);