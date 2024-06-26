
async function RawElement(backend_element_name, insert = true) {
    const response = await fetch(`/frontend/${backend_element_name}/${backend_element_name}.html`);
    const text = await response.text();
    return (insert) ? text : text.replace("$CONTENT$", "");
}

const importedScripts = [];

function ImportScript(src) {
    if (importedScripts.includes(src)) {
        return;
    }

    const script = document.createElement('script');
    document.head.appendChild(script);
    script.src = src;

    importedScripts.push(src);

    return new Promise((resolve, reject) => {
        script.onload = resolve;
        script.onerror = reject;
    });
}