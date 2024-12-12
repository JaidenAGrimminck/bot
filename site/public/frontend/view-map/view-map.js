class ViewMap extends HTMLElement {
    constructor() {
        super();

        this.map = null;
        this.ctx = null;
        this.mapLoop = null;

        this.properlyScaled = false;

        this.cmToPixelScale = 0.8;

        this.drawRobot = true;
    }

    async connectedCallback() {
        this.innerHTML = (await RawElement("view-map", false));

        this.map = this.querySelector("#view-map-canvas");
        this.ctx = this.map.getContext("2d", {alpha: false});

        setTimeout(() => {
            document.querySelector("#view-map-container").classList.remove("cc-hidden");

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
            document.querySelector("#view-map-container").classList.add("cc-hidden");

            setInterval(() => {
                this.draw();
            }, 1000 / 30);    
        }, 50)
    }

    normalizeAngle(angle) {
        while (angle < 0) {
            angle += Math.PI * 2;
        }

        while (angle > Math.PI * 2) {
            angle -= Math.PI * 2;
        }

        return angle;
    }

    async draw() {
        this.ctx.clearRect(0, 0, this.map.width, this.map.height);
        this.ctx.fillStyle = "rgb(242,242,242)";
        this.ctx.fillRect(0, 0, this.map.width, this.map.height);

        if (this.drawRobot) {
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

        this.ctx.save();
        this.ctx.translate(this.map.width / 2, this.map.height / 2);

        this.ctx.fillStyle = "rgb(255,0,0)";
        
        for (let point of lastLidarUpdate) {
            let angle = point.angle;
            let distance = point.distance;

            // if (Math.abs(this.normalizeAngle(angle) - (3 * Math.PI / 2)) < 0.1) {
            //     this.ctx.fillStyle = "rgb(0,255,0)";
            // } else {
            //     this.ctx.fillStyle = "rgb(0,0,0)";
            // }

            let x = Math.cos(angle) * distance;
            let y = Math.sin(angle) * distance;

            x *= this.cmToPixelScale;
            y *= this.cmToPixelScale;

            this.ctx.fillRect(x, y, 1, 1);
        }

        // draw circle at center
        this.ctx.fillStyle = "rgb(255,0,0)";
        this.ctx.fillRect(-2, -2, 4, 4);

        this.ctx.restore();
        
    }
}

customElements.define("view-map", ViewMap);