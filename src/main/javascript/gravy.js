/*
This script defines the Gravy JavaScript/CoffeeScript API

Script bindings:

'module'  - the calling module
'isCoffee'
'log'
'out'
//'coffeec' - bind the CoffeeScript compiler to the
*/

/*
TODO create 'require' function:

require('script.name')
	when js script: load('script.name')
	when coffee script: 
		compile coffee script
		load('script.name.js') - i.e. myScript.coffee -> myScript.coffee.js
*/

/*
Java imports
*/

var DELETE, ERROR, FORWARD, GET, OPTIONS, POST, PUT, REQUEST, Route, attr, deserialize, doFilter, get, print, println, serialize;

importPackage(javax.servlet.http);

importPackage(javax.servlet);

importPackage(java.util);

/*
http methods
*/


GET = 'get';
POST = 'post';
PUT = 'put';
OPTIONS = 'post';
DELETE = 'delete';

/*
dispatch types
*/


REQUEST = DispatcherType.REQUEST;
FORWARD = DispatcherType.FORWARD;
ERROR = DispatcherType.ERROR;

/*
utility functions
*/


println = function(str) {
  return out.println(str);
};

print = function(str) {
  return out.print(str);
};

serialize = function(obj) {
  return JSON.stringify(obj);
};

deserialize = function(str) {
  return JSON.parse(str);
};

attr = function(key, value) {
  var ret;
  if (value == null) {
    value = null;
  }
  ret = null;
  if (value === null) {
    ret = deserialize(getAttribute(key));
  } else {
    ret = value;
    this.setAttribute(key, serialize(value));
  }
  return ret;
};
println('3')

doFilter = function() {
  return this.chain.doFilter();
};

/*
forward = (uri) =>
	// TODO

render = (viewUri, model) =>
	@req.forward
	// TODO
*/


/*
monkey patch: request, response, session
*/
/*
println('44')
println('httpservlet request prototype: '+ HttpServletRequest.prototype)
HttpServletRequest.prototype.attr = attr;
println('5')

HttpServletRequest.prototype.forward = forward;

HttpServletRequest.prototype.chain = {};
println('6')

HttpServletRequest.prototype.doFilter = attr;
println('7')

HttpServletResponse.prototype.render = {};

HttpServletResponse.prototype.req = {};

HttpServletResponse.prototype.out = {};

HttpServletSession.prototype.attr = attr;
*/
/*
*/


Route = (function() {
  var constructor;

  function Route() {}

  constructor = function(get, post, put, _delete, options, error, uriPattern, dispatch) {
    this.get = get;
    this.post = post;
    this.put = put;
    this["delete"] = _delete;
    this.options = options;
    this.error = error;
    this.uriPattern = uriPattern;
    this.dispatch = dispatch;
  };

  return Route;

})();

/*
context functions
*/


/*
define a 'get' request handler

Example:
get '/hello/:name', (req, res) ->
	res.render '/greeting.html', {name: req.name}
*/


get = function(uriPattern, callBack) {
	var dispatch = new ArrayList()
	dispatch.add(REQUEST)
	return module.addEnterpriseService(uriPattern, GET, callBack, dispatch);
};

/*
post = (uriPattern, callBack) ->
	module.addRoute uriPattern, new JSHandlerWrapper(callBack), POST

put = (uriPattern, callBack) ->
	module.addRoute uriPattern, new JSHandlerWrapper(callBack), PUT

del = (uriPattern, callBack) -> // TODO is 'delete' a cs/js reserved word
	module.addRoute uriPattern, new JSHandlerWrapper(callBack), DELETE

options = (uriPattern, callBack) ->
	module.addRoute uriPattern, new JSHandlerWrapper(callBack), OPTIONS

route = (uriPattern, callBack) ->
	module.addRoute uriPattern, new JSHandlerWrapper(callBack)

route = (uriPattern) ->
	new Route()

schedule = (cronString, callBack) ->
	module.addScheduledTask cronString, new JSHandlerWrapper callBack
*/