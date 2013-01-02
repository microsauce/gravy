/*******************************************************

This script defines the Gravy JavaScript API

Script bindings:

'gravyModule'  	- the calling module
'log'			- the application logger
'out'			- the console PrintStream
'config'        - a Java Properties object
'util'			- a utility class - IO functions - etc

*******************************************************/

/*
 Java imports
 */

importPackage(javax.servlet.http)
importPackage(javax.servlet)
importPackage(java.util)
importPackage(java.io)

/********************************************************
 * undocumented global variables
 ********************************************************/

/*
 * http methods
 */

var GET  	= 'get'
var POST 	= 'post'
var PUT 	= 'put'
var OPTIONS = 'post'
var DELETE 	= 'delete'

/********************************************************
 * documented global variables
 ********************************************************/
	
/*
 * dispatch types
 */

var REQUEST = DispatcherType.REQUEST
var FORWARD = DispatcherType.FORWARD
var ERROR   = DispatcherType.ERROR
var INCLUDE = DispatcherType.INCLUDE

var addEnterpriseService = function(uriPattern, method, callBack, dispatch) {
	var dispatchList = new ArrayList()
	if ( dispatch == null || dispatch.length == 0 ) {
		dispatchList.add(REQUEST)
		dispatchList.add(FORWARD)
	} else {
		for ( i = 0; i < dispatch.length; i++  ) {
			dispatchList.add(dispatch[i])
		}
	}
	
	return gravyModule.addEnterpriseService(uriPattern, method, callBack, dispatchList)
}

var executeHandler = function(callBack, req, res, paramMap, paramList, objectBinding) {
	var jsHandler = new JSHandler(callBack)
	jsHandler.invokeHandler(req, res, paramMap, paramList, objectBinding)
}

var JSHandler = function(handler) {
	
    this.handler = handler
    
    this.invokeHandler = function(req, res, paramMap, paramList, objectBinding) {

    	// add uri parameters to 'this'
    	if ( paramMap != null ) {
	        var iterator = paramMap.keySet().iterator()
	        while (iterator.hasNext()) {
	            var key = iterator.next()
	            this[key] = paramMap.get(key)
	        }
    	}
    	
        // create the splat array
    	if ( paramList != null ) {
	        var iterator = paramList.iterator()
	        this.splat = []
	        while (iterator.hasNext()) {
	            var next = iterator.next()
	            this.splat.push(next)
	        }
    	}

    	// build the parameter array
    	var params = new Array()
    	if ( req != null && res != null ) {
    		params.push(req)
    		params.push(res)
    	}
    		
    	if ( objectBinding != null ) {
	        var iterator = objectBinding.keySet().iterator()
	        while (iterator.hasNext()) {
	            var key = iterator.next()
	            this[key] = objectBinding.get(key)
	        }
    	}
    	
        this.handler.apply(this, params)
    }
}

/********************************************************
 * documented service functions
 *******************************************************/

/*
 * define a 'get' request handler
 * 
 * Example: 
 * get '/hello/:name', (req, res) -> 
 * 		res.render '/greeting.html', {name: @name}
 * 
 * get '/hello/:name', (req, res) -> 
 * 		res.render '/greeting.html', {name: @name}
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
 * 		res.render '/greeting.html', {name: @name}
 * 
 * post '/hello/:name', (req, res) -> 
 * 		log.info "Hello #{@name}"
 * 		res.render '/greeting.html', {name: @name}
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
 * 		res.render '/greeting.html', {name: @name}
 * 
 * del '/hello/:name', (req, res) -> 
 * 		res.render '/greeting.html', {name: @name}
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
 * 		res.render '/greeting.html', {name: @name}
 * 
 * options '/hello/:name', (req, res) -> 
 * 		res.render '/greeting.html', {name: @name}
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

/*
 * define a catch-all request handler
 * 
 * Example: 
 * route '/hello/:name', (req, res) -> 
 * 		res.render '/greeting.html', {name: this.name}
 * 
 * route '/hello/:name', (req, res) -> 
 * 		res.render '/greeting.html', {name: this.name}
 * , [REQUEST]
 */
var route = function(uriPattern, callBack, dispatch) {
	addEnterpriseService(uriPattern, 'default', callBack, dispatch)
}

/*
 * schedule a handler on a cron timer
 * 
 * Example:
 * schedule '* * * * * ', ->
 * 		println "Another minute bites the dust"
 */
var schedule = function(cronString, callBack) {
	gravyModule.addCronService(cronString, callBack)
}
