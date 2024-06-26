
async function RawElement(backend_element_name, insert = true) {
    let htmlFile = backend_element_name;
    if (backend_element_name.includes("//")) {
        htmlFile = backend_element_name.split("//")[1];
        backend_element_name = backend_element_name.split("//")[0];
    }

    const response = await fetch(`/frontend/${backend_element_name}/${htmlFile}.html`);

    if (!response.ok) {
        throw new Error(`Failed to load element '${backend_element_name}/${htmlFile}'!`);
    }

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