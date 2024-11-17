class ViewEnvironment extends HTMLElement {
    constructor() {
        super();
    }

    async connectedCallback() {
        this.innerHTML = (await RawElement("view-environment", false));
    }
}

customElements.define("view-environment", ViewEnvironment);