var result = "";
var out = { 
    Write: function(s) { result += s; },
    WriteLine: function(s) { result += s + "\n"; }
  };
var err = {};

var compiler = new TypeScript.TypeScriptCompiler(out, err);
compiler.setErrorCallback(function(start, len, msg) { /* do smth useful here */ });
compiler.parser.errorRecovery = true;

compile = function(code) {
  compiler.addUnit(code, 'code.js');
  compiler.typeCheck();
  compiler.reTypeCheck();
  compiler.emit(false, function(name) { return out; });
  return result;
};
