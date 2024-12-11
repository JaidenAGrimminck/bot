class ViewTelemetry extends HTMLElement {
    constructor() {
        super();
    }

    async connectedCallback() {
        this.innerHTML = (await RawElement("view-telemetry", false));
        addTelemetryUpdateListener((data) => {
            this.updateTelemetry(data);
        });
    }

    updateTelemetry(data) {
        let telemetry = document.querySelector(".view-telemetry-data");

        //detect if the scroll is at the bottom.
        let scrollAtBottom = false;
        if (telemetry.scrollHeight - telemetry.scrollTop < telemetry.clientHeight + 10) {
            scrollAtBottom = true;
        }

        let span = document.createElement("span");
        if (data.type == 0x01) { // error
            span.classList.add("telemetry-error");
        } else {
            span.classList.add("telemetry-normal");
        }

        span.textContent = data.msg;
        telemetry.appendChild(span);

        //if scroll is at the bottom, scroll to the bottom.
        if (scrollAtBottom) {
            telemetry.scrollTop = telemetry.scrollHeight - telemetry.clientHeight - 1;
        }
    }
}

customElements.define("view-telemetry", ViewTelemetry);