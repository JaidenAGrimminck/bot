class ViewDrivebase extends HTMLElement {
    constructor() {
        super();
    }

    async connectedCallback() {
        this.innerHTML = (await RawElement("view-drivebase", false));
    }
}

customElements.define("view-drivebase", ViewDrivebase);