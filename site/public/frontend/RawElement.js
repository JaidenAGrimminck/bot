
async function RawElement(backend_element_name, insert = true) {
    const response = await fetch(`/frontend/${backend_element_name}/${backend_element_name}.html`);
    const text = await response.text();
    return (insert) ? text : text.replace("$CONTENT$", "");
}