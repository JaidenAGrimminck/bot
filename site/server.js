const express = require('express');
const path = require('path');

const frontendRouter = require('./routes/frontend/CustomElementManager.js');

const port = process.env.PORT || 8080;

const app = express();

app.use(express.static(path.join(__dirname, 'public')));
app.use('/frontend', frontendRouter);

app.get('/', (req, res) => {
    res.sendFile(path.join(__dirname, "public/home/index.html"))
});

app.get('/require.js', (req, res) => {
    res.sendFile(path.join(__dirname, "public/Require.js"))
})

app.listen(port, () => {
    console.log(`Server is running on port ${port}`);
});