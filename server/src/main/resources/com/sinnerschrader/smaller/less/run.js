// FIX bug in envjs/rhino which returns undefined as http-status
var __oldSend = XMLHttpRequest.prototype.send;
XMLHttpRequest.prototype.send = function() {
  __oldSend.apply(this, arguments);
  this.status = this.responseText != null ? 0 : undefined;
};

var lessIt = function(css) {
    var result;
    var parser = new less.Parser({ 
        optimization: 2,
        paths: [base]
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
