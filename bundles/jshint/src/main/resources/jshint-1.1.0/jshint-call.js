hint = function(params) {
  params = JSON.parse(params);
  return JSON.stringify(JSHINT(params.source, params.options, {}) ? {} : JSHINT.data());
}
