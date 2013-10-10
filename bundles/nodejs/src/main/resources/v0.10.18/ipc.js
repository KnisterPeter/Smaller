var util = require('util');

var writeResponse = function(response) {
	process.stdout.write(response + '\n');
}

var stdout = [];
var stderr = [];
console.log = function() {
  var args = Array.prototype.slice.call(arguments);
  args.forEach(function(arg) {
    stdout.push(util.inspect(arg));
  });
}
console.error = function() {
  var args = Array.prototype.slice.call(arguments);
  args.forEach(function(arg) {
    stderr.push(util.inspect(arg));
  });
}

process.stdin.resume();
process.stdin.setEncoding('utf8');

process.stdin.on('data', function(chunk) {
  try {
    var command = JSON.parse(chunk);
    process.chdir(command.cwd);
    require('index')(command, function() {
      writeResponse(JSON.stringify(
        {
          'result': 'done',
          'stdout': stdout, 
          'stderr': stderr
        }));
      });
  } catch (e) {
    writeResponse(JSON.stringify(
      {
        'error': util.inspect(e), 
        'stdout': stdout, 
        'stderr': stderr
      }));
  }
});

writeResponse('ipc-ready');
