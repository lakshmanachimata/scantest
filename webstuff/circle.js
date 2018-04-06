var connect = require('connect');
var serveStatic = require('serve-static');
connect().use(serveStatic("./")).listen(6666, function () {
    console.log('Server running on 6666...');
});