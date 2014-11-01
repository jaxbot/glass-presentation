var WebSocket = require('ws');
var ws = new WebSocket('ws://localhost:9811/');
ws.on('message', function(data, flags) {
    // flags.binary will be set if a binary data is received
    // flags.masked will be set if the data was masked
    console.log(data);
});

