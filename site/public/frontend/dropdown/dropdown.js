
class Dropdown extends HTMLElement {
    constructor() {
        super();
        this.id = createUUID().replaceAll("-", "");

        this.options = [];
        this.selected = "";
        this.selector = false;
    }

    onChange(newOption, changeTitle=true) {
        this.selected = newOption;
        this.setAttribute("value", newOption);

        if (changeTitle) {
            this.querySelector(".dropbtn").textContent = newOption;
        }
    }

    async connectedCallback() {
        let b4 = this.innerHTML + "";

        let title = this.getAttribute("title");

        if (title == undefined || title == null) {
            title = "Dropdown";
        }

        if (title == "$current") {
            this.selector = true;

            for (let child of this.children) {
                this.options.push(child.textContent);
            }
            this.onChange(this.options[0], false);

            title = this.options[0];
        }

        this.innerHTML = (await RawElement("dropdown", true))
            .replace("$CONTENT$", b4)
            .replaceAll("$ID$", this.id)
            .replace("$TITLE$", title);

        window["dropdown_" + this.id] = () => {
            this.querySelector(`#drp${this.id}`).classList.toggle("show");
        }

        if (this.selector) {
            for (let child of this.querySelector(".dropdown-content").children) {
                child.addEventListener("click", () => {
                    this.onChange(child.textContent);
                });
            }
        }
        
        window.addEventListener("click", (e) => {
            if (!e.target.matches(`.dropbtn`)) {
                let drpdwns = document.getElementsByClassName("dropdown-content");

                for (let drpdwn of drpdwns) {
                    if (drpdwn.classList.contains("show")) {
                        drpdwn.classList.remove("show")
                    }
                }
            }
        });
    }   
}

customElements.define("c-dropdown", Dropdown);