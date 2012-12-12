/*
This script defines the Gravy JavaScript/CoffeeScript API

Script bindings:

'gravyModule'  	- the calling module
'log'			- the application logger
'out'			- the console PrintStream

 */

/*
 Java imports
 */
importPackage(javax.servlet.http)
importPackage(javax.servlet)
importPackage(java.util)
importPackage(java.io)

/*
 * http methods
 */

var GET = 'get'
var POST = 'post'
var PUT = 'put'
var OPTIONS = 'post'
var DELETE = 'delete'

/*
 * dispatch types
 */

var REQUEST = DispatcherType.REQUEST
var FORWARD = DispatcherType.FORWARD
var ERROR = DispatcherType.ERROR

/*
 * documented utility functions
 */

/*
 * script loader
 */
var load = function(scriptUri) {
	var script = gravyModule.load(scriptUri)
	eval(script)
}

var require = function(scriptUri) {
	load(scriptUri)
}

var println = function(str) {
	return out.println(str)
}

var print = function(str) {
	return out.print(str)
}

/*
 * undocumented utility functions/classes
 */

String.prototype.endsWith = function(suffix) {
    return this.indexOf(suffix, this.length - suffix.length) !== -1
};

/*
 * JSON -> obj
 */
function reviver(key, value) {
	if ( key.endsWith('Date') ) 
		return new Date(intValue(value))
	
	return value
}

/*
 * obj -> JSON
 */
function replacer(key, value) {
	if ( key.endsWith('Date') )
		return value.getTime()
	else return value
}

var addEnterpriseService = function(uriPattern, method, rawCallBack, dispatch) {
	var dispatchList = new ArrayList()

	if ( dispatch == null || dispatch.length == 0 ) {
		dispatchList.add(REQUEST)
		dispatchList.add(FORWARD)
	} else {
		for ( i = 0; i < dispatch.length; i++  ) {
			dispatchList.add(dispatch[i])
		}
	}
	
	return gravyModule.addEnterpriseService(uriPattern, method, new JSHandler(rawCallBack), dispatchList)
}

var JSHandler = function(handler) {
	
    this.handler = handler
    
    this.invokeHandler = function(req, res, paramMap, paramList) {

    	// add uri parameters to 'this'
        var iterator = paramMap.keySet().iterator()
        while (iterator.hasNext()) {
            var key = iterator.next()
            this[key] = paramMap.get(key)
        }

        // create the splat array
        iterator = paramList.iterator()
        this.splat = []
        while (iterator.hasNext()) {
            var next = iterator.next()
            this.splat.push(next)
        }

        this.handler.apply(this, [req, res])
    }
}

/*
 * public Gravy API
 */

/*
 * define a 'get' request handler
 * 
 * Example: 
 * get '/hello/:name', (req, res) -> 
 * 		res.render '/greeting.html', {name: this.name}
 * 
 * get '/hello/:name', (req, res) -> 
 * 		res.render '/greeting.html', {name: this.name}
 * , [REQUEST]
 */
var get = function(uriPattern, callBack, dispatch) {
	addEnterpriseService(uriPattern, GET, callBack, dispatch)
}

/*
 * define a 'post' request handler
 * 
 * Example: 
 * post '/hello/:name', (req, res) -> 
 * 		res.render '/greeting.html', {name: this.name}
 * 
 * post '/hello/:name', (req, res) -> 
 * 		log.info "Hello #{@name}"
 * 		res.render '/greeting.html', {name: this.name}
 * , [REQUEST]
 */
var post = function(uriPattern, callBack, dispatch) {
	addEnterpriseService(uriPattern, POST, callBack, dispatch)
}

/*
 * define a 'del' request handler
 * 
 * Example: 
 * del '/hello/:name', (req, res) -> 
 * 		res.render '/greeting.html', {name: this.name}
 * 
 * del '/hello/:name', (req, res) -> 
 * 		res.render '/greeting.html', {name: this.name}
 * , [REQUEST]
 */
var del = function(uriPattern, callBack, dispatch) {
	addEnterpriseService(uriPattern, DELETE, callBack, dispatch)
}

/*
 * define a 'options' request handler
 * 
 * Example: 
 * options '/hello/:name', (req, res) -> 
 * 		res.render '/greeting.html', {name: this.name}
 * 
 * options '/hello/:name', (req, res) -> 
 * 		res.render '/greeting.html', {name: this.name}
 * , [REQUEST]
 */
var options = function(uriPattern, callBack, dispatch) {
	addEnterpriseService(uriPattern, OPTIONS, callBack, dispatch)
}

/*
 * define a 'put' request handler
 * 
 * Example: 
 * put '/hello/:name', (req, res) -> 
 * 		res.render '/greeting.html', {name: this.name}
 * 
 * put '/hello/:name', (req, res) -> 
 * 		res.render '/greeting.html', {name: this.name}
 * , [REQUEST]
 */
var put = function(uriPattern, callBack, dispatch) {
	addEnterpriseService(uriPattern, PUT, callBack, dispatch)
}


