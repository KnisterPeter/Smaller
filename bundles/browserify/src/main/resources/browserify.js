var browserify = require('browserify');
var fs = require('fs');
var path = require('path');
var through = require('through');
var convert = require('convert-source-map');
var mkdirp = require('mkdirp');

var browserResolve = require('browser-resolve');

module.exports = function(command, done) {
  if (!command.file) { 
    throw new Error("No input file specified");
  }
  process.chdir(command.indir);
  var withSourceMaps = command.options['source-maps'] == 'true' ? true : false;
  var abs = path.join(command.indir, command.file);
  var min = '';
  
  var aliases = !!command.options['aliases'] ? command.options['aliases'] : [];
  console.log('aliases: ' + aliases);
  var transforms = !!command.options['transforms'] ? command.options['transforms'] : [];
  console.log('transforms: ' + transforms);
  
  console.log('Processing ' + abs);
  var b = browserify({
      resolve: function resolve(id, opts, cb) {
        if (id.indexOf(command.indir) === -1 && id[0] == '/') {
          id = path.join(command.indir, id);
        }
        return browserResolve(id, opts, cb);
      }
    });
  transforms.forEach(function(transform) { b.transform(require(transform)); });
  b.add(abs);
  
  for (var alias in aliases) {
    if (aliases.hasOwnProperty(alias)) {
      b.require(alias, {expose: aliases[alias]})
    }
  }
  
  b.bundle({ debug: withSourceMaps }).pipe(through(
      function(data) { min += data; }, 
      function() {
        if (withSourceMaps) {
          var sourceMap = convert.fromSource(min);
          sourceMap.setProperty('sources', sourceMap.getProperty('sources').map(
            function(source) { return path.relative(command.indir, source); }));
          min = convert.removeComments(min);
          min += '\n' + sourceMap.toComment() + '\n';
        }
        fs.writeFile(path.join(command.outdir, 'output.js'), min, function(e) {
          if (e) {
            console.log(e);
            done();
          } else {
            console.log('Written result to ' + path.join(command.outdir, 'output.js'));
            done('output.js');
          }
        });
      }));
}
