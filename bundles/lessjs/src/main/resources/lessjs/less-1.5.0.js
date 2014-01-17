var less = require('less');
var fs = require('fs');
var path = require('path');
var mkdirp = require('mkdirp');

module.exports = function(command, done) {
  var abs = path.join(command.indir, command.file);
  var opts = {
    verbose: true,
    filename: command.file,
    paths: [path.dirname(abs)],
    sourceMap: command.options['source-maps'] == 'true' ? true : false,
    outputSourceFiles: command.options['source-maps'] == 'true' ? true : false
  };
  console.log('Processing: ' + abs);
  less.render(fs.readFileSync(abs, {encoding:'utf8'}), opts, function(e, css) {
    if (e) {
      console.error(e);
      done();
      return; 
    }
    var out = command.file.replace('.less', '.css');
    var target = path.join(command.outdir, out);
    console.log('Writing less result to ' + target);
    
    mkdirp(path.dirname(target), function (e) {
      if (e) {
        console.error(e);
        done();
      } else {
        var e = fs.writeFileSync(target, css, {encoding:'utf8'});
        if (e) {
          console.error(e);
          done();
          return; 
        }
        done(out);
      }
    });
  });
}
