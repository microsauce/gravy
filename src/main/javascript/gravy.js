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
importPackage(java.lang)
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

global.addEnterpriseService = function() {
    var args = Array.prototype.slice.call(arguments, 0)

    if ( args.length < 4 ) throw new Error('ERROR: Invalid arguments: ' + method)
    var gravyModule = args[0]
    var method = args[1]
    var uriPattern = args[2]
    if (typeof(uriPattern) !== 'string' && !(uriPattern instanceof String))
        throw new Error('ERROR: ' + method + ': first parameter must be string')
    var handlers = args.slice(3,args.length)
    var middleware = new ArrayList()
    var middlewareSlice = handlers.slice(0,(handlers.length-1))
    for ( var j = 0; j < middlewareSlice.length; j++ ) {
        middleware.add(middlewareSlice[j])
    }

    return gravyModule.addEnterpriseService(uriPattern,method,middleware,handlers[handlers.length-1])
}

global.addParameterPrecondition = function() {
    var args = Array.prototype.slice.call(arguments, 0)
    if ( args.length < 3 ) throw new Error('ERROR: Invalid arguments: ' + method)
    var gravyModule = args[0]
    var paramName = args[1]
    var callback = args[2]

    return gravyModule.addParameterPrecondition(paramName, callback)
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
        sess = new ScriptableMap(new JSSessObject(servletFacade));
        nativeSess.setAttribute('_js_session', sess);
        sess.__noSuchMethod__ = function(name, args) {
            nativeSess[name].apply(nativeSess, args);
        }
    }
    return sess;
}
global.newJSRequest = function(servletFacade) {
    var req = new ScriptableMap(new JSReqObject(servletFacade))
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
            this.facade.redirect(url);
        }
        this.renderJson = function(model) {
            this.facade.renderJson(model);
        }
        this.render = function(viewUri, model) {
            this.facade.render(viewUri,model);
        }
        this.out = servletFacade.getOut();
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
            var method = req.method //req.getMethod()
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
        params.push(jsFacade.req)
        params.push(jsFacade.res)
        params.concat(jsFacade.req.splat)
		this.handler.apply(jsFacade.binding, params)

		jsFacade.res.out.flush()
	}
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
    this.get = function() {
        var args = Array.prototype.slice.call(arguments, 0)
        args.unshift(GET)
        args.unshift(this.gravyModule)
        addEnterpriseService.apply(null, args)
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
    this.post = function() {
        var args = Array.prototype.slice.call(arguments, 0)
        args.unshift(POST)
        args.unshift(this.gravyModule)
        addEnterpriseService.apply(null, args)
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
     */
    this.del = function() {
        var args = Array.prototype.slice.call(arguments, 0)
        args.unshift(DELETE)
        args.unshift(this.gravyModule)
        addEnterpriseService.apply(null, args)
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
    this.put = function() {
        var args = Array.prototype.slice.call(arguments, 0)
        args.unshift(PUT)
        args.unshift(this.gravyModule)
        addEnterpriseService.apply(null, args)
    }

    /*
     * define 'get', 'post', 'del', and 'put' request handlers
     *
     * Example:
     * all '/hello/:name', (req, res) ->
     * 		res.render '/greeting.html', {name: this.name}
     *
     * all '/hello/:name', (req, res) ->
     * 		res.render '/greeting.html', {name: this.name}
     * , [REQUEST]
     */
    this.all = function() {
        var args = Array.prototype.slice.call(arguments, 0)
        this.get(args)
        this.post(args)
        this.put(args)
        this.del(args)
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
    this.route = function() {
        var args = Array.prototype.slice.call(arguments, 0)
        if ( args.length < 2 && typeof(args[0]) !== 'string' && !(args[0] instanceof String) )
            args.unshift('/*')
        args.unshift('default')
        args.unshift(this.gravyModule)

        addEnterpriseService.apply(null, args)
    }

    /*
     * define a parameter precondition
     *
     * Example:
     * param '/hello/:name', (req, res) ->
     * 		res.render '/greeting.html', {name: this.name}
     *
     * param 'name', (req, res) ->
     * 		res.render '/greeting.html', {name: this.name}
     * , [REQUEST]
     */
    this.param = function() {
        var args = Array.prototype.slice.call(arguments, 0)
        args.unshift(this.gravyModule)
        addParameterPrecondition.apply(null, args)
    }

    /*
     * alias for 'route' - a la expressjs.
     */
    this.use = function() {
        var args = Array.prototype.slice.call(arguments, 0)
        if ( args.length < 2 && args.length > 0 && typeof(args[0]) !== 'string' && !(args[0] instanceof String) )
            args.unshift('/*')
        args.unshift('default')
        args.unshift(this.gravyModule)

        addEnterpriseService.apply(null, args)
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
