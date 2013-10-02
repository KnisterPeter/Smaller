module.exports = function(command) {
  return 'bla';
  var browserify = require('browserify');

  var b = browserify();
  b.add(command.path + '/main.js');
  b.bundle().pipe(process.stderr);
  return command.path + '/main.js';
}
