
const arrowRightSVG = "/images/chevron-right.svg";
const arrowDownSVG = "/images/chevron-down.svg";
const circleSVG = "/images/circle-medium.svg";
const codeSVG = "/images/code-json.svg";
const horizSizeSVG = "/images/arrow-split-vertical.svg";

class ViewSideItem extends HTMLElement {
    constructor() {
        super();

        this.isOpen = false;
        this.hasChildren = false;
        this.selected = false;
    }

    async connectedCallback() {
        let childrenHTML = this.innerHTML;

        if (childrenHTML != "") {
            this.hasChildren = true;
        }

        this.innerHTML = (await RawElement("view//sideitem", false)).replace("{{children}}", childrenHTML);
        this.title = this.getAttribute("title");

        this.querySelector(".side-item-title").innerHTML = this.title;
        this.querySelector(".view-side-item-children-container").classList.add("cc-hidden");
        
        if (!this.hasChildren) {
            this.querySelector("img").src = codeSVG;
            this.querySelector(".view-side-item-content").addEventListener("click", this.select.bind(this));
        } else {
            this.querySelector(".view-side-item-content").addEventListener("click", this.toggle.bind(this));
            this.querySelector("img").src = arrowRightSVG;
        }
    }

    async select() {
        this.selected = !this.selected;

        if (this.selected) {
            this.children[0].classList.add("selected");
        } else {
            this.children[0].classList.remove("selected");
        }
    }

    async toggle() {
        this.isOpen = !this.isOpen;

        if (this.isOpen) {
            this.querySelector(".view-side-item-children-container").classList.remove("cc-hidden");
        } else {
            this.querySelector(".view-side-item-children-container").classList.add("cc-hidden");
        }
        
        if (this.hasChildren) {
            //change image to open/close
            let img = this.querySelector("img");

            if (this.isOpen) {
                img.src = arrowDownSVG;
            } else {
                img.src = arrowRightSVG;
            }
        }
    }
}

class ViewTopBar extends HTMLElement {
    constructor() {
        super();
    }

    async connectedCallback() {
        this.innerHTML = (await RawElement("view//viewtopbar", false));
    }
}

class View extends HTMLElement {
    constructor() {
        super();

        this.adjustment = {
            selected: false
        }
    }

    async connectedCallback() {
        this.innerHTML = (await RawElement("view", false));

        this.querySelector(".side-size-adjuster").addEventListener("mousedown", this.initAdjustSize.bind(this));
        document.addEventListener("mousemove", this.adjustSize.bind(this));
        document.addEventListener("mouseup", this.endAdjustSize.bind(this));
        document.addEventListener("mouseleave", this.endAdjustSize.bind(this));

        //when the window is resized, adjust the size of the side
        window.addEventListener("resize", () => {
            let sideWidth = this.querySelector(".side").getBoundingClientRect().width;
            this.querySelector(".side").style.width = sideWidth + "px";
            this.querySelector(".main").style.width = "calc(100% - " + sideWidth + "px)";
        });

        //and add mouse up event listener to window
    }

    async initAdjustSize(e) {
        this.adjustment.selected = true;
        document.querySelector("body").style.cursor = "ew-resize";
    }

    async adjustSize(e) {
        if (!this.adjustment.selected) return;

        //get mouse position
        let x = e.clientX;
        let y = e.clientY;

        //adjust width to mouse position
        this.querySelector(".side").style.width = (x) + "px";
        this.querySelector(".main").style.width = "calc(100% - " + (x) + "px)";

        //get new bounding box of side
        let sideBB = this.querySelector(".side").getBoundingClientRect();

        let sideWidth = sideBB.width;

        this.querySelector(".side-size-adjuster").style.left = (sideWidth - 7) + "px";
    }

    async endAdjustSize() {
        this.adjustment.selected = false;
        document.querySelector("body").style.cursor = "default";
    }
}

customElements.define("view-sideitem", ViewSideItem);
customElements.define("info-view", View);
customElements.define("view-top-bar", ViewTopBar);