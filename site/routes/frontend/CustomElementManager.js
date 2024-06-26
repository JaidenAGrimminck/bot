//express router
const express = require('express');
const fs = require('fs');

const router = express.Router();

router.get("/:elementName", (req, res) => {
    const elementName = req.params.elementName;
    const elementFolderPath = `./public/frontend/${elementName}`;

    if (fs.existsSync(elementFolderPath)) {
        let htmlExists = false;
        let jsExists = false;
        let cssExists = false;

        const htmlPath = `${elementFolderPath}/${elementName}.html`;
        const jsPath = `${elementFolderPath}/${elementName}.js`;
        const cssPath = `${elementFolderPath}/${elementName}.css`;

        if (fs.existsSync(htmlPath)) {
            htmlExists = true;
        }
        if (fs.existsSync(jsPath)) {
            jsExists = true;
        }
        if (fs.existsSync(cssPath)) {
            cssExists = true;
        }

        res.json({
            htmlExists,
            jsExists,
            cssExists
        });
    } else {
        res.json({
            htmlExists: false,
            jsExists: false,
            cssExists: false
        });
    }
});

module.exports = router;