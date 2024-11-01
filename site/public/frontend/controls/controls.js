

//https://medium.com/wdstack/fixing-html5-2d-canvas-blur-8ebe27db07da
function fix_dpi(canvas) {
    //create a style object that returns width and height
    let style = {
        height() {
        return +getComputedStyle(canvas).getPropertyValue('height').slice(0,-2);
        },
        width() {
        return +getComputedStyle(canvas).getPropertyValue('width').slice(0,-2);
        }
    }

    let dpi = window.devicePixelRatio;

    //set the correct attributes for a crystal clear image!
    canvas.setAttribute('width', style.width() * dpi);
    canvas.setAttribute('height', style.height() * dpi);
}

class Controls extends HTMLElement {
    constructor() {
        super();

        this.joystick_canvas = null;
        this.ctx = null;

        this.controls = {
            x: 0,
            y: 0,
            lastX: 0,
            lastY: 0
        }
    }

    async connectedCallback() {
        this.innerHTML = (await RawElement("controls", false)).replace("$$CONTROLS-CANVAS-ID$$", "joystick-canvas");

        this.joystick_canvas = document.getElementById("joystick-canvas");
        this.ctx = this.joystick_canvas.getContext("2d");

        //fix_dpi(this.joystick_canvas);

        setInterval(() => {
            this.draw(this.ctx);
        }, 1000 / 60);

        setInterval(() => {
            if (this.controls.x === this.controls.lastX && this.controls.y === this.controls.lastY) {
                return;
            }

            let xBuffer = new ArrayBuffer(8);
            let xView = new DataView(xBuffer);
            xView.setFloat64(0, this.controls.x);

            let yBuffer = new ArrayBuffer(8);
            let yView = new DataView(yBuffer);
            yView.setFloat64(0, this.controls.y);

            sendPayload([
                0x01,
                0x02,
                0xD5,
                ...new Uint8Array(xBuffer),
                ...new Uint8Array(yBuffer)
            ])

            this.controls.lastX = this.controls.x;
            this.controls.lastY = this.controls.y;
        }, 100);

        document.getElementById("controls-stop").addEventListener("click", () => {
            this.controls.x = 0;
            this.controls.y = 0;
        })

        this.joystick_canvas.addEventListener("click", (e) => {
            let rect = this.joystick_canvas.getBoundingClientRect();
            this.controls.x = ((e.clientX - rect.left) / rect.width - 0.5) * 2;
            this.controls.y = -((e.clientY - rect.top) / rect.height - 0.5) * 2;
        })
        
        this.joystick_canvas.addEventListener("mousemove", (e) => {
            if (e.buttons === 1) {
                let rect = this.joystick_canvas.getBoundingClientRect();
                this.controls.x = ((e.clientX - rect.left) / rect.width - 0.5) * 2;
                this.controls.y = -((e.clientY - rect.top) / rect.height - 0.5) * 2;
            }
        });
    }

    draw(ctx) {
        ctx.clearRect(0, 0, 300, 300);
        ctx.fillStyle = "black";
        ctx.fillRect(0, 0, 300, 300);

        ctx.fillStyle = "rgb(230,230,230)";
        for (let y = 0; y < 300; y += 300 / 20) {
            ctx.fillRect(0, y, 300, 1);
        }

        for (let x = 0; x < 300; x += 300 / 20) {
            ctx.fillRect(x, 0, 1, 300);
        }
        
        ctx.fillStyle = "rgb(200,200,200)";
        ctx.fillRect(0, 149, 300, 2);
        ctx.fillRect(149, 0, 2, 300);

        let px = this.controls.x * 150 + 150;
        let py = -this.controls.y * 150 + 150;

        ctx.fillStyle = "rgb(0,0,255)";
        ctx.beginPath();
        ctx.arc(px, py, 6, 0, Math.PI * 2);
        ctx.fill();
    }
}

customElements.define("c-controls", Controls);