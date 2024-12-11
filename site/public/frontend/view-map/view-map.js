class ViewMap extends HTMLElement {
    constructor() {
        super();

        this.map = null;
        this.ctx = null;
        this.mapLoop = null;

        this.properlyScaled = false;

        this.cmToPixelScale = 0.8;
    }

    async connectedCallback() {
        this.innerHTML = (await RawElement("view-map", false));

        this.map = this.querySelector("#view-map-canvas");
        this.ctx = this.map.getContext("2d", {alpha: false});

        setInterval(() => {
            this.draw();
        }, 1000 / 30);
        
        this.querySelector("#view-map-container").classList.remove("cc-hidden");

        const dpr = window.devicePixelRatio || 1;
        const bsr = this.ctx.webkitBackingStorePixelRatio ||
            this.ctx.mozBackingStorePixelRatio ||
            this.ctx.msBackingStorePixelRatio ||
            this.ctx.oBackingStorePixelRatio ||
            this.ctx.backingStorePixelRatio || 1;

        const ratio = dpr / bsr;

        const canvasWidth = Math.round(this.map.clientWidth);
        const canvasHeight = Math.round(this.map.clientHeight);

        this.map.width = canvasWidth * ratio;
        this.map.height = canvasHeight * ratio;
        this.map.style.width = canvasWidth + "px";
        this.map.style.height = canvasHeight + "px";

        this.ctx.scale(ratio, ratio);
        this.ctx.setTransform(ratio, 0, 0, ratio, 0, 0);
        
        this.ctx.imageSmoothingEnabled = true;
        
        //get parent
        this.querySelector("#view-map-container").classList.add("cc-hidden");
    }

    async draw() {
        this.ctx.clearRect(0, 0, this.map.width, this.map.height);
        this.ctx.fillStyle = "rgb(242,242,242)";
        this.ctx.fillRect(0, 0, this.map.width, this.map.height);

        //draw robot in the center
        this.ctx.fillStyle = "rgb(0,0,0)";
        this.ctx.save();
        // translate to center
        this.ctx.translate(this.map.width / 2, this.map.height / 2);
        this.ctx.rotate(0);
        this.ctx.fillRect(-20 * this.cmToPixelScale, -30 * this.cmToPixelScale, 40 * this.cmToPixelScale, 60 * this.cmToPixelScale);

        // reset translate
        this.ctx.restore();
    }
}

customElements.define("view-map", ViewMap);