window = {
  location : {
    href : "",
    port : ""
  }
};
location = window.location;

document = {
  getElementById : function() {
    return {
      childNodes : [],
      style: {},
      appendChild : function() {
      }
    };
  },
  getElementsByTagName : function() {
    return [];
  },
  createElement : function() {
    return {
      style: {}
    };
  },
  createTextNode : function() {
    return {};
  }
};

window.XMLHttpRequest = function() {
  this.status = 200;
  this.url = null;
  this.resource = null;
};
window.XMLHttpRequest.prototype.open = function(method, url, async) {
  this.url = url;
};
window.XMLHttpRequest.prototype.setRequestHeader = function(name, value) {
};
window.XMLHttpRequest.prototype.send = function(data) {
  this.responseText = new String(resolver.resolve(this.url).getContents());
};
window.XMLHttpRequest.prototype.getResponseHeader = function(name) {
};
XMLHttpRequest = window.XMLHttpRequest;

lessIt = function(data) {
  var result;
  var parser = new window.less.Parser({
    optimization : 1
  });

  parser.parse(data, function(e, root) {
    if (e) {
      throw e.message;
    }
    result = data;
    result = root.toCSS();
  });
  return result;
}
