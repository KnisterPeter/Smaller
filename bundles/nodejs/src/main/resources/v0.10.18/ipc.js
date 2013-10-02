var writeResponse = function(response) {
	process.stdout.write(response + '\n');
}

process.stdin.resume();
process.stdin.setEncoding('utf8');

process.stdin.on('data', function(chunk) {
  var command = JSON.parse(chunk);
  var resonse = require('index')(command);
  writeResponse(JSON.stringify(command));
});

writeResponse('ipc-ready');
