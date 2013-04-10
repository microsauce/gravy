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
importPackage(org.microsauce.gravy.lang.javascript)

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

global.getJSSession = function(servletFacade) {
    var nativeSess = servletFacade.nativeReq.session;
    var sess = nativeSess.getAttribute('_js_session');
    if (!sess) {
        sess = new ScriptableMap(new JSSessObject(nativeSess));
        nativeSess.setAttribute('_js_session', sess);
        sess.__noSuchMethod__ = function(name, args) {
            nativeSess[name].apply(nativeSess, args);
        }
    }
    return sess;
}
global.newJSRequest = function(servletFacade) {
    var req = new ScriptableMap(new JSReqObject(servletFacade.nativeReq))
    req.initialize = function(servletFacade) {
        this.facade = servletFacade  // TODO i can probably due w/o this
        this.__noSuchMethod__ = function (name, args) {
            return this.facade.nativeReq[name].apply(this.facade.nativeReq, args)
        }
        this.next = function() {
            this.facade.next();
        }
        this.forward = function(uri) {
            this.facade.forward(uri);
        }
        this.input = servletFacade.getInput();
        this.session = getJSSession(servletFacade);
        this.json = servletFacade.getJson();
        return this
    }
    return req.initialize(servletFacade);
}
global.newJSResponse = function(servletFacade) {
    var res = new ScriptableMap(new JSResObject(servletFacade.nativeRes))
    res.initialize = function(servletFacade) {
        this.facade = servletFacade   // TODO i can probably due w/o this
        this.__noSuchMethod__ = function(name, args) {
            this.facade.nativeRes[name].apply(this.facade.nativeRes, args)
        }
        this.print = function(str) {
            this.facade.print(str);
        }
        this.write = function(bytes) {
            this.facade.write(bytes);
        }
        this.redirect = function(url) {
            this.facade.redirect(url)
        }
        this.renderJson = function(model) {
            this.facade.renderJson(model)
        }
        this.render = function(viewUri, model) {
            this.facade.render(viewUri,model)
        }
        this.out = servletFacade.getOut()
        return this;
    }
    return res.initialize(servletFacade);
}

global.NativeJSHandler = function(handler) {

	this.handler = handler

	this.invokeHandler = function(servletFacade) {
        var jsFacade = servletFacade.nativeReq.getAttribute('_js_facade')
        if ( !jsFacade ) {

            var req = newJSRequest(servletFacade)
            var res = newJSResponse(servletFacade)

            // add uri parameters to 'this'
            var params = {}
            if (servletFacade.uriParamMap != null) {
                var iterator = servletFacade.uriParamMap.keySet().iterator()
                while (iterator.hasNext()) {
                    var key = iterator.next()
                    var value = servletFacade.uriParamMap.get(key)
                    params[key] = value
                    this[key] = value
                }
            }
            req.params = params

            // create the splat array
            var splat
            if (servletFacade.splat != null) {
                splat = new ScriptableList(servletFacade.splat)
            }
            req.splat = splat

            // set the form/query properties
            var method = req.getMethod()
            var parameters = new ScriptableMap(servletFacade.requestParams)
            if (method == 'GET' || method == 'DELETE') {
                req.query = parameters
                this.query = parameters
            } else if (method == 'POST' || method == 'PUT') {
                req.form = parameters
                this.form = parameters
            }

            jsFacade = {req: req, res: res, binding: this}
            servletFacade.nativeReq.setAttribute('_js_facade', jsFacade)
        }

        // build the parameter array
        var params = new Array()
        if (req != null && res != null) {
            params.push(jsFacade.req)
            params.push(jsFacade.res)
        }
        params.concat(jsFacade.req.splat)
		this.handler.apply(jsFacade.binding, params)

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
