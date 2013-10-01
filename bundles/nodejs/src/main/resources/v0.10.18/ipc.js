var writeResponse = function(response) {
	process.stdout.write(response + '\n');
}

process.stdin.resume();
process.stdin.setEncoding('utf8');

process.stdin.on('data', function(chunk) {
  writeResponse(chunk);
});

writeResponse('ipc-ready');
