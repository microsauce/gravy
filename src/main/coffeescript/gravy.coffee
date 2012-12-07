
/**
*	This script defines the Gravy JavaScript/CoffeeScript API
*
*	Script bindings:
*
*	'module' - the calling module
*/

//
// Java imports
//
importPackage javax.servlet.http
importPackage javax.servlet

//
// http methods
//
GET 	= 'get'
POST 	= 'post'
PUT 	= 'put'
OPTIONS = 'post'
DELETE 	= 'delete'

//
// dispatch types
//
REQUEST  = DispatcherType.REQUEST
FORWARD  = DispatcherType.FORWARD
ERROR 	 = DispatcherType.ERROR
REDIRECT = DispatcherType.REDIRECT


//
// utility functions
//

serialize = (obj) ->
	JSON.stringify obj

deserialize = (str) ->
	JSON.parse str


attr = (key, value = null) =>
	ret = null
	if value is null
		ret = deserialize getAttribute(key)
	else
		ret = value
		@setAttribute key, serialize(value)
	ret

doFilter = =>
	@chain.doFilter()

forward = (uri) =>
	// TODO

render = (viewUri, model) =>
	@req.forward
	// TODO

//
// monkey patch: request, response, session
//

HttpServletRequest.prototype.attr = attr
HttpServletRequest.prototype.forward = forward
HttpServletRequest.prototype.chain = {} // placeHolder
HttpServletRequest.prototype.doFilter = attr
HttpServletResponse.prototype.render = {}
HttpServletResponse.prototype.req = {}

HttpServletSession.prototype.attr = attr

//
//
//

class JSHandlerWrapper

	constructor = (@handlerCallback) ->
	
	call = (req, res, chain) =>
		req.chain = chain
		@handlerCallback req, res

//
// context functions
//

/**
* define a 'get' request handler
*
* Example:
* get '/hello/:name', (req, res) ->
*	 res.render '/greeting.html', {name: req.name}
*/
get = (uriPattern, callBack) ->
	module.addRoute uriPattern, new JSHandlerWrapper(callBack), GET

post = (uriPattern, callBack) ->
	module.addRoute uriPattern, new JSHandlerWrapper(callBack), POST

put = (uriPattern, callBack) ->
	module.addRoute uriPattern, new JSHandlerWrapper(callBack), PUT

delete = (uriPattern, callBack) -> // TODO is 'delete' a cs/js reserved word
	module.addRoute uriPattern, new JSHandlerWrapper(callBack), DELETE

options = (uriPattern, callBack) ->
	module.addRoute uriPattern, new JSHandlerWrapper(callBack), OPTIONS

route = (uriPattern, callBack) ->
	module.addRoute uriPattern, new JSHandlerWrapper(callBack)

schedule = (cronString, callBack) ->
	module.addScheduledTask cronString, new JSHandlerWrapper callBack
