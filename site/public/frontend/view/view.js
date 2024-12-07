
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

        this.views_id = {
            "camera": "view-camera-container",
            "drivebase": "view-drivebase-container",
            "environment": "view-environment-container",
            "map": "view-map-container",
            "plot": "view-plot-container",
            "telemetry": "view-telemetry-container",
        }

        this.last_selected_robot = null;
        this.current_state = "disabled";

        let last_robot_status = 0;

        setTimeout(() => {
            if (robot_status["last_update"] - last_robot_status > 3000) {
                document.getElementById("live-view-container").innerHTML = `Live View <div class="live-dot">`;
            } else {
                document.getElementById("live-view-container").innerHTML = `Disconnected <div class="not-live-dot">`;
            }

            last_robot_status = Date.now();
        }, 100);
    }

    async connectedCallback() {
        this.innerHTML = (await RawElement("view//viewtopbar", false));

        const children = this.querySelector(".main-top-bar-selection").children;

        for (let i = 0; i < children.length; i++) {
            children[i].addEventListener("click", this.select.bind(this));
        }
        
        setTimeout(() => {
            for (let key of Object.keys(this.views_id)) {
                document.getElementById(this.views_id[key]).classList.add("cc-hidden");
            }
        })

        waitForRobotClasses((data) => {
            document.getElementById("robot-selection").innerHTML = "";

            let no_option = document.createElement("option");
            no_option.value = "none";
            no_option.textContent = "No Robot Selected";

            document.getElementById("robot-selection").appendChild(no_option);

            let editable = window.robot_status["editable"];

            let i = 0;
            for (let robot_class of data) {
                let option = document.createElement("option");
                option.value = i;
                option.textContent = robot_class.name;

                if (robot_class.disabled || !editable) {
                    option.disabled = true;
                }

                document.getElementById("robot-selection").appendChild(option);
                i++;
            }
        })

        document.getElementById("robot-selection").addEventListener("change", (e) => {
            let robotIndex = e.target.value;

            if (this.current_state != "disabled") {
                //cancel event
                e.target.value = this.last_selected_robot;
                return;
            }

            if (robotIndex == "none") {
                // set the play-pause to select
                document.getElementById("play-pause").src = "/images/select.svg";
                this.current_state = "disabled";
            } else {
                if (this.last_selected_robot == robotIndex) {
                    return;
                }

                // set the play-pause to play
                document.getElementById("play-pause").src = "/images/play.svg";
                this.current_state = "disabled";
            }

            document.getElementById("stop").src = "/images/empty.svg";

            this.last_selected_robot = robotIndex;
        });

        document.getElementById("play-pause").addEventListener("click", (e) => {
            let robotIndex = document.getElementById("robot-selection").value;

            if (robotIndex == "none") {
                return;
            }

            let src = e.target.src;

            if (src.includes("play")) {
                e.target.src = "/images/pause.svg";
                document.getElementById("stop").src = "/images/stop.svg";

                if (this.current_state == "disabled") {
                    setRobotState(robotIndex, ROBOT_STATES.START);
                } else if (this.current_state == "pause") {
                    setRobotState(robotIndex, ROBOT_STATES.RESUME);
                }

                this.current_state = "play";
            } else if (src.includes("pause")) {
                e.target.src = "/images/play.svg";

                this.current_state = "pause";
                setRobotState(robotIndex, ROBOT_STATES.PAUSE);
                document.getElementById("stop").src = "/images/stop.svg";
            }
        });

        document.getElementById("stop").addEventListener("click", (e) => {
            let robotIndex = document.getElementById("robot-selection").value;

            if (robotIndex == "none") {
                return;
            }

            if (this.current_state == "disabled") {
                return;
            }

            let src = e.target.src;

            if (src.includes("stop")) {
                e.target.src = "/images/empty.svg";
                this.current_state = "disabled";
                document.getElementById("play-pause").src = "/images/play.svg";

                setRobotState(robotIndex, ROBOT_STATES.STOP);
            }
        })
    }

    async select(e) {
        let children = this.querySelector(".main-top-bar-selection").children;

        let previousSelected = null;
        for (let i = 0; i < children.length; i++) {
            //check if the child is the one that was clicked
            if (children[i].classList.contains("top-bar-selected")) {
                previousSelected = i;
            }
            children[i].classList.remove("top-bar-selected");
        }

        for (let key of Object.keys(this.views_id)) {
            document.getElementById(this.views_id[key]).classList.add("cc-hidden");
        }

        if (previousSelected != null) {
            if (children[previousSelected] == e.target) {
                return;
            }
        }

        let target_text = e.target.querySelector("span").textContent.toLowerCase().replace(" ", "");

        document.getElementById(this.views_id[target_text]).classList.remove("cc-hidden");

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
customElements.define("variables-view", VariablesView);