var csso = require('csso');
var fs = require('fs');
var path = require('path');

module.exports = function(command, done) {
  if (!command.file) { 
    throw new Error("No input file specified");
  }
  fs.writeFileSync(
      path.join(command.outdir, command.file), 
      csso.justDoIt(
          fs.readFileSync(
              path.join(command.indir, command.file), 
              {encoding:'utf8'})));
  done(command.file);
}
