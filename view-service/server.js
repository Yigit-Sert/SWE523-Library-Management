const express = require('express');
const path = require('path');
const axios = require('axios');
const app = express();
const port = 8080;

app.use(express.static(path.join(__dirname, 'public')));
app.use(express.json());

async function forwardRequest(req, res, targetBaseUrl) {
    try {
        const url = `${targetBaseUrl}${req.originalUrl}`;

        const headers = { ...req.headers };

        const config = {
            method: req.method,
            url: url,
            headers: headers,
            data: req.method === 'GET' ? undefined : req.body,
            maxRedirects: 0,
            validateStatus: (status) => status < 500,
            responseType: 'arraybuffer'
        };

        delete config.headers['host'];

        const response = await axios(config);

        Object.keys(response.headers).forEach(key => {
            if (key.toLowerCase() === 'location') {
                let location = response.headers[key];
                location = location.replace('http://member-service:8081', 'http://localhost:8080');
                location = location.replace('http://borrowing-service:8082', 'http://localhost:8080');
                location = location.replace('http://book-service:8083', 'http://localhost:8080');
                res.setHeader(key, location);
            } else {
                res.setHeader(key, response.headers[key]);
            }
        });

        res.status(response.status).send(response.data);

    } catch (error) {
        console.error(`Proxy Error [${req.originalUrl}]:`, error.message);
        if (error.response) {
            res.status(error.response.status).send(error.response.data);
        } else {
            res.status(500).send("Gateway Error: " + error.message);
        }
    }
}

const MEMBER_SERVICE = 'http://member-service:8081';
const BORROWING_SERVICE = 'http://borrowing-service:8082';
const BOOK_SERVICE = 'http://book-service:8083';

// Auth
app.use('/oauth2', (req, res) => forwardRequest(req, res, MEMBER_SERVICE));
app.use('/login', (req, res) => forwardRequest(req, res, MEMBER_SERVICE));
app.use('/logout', (req, res) => forwardRequest(req, res, MEMBER_SERVICE));

// API
app.use('/api/members', (req, res) => forwardRequest(req, res, MEMBER_SERVICE));
app.use('/api/users', (req, res) => forwardRequest(req, res, MEMBER_SERVICE));
app.use('/api/admin', (req, res) => forwardRequest(req, res, MEMBER_SERVICE));

app.use('/api/borrowings', (req, res) => forwardRequest(req, res, BORROWING_SERVICE));
app.use('/api/requests', (req, res) => forwardRequest(req, res, BORROWING_SERVICE));

app.use('/api/books', (req, res) => forwardRequest(req, res, BOOK_SERVICE));

// Frontend
app.get('*', (req, res) => {
    res.sendFile(path.join(__dirname, 'index.html'));
});

app.listen(port, () => {
    console.log(`Gateway Node server running on port ${port}`);
});