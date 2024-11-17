class ViewCamera extends HTMLElement {
    constructor() {
        super();
    }

    async connectedCallback() {
        this.innerHTML = (await RawElement("view-camera", false));
    }
}

customElements.define("view-camera", ViewCamera);