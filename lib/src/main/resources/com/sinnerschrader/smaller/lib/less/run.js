// FIX bug in envjs/rhino which returns undefined as http-status
var __oldSend = XMLHttpRequest.prototype.send;
XMLHttpRequest.prototype.send = function() {
  __oldSend.apply(this, arguments);
  this.status = this.responseText != null ? 0 : undefined;
};
// FIX set correct base path (EnvJS simulates a browser)
window.location.href = base;
//FIX url rewriting behaviour
var __oldURL = less.tree.URL.prototype;
less.tree.URL = function (val, paths) {
  if (val.data) {
      this.attrs = val;
  } else {
      this.value = val;
      this.paths = paths;
  }
};
less.tree.URL.prototype.toCSS = __oldURL.toCSS;
less.tree.URL.prototype.eval = __oldURL.eval;

var lessIt = function(css) {
    var result;
    var parser = new less.Parser({ 
        optimization: 1
      });

    parser.parse(css, function (e, root) {
    	if (e) {
       		throw e;    		
    	}
    	result = css;
   		result = root.toCSS();
    });
    return result;
};
