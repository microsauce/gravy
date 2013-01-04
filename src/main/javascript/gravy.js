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
importPackage(org.microsauce.gravy.context.javascript)
importPackage(org.microsauce.gravy.lang.object)

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

/**
 * undocumented export functions
 */

var commonObj = function(nativeObj) {
	if ( nativeObj != null ) { 
		return new CommonObject(nativeObj, GravyType.JAVASCRIPT)
	} return null
}

/*
 * called by the 'app' module
 */
var prepareImports = function(allImports, scope) {
	var iterator = allImports.entrySet().iterator()
	while ( iterator.hasNext() ) {
		var thisModuleExports = iterator.next()
		var moduleName = thisModuleExports.getKey()
		var moduleExports = thisModuleExports.getValue()
		var thisExportIterator = moduleExports.entrySet().iterator()
		while ( thisExportIterator.hasNext() ) {
			var keyValue = thisExportIterator.next()
			var exp = keyValue.getKey()
			var thisHandler = keyValue.getValue()
			if ( scope[moduleName] == null ) scope[moduleName] = new Object()
			scope[moduleName][exp] = function(parm1,parm2,parm3,parm4,parm5,parm6,parm7) {
				return thisHandler.call(
					commonObj(parm1),
					commonObj(parm2),
					commonObj(parm3),
					commonObj(parm4),
					commonObj(parm5),
					commonObj(parm6),
					commonObj(parm7)
				)
			}
		}
	}
}

var prepareExports = function(exports) { 
	var preparedExports = new HashMap()
	for (exp in exports) {
		if ( typeof(exp) == 'function' ) {
			var handler = new JSHandler(exports[exp], this) // TODO verify 'this'
			preparedExports.put(exp, handler)
		}
	}
	return preparedExports
}
