
const rname = "req-use";

class Require extends HTMLElement {
    constructor() {
        super();
    }
}

customElements.define(rname, Require);

window.onload = (async function() {
    if (document.getElementsByTagName(rname.toUpperCase()).length === 0) {
        console.error(rname + " element not found");
        return;
    }

    //add RawElement script first
    const script = document.createElement('script');
    script.src = '/frontend/RawElement.js';

    document.head.appendChild(script);

    await new Promise((resolve) => {
        script.onload = resolve;
    });

    console.log("RawElement script loaded, importing other necessary scripts...")

    //import any other necessary scripts
    await ImportScript("frontend/Queue.js");
    await ImportScript("misc/uuid.js")

    console.log("Added necessary scripts, loading elements...")

    //load custom elements

    const elements = document.getElementsByTagName(rname.toUpperCase())[0].getAttribute('elements').split(',');

    let t = performance.now();

    let unsuccessful = 0;
    let dependencies = 0;
    let loaded = [];

    async function load(element, dependent=false) {
        const response = await fetch(`/frontend/${element}`);
        const json = await response.json();
        
        if (!json.jsExists) {
            unsuccessful++;
            return;
        }

        if (json.requirements.length > 0) {
            for (let requirement of json.requirements) {
                if (!loaded.includes(requirement)) {
                    await load(requirement, true);
                }
            }
        }
        
        const script = document.createElement('script');
        script.src = `/frontend/${element}/${element}.js`;
        document.head.appendChild(script);
        
        if (json.cssExists) {
            const css = document.createElement('link');
            css.rel = 'stylesheet';
            css.href = `/frontend/${element}/${element}.css`;
            document.head.appendChild(css);
        }

        loaded.push(element);
        if (dependent) {
            dependencies++;
        }
    }

    for (let element of elements) {
        await load(element);
    }

    console.log(`Finished loading ${loaded.length} elements (${dependencies} dependencies, ${unsuccessful} failed to load) in ${Math.round((performance.now() - t) * 10) / 10}ms`);
});