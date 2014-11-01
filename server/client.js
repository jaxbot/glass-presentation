var WebSocket = require('ws');
var ws = new WebSocket('ws://localhost:9811/');
ws.on('message', function(data, flags) {
    // flags.binary will be set if a binary data is received
    // flags.masked will be set if the data was masked
    console.log(data);
    switch (data) {
      case "next":
	sendKey(125);
	break;
      case "prev":
	sendKey(126);
	break;
    }
});

var exec = require('child_process').exec;
function sendKey(keycode) {
  exec("osascript -e 'tell application \"System Events\" to key code " + keycode + "'");
}
