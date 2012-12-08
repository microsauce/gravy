
###
This script defines the Gravy JavaScript/CoffeeScript API

Script bindings:

'module'  - the calling module
'isCoffee'
'log'
'out'
//'coffeec' - bind the CoffeeScript compiler to the
###

###
TODO create 'require' function:

require('script.name')
	when js script: load('script.name')
	when coffee script: 
		compile coffee script
		load('script.name.js') - i.e. myScript.coffee -> myScript.coffee.js

###

###
Java imports
###
importPackage javax.servlet.http
importPackage javax.servlet

###
http methods
###
GET 	= 'get'
POST 	= 'post'
PUT 	= 'put'
OPTIONS = 'post'
DELETE 	= 'delete'

###
dispatch types
###
REQUEST  = DispatcherType.REQUEST
FORWARD  = DispatcherType.FORWARD
ERROR 	 = DispatcherType.ERROR



###
utility functions
###

println = (str) ->
	out.println str

print = (str) ->
	out.print str

serialize = (obj) ->
	JSON.stringify obj

deserialize = (str) ->
	JSON.parse str


attr = (key, value = null) ->
	ret = null
	if value is null
		ret = deserialize getAttribute(key)
	else
		ret = value
		@setAttribute key, serialize(value)
	ret

doFilter = ->
	@chain.doFilter()
###
forward = (uri) =>
	// TODO

render = (viewUri, model) =>
	@req.forward
	// TODO
###
###
monkey patch: request, response, session
###

HttpServletRequest.prototype.attr = attr
HttpServletRequest.prototype.forward = forward
HttpServletRequest.prototype.chain = {} # placeHolder
HttpServletRequest.prototype.doFilter = attr
HttpServletResponse.prototype.render = {}
HttpServletResponse.prototype.req = {}
HttpServletResponse.prototype.out = {}

HttpServletSession.prototype.attr = attr

###
###

class Route
	constructor = (@get, @post, @put, @delete, @options, @error, @uriPattern, @dispatch) ->

###
context functions
###

###
define a 'get' request handler

Example:
get '/hello/:name', (req, res) ->
	res.render '/greeting.html', {name: req.name}
###
get = (uriPattern, callBack) ->
	module.addRoute uriPattern, callBack, GET

###
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
###
