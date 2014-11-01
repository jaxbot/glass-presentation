var http = require("http");
var WebSocketServer = require('ws').Server
  , wss = new WebSocketServer({port: 9811});

wss.broadcast = function(data) {
	for(var i in this.clients)
		this.clients[i].send(data);
};

http.createServer(function(req, res) {
  console.log(req.url);
  switch (req.url) {
    case "/next":
      wss.broadcast("next");
      break;
    case "/prev":
      wss.broadcast("prev");
      break;
  }

  res.end("Sure");
}).listen(9810, '192.168.1.104');
