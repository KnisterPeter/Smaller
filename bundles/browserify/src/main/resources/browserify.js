var browserify = require('browserify');
var fs = require('fs');
var path = require('path');
var through = require('through');
var convert = require('convert-source-map');

module.exports = function(command, done) {
  if (!command.file) { 
    throw new Error("No input file specified");
  }
  var withSourceMaps = command.options['source-maps'] == 'true' ? true : false;
  var abs = path.join(command.indir, command.file);
  var min = '';
  browserify()
    .add(abs)
    .bundle({ debug: withSourceMaps }).pipe(through(
      function(data) { min += data; }, 
      function() {
        if (withSourceMaps) {
          var sourceMap = convert.fromSource(min);
          sourceMap.setProperty('sources', sourceMap.getProperty('sources').map(
            function(source) { return path.relative(command.indir, source); }));
          min = convert.removeComments(min);
          min += '\n' + sourceMap.toComment() + '\n';
        }
        fs.writeFileSync(path.join(command.outdir, 'output.js'), min);
        done('output.js');
      }));
}
