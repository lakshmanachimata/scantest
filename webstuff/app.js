var express = require('express');
var app = express();
var path = require('path');

// viewed at http://localhost:6666
app.get('/', function (req, res) {
    res.sendFile(__dirname + '/testin.html');
});

app.listen(8080);