var eslint = require('./node_modules/eslint/lib/cli');
var fs = require('fs');
var path = require('path');

module.exports = function(command, done) {
  delete command.options.version;
  process.chdir(command.indir);
  fs.writeFile('__custom-eslint.rc', JSON.stringify(command.options), function (e) {
    if (e) {
      console.error(e);
      done();
    } else {
      var exit = process.exit;
      try {
        process.exit = function() {};
        var log = console.log
        try {
          var errors = [];
          console.log = function(str) { errors.push(str); log(str); }
          eslint.execute(['--config', '__custom-eslint.rc', '.']);
          fs.writeFile(path.join(command.outdir, 'result.json'), errors.join('\n'), function(e) {
            if (e) {
              console.error(e);
            }
            done('/result.json');
          });
        } finally {
          console.log = log
        }
      } finally {
        process.exit = exit;
      }
    }
  });
}
