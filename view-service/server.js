const express = require('express');
const path = require('path');
const axios = require('axios');
const app = express();
const port = 8080;

app.use(express.static(path.join(__dirname, 'public')));
app.use(express.json());

const getConfig = (req) => {
    return {
        headers: {
            'Cookie': req.headers.cookie || '',
            'Content-Type': 'application/json'
        }
    };
};

// Book Service Proxy (App3 - 8083)
app.use('/api/books', async (req, res) => {
    try {
        const url = `http://book-service:8083/api/books${req.url === '/' ? '' : req.url}`;
        const config = getConfig(req);

        const response = await axios({
            method: req.method,
            url: url,
            data: req.body,
            ...config
        });
        res.status(response.status).json(response.data);
    } catch (error) {
        res.status(error.response ? error.response.status : 500).json(error.response ? error.response.data : error.message);
    }
});

// Member Service Proxy (App1 - 8081)
app.use('/api/members', async (req, res) => {
    try {
        const url = `http://member-service:8081/api/members${req.url === '/' ? '' : req.url}`;
        const config = getConfig(req);

        const response = await axios({ method: req.method, url: url, data: req.body, ...config });
        res.status(response.status).json(response.data);
    } catch (error) { res.status(error.response ? error.response.status : 500).json(error.message); }
});

// User Endpoints Proxy (App1 - 8081)
app.use('/api/users', async (req, res) => {
    try {
        const url = `http://member-service:8081/api/users${req.url === '/' ? '' : req.url}`;
        const config = getConfig(req);

        const response = await axios({ method: req.method, url: url, data: req.body, ...config });
        res.status(response.status).json(response.data);
    } catch (error) { res.status(error.response ? error.response.status : 500).json(error.message); }
});

// Admin Endpoints Proxy (App1 - 8081)
app.use('/api/admin', async (req, res) => {
    try {
        const url = `http://member-service:8081/api/admin${req.url === '/' ? '' : req.url}`;
        const config = getConfig(req);

        const response = await axios({ method: req.method, url: url, data: req.body, ...config });
        res.status(response.status).json(response.data);
    } catch (error) { res.status(error.response ? error.response.status : 500).json(error.message); }
});

// Borrowing Service Proxy (App2 - 8082)
app.use('/api/borrowings', async (req, res) => {
    try {
        const url = `http://borrowing-service:8082/api/borrowings${req.url === '/' ? '' : req.url}`;
        const config = getConfig(req);

        const response = await axios({ method: req.method, url: url, data: req.body, ...config });
        res.status(response.status).json(response.data);
    } catch (error) { res.status(error.response ? error.response.status : 500).json(error.message); }
});

app.use('/api/requests', async (req, res) => {
    try {
        const url = `http://borrowing-service:8082/api/requests${req.url === '/' ? '' : req.url}`;
        const config = getConfig(req);

        const response = await axios({ method: req.method, url: url, data: req.body, ...config });
        res.status(response.status).json(response.data);
    } catch (error) { res.status(error.response ? error.response.status : 500).json(error.message); }
});

app.get('*', (req, res) => {
    res.sendFile(path.join(__dirname, 'index.html'));
});

app.listen(port, () => {
    console.log(`ViewAPI Node server running on port ${port}`);
});