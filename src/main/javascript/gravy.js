/*******************************************************

This script defines the Gravy JavaScript API

Script bindings:

'gravyModule'  	- the calling module
'log'           - the application logger
'out'           - the console PrintStream
'config'        - a Java Properties object

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

global.GET = 'get'
global.POST = 'post'
global.PUT = 'put'
global.DELETE = 'delete'

/********************************************************
 * documented global variables
 ********************************************************/

/*
 * define global scope
 */

global.REQUEST = DispatcherType.REQUEST
global.FORWARD = DispatcherType.FORWARD
global.ERROR = DispatcherType.ERROR
global.INCLUDE = DispatcherType.INCLUDE

global.addEnterpriseService = function(gravyModule, uriPattern, method, callBack, dispatch) {
	var dispatchList = new ArrayList()
	if (dispatch == null || dispatch.length == 0) {
		dispatchList.add(REQUEST)
		dispatchList.add(FORWARD)
	} else {
		for (i = 0; i < dispatch.length; i++) {
			dispatchList.add(dispatch[i])
		}
	}

	return gravyModule.addEnterpriseService(uriPattern, method, callBack, dispatchList)
}

global.executeHandler = function(callBack, req, res, paramMap, paramList,
		objectBinding, parms) {
	var jsHandler = new NativeJSHandler(callBack)
	jsHandler.invokeHandler(req, res, paramMap, paramList, objectBinding, parms)
}

global.NativeJSHandler = function(handler) {

	this.handler = handler

	this.invokeHandler = function(req, res, paramMap, paramList, objectBinding, parms) {
        // TODO cache the binding 'this' in the req

        var binding = req.getAttribute('_js_binding')
        if ( !binding ) {

            // add uri parameters to 'this'
            if (paramMap != null) {
                var iterator = paramMap.keySet().iterator()
                while (iterator.hasNext()) {
                    var key = iterator.next()
                    this[key] = paramMap.get(key)
                }
            }

            // create the splat array
            if (paramList != null) {
                var iterator = paramList.iterator()
                this.splat = []
                while (iterator.hasNext()) {
                    var next = iterator.next()
                    this.splat.push(next)
                }
            }

            // set the form/query properties
            var method = req.getMethod()
            var parameters = new ScriptableMap(parms)
            if (method == 'GET' || method == 'DELETE') {
                this.query = parameters
            } else if (method == 'POST' || method == 'PUT') {
                this.form = parameters
            }

            if (objectBinding != null) {
                var iterator = objectBinding.keySet().iterator()
                while (iterator.hasNext()) {
                    var key = iterator.next()
                    this[key] = objectBinding.get(key)
                }
            }
            req.setAttribute('_js_binding', this)
        }

        // build the parameter array
        var params = new Array() // TODO cache this too ???
        if (req != null && res != null) {
            params.push(req)
            params.push(res)
        }
        var parmListSize = paramList.size()
        for ( var i = 0; i<parmListSize; i++ ) {
            params.push(paramList.get(i))
        }

		this.handler.apply(binding ? binding : this, params)

		res.out.flush()
	}
}

/**
 * undocumented export functions
 */

global.commonObj = function(nativeObj) {
	if (nativeObj != null) {
		return new CommonObject(nativeObj, GravyType.JAVASCRIPT)
	}
	return null
}

/*
 * called by the 'app' module
 */
global.Imports = function(importMap) {
	var exportIterator = importMap.entrySet().iterator();
	while (exportIterator.hasNext()) {
		var keyValue = exportIterator.next();
		var exp = keyValue.getKey();
		(function(expName, imp, handler) {
			imp[expName] = function(parm1, parm2, parm3, parm4, parm5, parm6, parm7) {
				return handler.call(commonObj(parm1), commonObj(parm2),
					commonObj(parm3), commonObj(parm4), commonObj(parm5),
					commonObj(parm6), commonObj(parm7));
			}
		})(exp, this, keyValue.getValue());
	}

}

global.prepareImports = function(allImports, scope) {
	var moduleIterator = allImports.entrySet().iterator()
	while (moduleIterator.hasNext()) {
		var thisModuleExports = moduleIterator.next()
		var moduleName = thisModuleExports.getKey()
		var moduleImports = thisModuleExports.getValue()
		scope[moduleName] = new Imports(moduleImports)
	}
}

global.prepareExports = function(exports) { // service exports
	var preparedExports = new HashMap()
	for (exp in exports) {
		if (Object.prototype.toString.call(exports[exp]) == '[object Function]') {
			var handler = new JSHandler(exports[exp], this)
			preparedExports.put(exp, handler)
		}
	}
	return preparedExports
}

//
// define the gravy API for javascript
//

global.GravyModule = function(j_module, j_config, j_logger) {

    this.gravyModule = j_module
    this.conf = global.initModuleConfig(j_config)
    this.log = j_logger

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
    this.get = function(uriPattern, callBack, dispatch) {
        addEnterpriseService(this.gravyModule, uriPattern, GET, callBack, dispatch)
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
    this.post = function(uriPattern, callBack, dispatch) {
        addEnterpriseService(this.gravyModule, uriPattern, POST, callBack, dispatch)
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
    this.del = function(uriPattern, callBack, dispatch) {
        addEnterpriseService(this.gravyModule, uriPattern, DELETE, callBack, dispatch)
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
    this.put = function(uriPattern, callBack, dispatch) {
        addEnterpriseService(this.gravyModule, uriPattern, PUT, callBack, dispatch)
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
    this.route = function(uriPattern, callBack, dispatch) {
        addEnterpriseService(this.gravyModule, uriPattern, 'default', callBack, dispatch)
    }

    /*
     * alias for 'route' - a la expressjs.
     */
    this.use = function(uriPattern, callBack, dispatch) {
        route(uriPattern, 'default', callBack, dispatch)
    }

    /*
     * schedule a handler on a cron timer
     *
     * Example:
     * schedule '* * * * * ', ->
     * 		println "Another minute bites the dust"
     */
    this.schedule = function(cronString, callBack) {
        this.gravyModule.addCronService(cronString, callBack)
    }
}
