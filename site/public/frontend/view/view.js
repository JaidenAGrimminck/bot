
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

        const children = this.querySelector(".main-top-bar-selection").children;

        for (let i = 0; i < children.length; i++) {
            children[i].addEventListener("click", this.select.bind(this));
        }
    }

    async select(e) {
        let children = this.querySelector(".main-top-bar-selection").children;

        for (let i = 0; i < children.length; i++) {
            children[i].classList.remove("top-bar-selected");
        }

        e.target.classList.add("top-bar-selected");
    }
}

class VariablesView extends HTMLElement {
    constructor() {
        super();


    }

    async connectedCallback() {
        this.innerHTML = (await RawElement("view//variablesview", false));
        this.classList.add("variables-view");
    }
}

class View extends HTMLElement {
    constructor() {
        super();

        this.adjustment = {
            selected: false
        }

        this.bottomAdjustment = {
            selected: false
        }

        this.sideMaxWidth = 0.50; // percentage
        this.minSideWidth = 200;
    }

    async connectedCallback() {
        this.innerHTML = (await RawElement("view", false));

        this.querySelector(".side-size-adjuster").addEventListener("mousedown", this.initAdjustSize.bind(this));
        document.addEventListener("mousemove", this.adjustSize.bind(this));
        document.addEventListener("mouseup", this.endAdjustSize.bind(this));
        document.addEventListener("mouseleave", this.endAdjustSize.bind(this));
        
        //cheat to let element load first
        setTimeout(() => {
            this.querySelector(".bottom-size-adjuster").addEventListener("mousedown", this.initAdjustBottomSize.bind(this));
            document.addEventListener("mousemove", this.adjustBottomSize.bind(this));
            document.addEventListener("mouseup", this.endAdjustBottomSize.bind(this));
            document.addEventListener("mouseleave", this.endAdjustBottomSize.bind(this));
        }, 100)

        //when the window is resized, adjust the size of the side
        window.addEventListener("resize", () => {
            let sideWidth = this.querySelector(".side").getBoundingClientRect().width;
            this.querySelector(".side").style.width = sideWidth + "px";
            this.querySelector(".main").style.width = "calc(100% - " + sideWidth + "px)";
            document.querySelector(".bottom-size-adjuster").style.left = "calc(" + sideWidth + "px + (100% - " + sideWidth + "px) / 2)";
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

        let maxWidth = window.innerWidth * this.sideMaxWidth;

        if (x > maxWidth) {
            x = maxWidth;
        }
        if (x < this.minSideWidth) {
            x = this.minSideWidth;
        }

        //adjust width to mouse position
        this.querySelector(".side").style.width = (x) + "px";
        this.querySelector(".main").style.width = "calc(100% - " + (x) + "px)";
        document.querySelector(".bottom-size-adjuster").style.left = "calc(" + x + "px + (100% - " + x + "px) / 2)";

        //get new bounding box of side
        let sideBB = this.querySelector(".side").getBoundingClientRect();

        let sideWidth = sideBB.width;

        this.querySelector(".side-size-adjuster").style.left = (sideWidth - 7) + "px";
    }

    async endAdjustSize() {
        this.adjustment.selected = false;
        document.querySelector("body").style.cursor = "default";
    }

    async initAdjustBottomSize(e) {
        this.bottomAdjustment.selected = true;
        document.querySelector("body").style.cursor = "ns-resize";
    }

    async adjustBottomSize(e) {
        if (!this.bottomAdjustment.selected) return;

        //get mouse position
        let x = e.clientX;
        let y = e.clientY;

        let height = window.innerHeight - y;

        if (height < 200) {
            height = 200;
        } else if (height > 400) {
            height = 400;
        }

        this.querySelector(".bottom-size-adjuster").style.bottom = (height - 6) + "px";
        this.querySelector(".variables-view").style.height = height + "px";
    }

    async endAdjustBottomSize() {
        this.bottomAdjustment.selected = false;
        document.querySelector("body").style.cursor = "default";
    }
}

customElements.define("view-sideitem", ViewSideItem);
customElements.define("info-view", View);
customElements.define("view-top-bar", ViewTopBar);
customElements.define("variables-view", VariablesView);