var tsc = require('node-tsc');
var file = require('file');
var fs = require('fs');
var path = require('path');
var convert = require('convert-source-map');

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
  
  var queue = getAllFiles(command.indir, 'ts');
  var opts = ['--module', 'commonjs', '--outDir', command.outdir];
  if (withSourceMaps) {
    opts.push('--sourcemap')
  }
  var exitCode = tsc.compile(queue, opts, function(e) {
    console.error(e);
    return false;
  });
  console.log('tsc exitCode: ' + exitCode);
  if (exitCode != 0) {
    throw new Error('Failed to compile typescript');
  }
  if (withSourceMaps) {
    queue.forEach(function(f) {
      var rel = path.relative(command.indir, f);
      var target = path.join(command.outdir, rel.replace('.ts', '.js'));
      
      fs.readFile(target, {encoding:'utf8'}, function(e, data) {
        if (e) throw e;
        js = data;
        var sourceMap = convert.fromMapFileSource(js, command.outdir);
        sourceMap.setProperty('sources', [rel]);
        js = convert.removeMapFileComments(js);
        js += '\n' + sourceMap.toComment() + '\n';
        
        fs.writeFile(target, js, function() {
          queue = queue.slice(1);
          if (queue.length == 0) done();
        });
      });
    });
  } else {
    done();
  }
}
