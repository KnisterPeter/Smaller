var fs = require('fs');
var path = require('path');
var file = require('file');
var UglifyJS = require("uglify-js");
var convert = require('convert-source-map');
var mkdirp = require('mkdirp');

module.exports = function(command, done) {
  var withSourceMaps = command.options['source-maps'] == 'true' ? true : false;
  
  var abs = path.join(command.indir, command.file);
  var opts = {};
  if (withSourceMaps) {
    var inSourceMap = convert.fromSource(fs.readFileSync(abs, {encoding:'utf8'}));
    opts = {
        inSourceMap: inSourceMap.souremap,
        outSourceMap: path.basename(abs) + ".map"
    };
  }
  
  var result = UglifyJS.minify(abs, opts);
  var target = path.join(command.outdir, command.file);
  
  var js = result.code;
  if (withSourceMaps) {
    var map = convert.fromJSON(result.map);
    map.setProperty('sources', [command.file]);
    map.setProperty('sourcesContent', []);
    js += "\n" + map.toComment() + "\n";
  }
  
  mkdirp(path.dirname(target), function(e) {
    if (e) {
      console.log(e);
      done();
    } else {
      fs.writeFile(target, js, {encoding:'utf8'}, function(e) {
        if (e) {
          console.log(e);
          done();
        } else {
          console.log('Written uglify result: ' + target);
          done(command.file);
        }
      });
    }
  });
}
