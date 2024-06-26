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
        let requirements = [];

        const htmlPath = `${elementFolderPath}/${elementName}.html`;
        const jsPath = `${elementFolderPath}/${elementName}.js`;
        const cssPath = `${elementFolderPath}/${elementName}.css`;
        const requirementsPath = `${elementFolderPath}/need.txt`;

        if (fs.existsSync(htmlPath)) {
            htmlExists = true;
        }
        if (fs.existsSync(jsPath)) {
            jsExists = true;
        }
        if (fs.existsSync(cssPath)) {
            cssExists = true;
        }
        if (fs.existsSync(requirementsPath)) {
            requirements = fs.readFileSync(requirementsPath).toString().split(",");
        }

        res.json({
            htmlExists,
            jsExists,
            cssExists,
            requirements
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