class ViewTelemetry extends HTMLElement {
    constructor() {
        super();
    }

    async connectedCallback() {
        this.innerHTML = (await RawElement("view-telemetry", false));
    }
}

customElements.define("view-telemetry", ViewTelemetry);