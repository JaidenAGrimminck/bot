
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

    console.log("Added RawElement script, appending elements...")

    const elements = document.getElementsByTagName(rname.toUpperCase())[0].getAttribute('elements').split(',');

    let t = performance.now();

    let unsuccessful = 0;
    for (let element of elements) {
        const response = await fetch(`/frontend/${element}`);
        const json = await response.json();

        if (!json.jsExists) {
            unsuccessful++;
            continue;
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
    }

    console.log(`Finished loading ${elements.length - unsuccessful} elements (${unsuccessful} failed to load) in ${Math.round((performance.now() - t) * 10) / 10}ms`);
});