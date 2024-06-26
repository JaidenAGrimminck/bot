
class InfoPanel extends HTMLElement {
    constructor() {
        super();
    }

    async connectedCallback() {
        //copy
        const old = this.innerHTML + "";

        this.innerHTML = (await RawElement("panel")).replace("$CONTENT$", old);
    }
}

customElements.define("info-panel", InfoPanel);