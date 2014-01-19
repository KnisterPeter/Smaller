var cs = require('coffee-script');
var file = require('file');
var fs = require('fs');
var path = require('path');
var mkdirp = require('mkdirp');

var getAllFiles = function(indir, type) {
  var expr = new RegExp('.*\.' + type + '$');
  var queue = [];
  file.walkSync(indir, function(dir, dirs, files) {
    files.forEach(function(f) {
      if (expr.test(f)) {
        queue.push(path.join(dir, f));
      }
    });
  });
  return queue;
}

module.exports = function(command, done) {
  var withSourceMaps = command.options['source-maps'] == 'true' ? true : false;
  if (withSourceMaps) {
    var convert = require('convert-source-map');
  }
  
  var queue = getAllFiles(command.indir, 'coffee');
  queue.forEach(function(f) {
    var rel = path.relative(command.indir, f);
    console.log('Compiling ' + rel);
    var result = cs.compile(fs.readFileSync(f, {encoding:'utf8'}), {
      generatedFile: rel,
      literate: false,
      bare: command.options['bare'] || false,
      header: command.options['header'] || false,
      sourceMap: withSourceMaps,
      inline: true
    });
    var target = path.join(command.outdir, rel.replace('.coffee', '.js'));
    var js = result;
    if (withSourceMaps) {
      var map = convert.fromJSON(result.v3SourceMap);
      map.setProperty('sources', [rel]);
      js = result.js + '\n' + map.toComment();
    }
    mkdirp(path.dirname(target), 0777, function(e) {
      if (e && e.errno != 47) {
        console.error(e);
        done();
      } else {
        fs.writeFile(target, js, {encoding:'utf8'}, function(e) {
          if (e) {
            console.error(e);
            done();
          } else {
            console.log('Written ' + target);
            queue = queue.slice(1);
            if (queue.length == 0) done();
          }
        });
      }
    });
  });
}
