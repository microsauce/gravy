
Servlet 3.0 (the meat and potatoes) & Groovy (the Gravy)
===

	route('/hello/:name') { 
		out << "Hello $name!"
	}

This README is a work in progress, more to come . . .

Gravy is a framework for rapid development of web applications on Servlet 3.0 compliant web containers.  Taking inspiration from Sinatra and Express.js, Gravy is an effort to bring dynamic server scripting into the Java space, in particular, the Java enterprise web container.

## Features
***
* Rapid application development
* Sinatra style routing 
	* Regular expressions
	* named, wildcard, and optional uri parameters
* Elegant controller syntax
* Modules
* Templates (three template modules and counting):
	* gstring module - an enhanced GStringTemplateEngine supporting:
		* layouts
		* internationalization
		* XSS sanitizing
	* freemarker module
	* scalate module 
* Environment based configuration
* Integrated development lifecycle tools for:
	* building
	* testing
	* packaging

## System Requirements
***
* Development Environment Requirements
	* ant or gradle
	* Java 7 JDK
	* JAVA_HOME must refer to your Java 7 JDK

* Deployment Environment Requirements
	* Java 7 JRE
	* a Servlet 3.0 compliant web container

## Building/Installing the Gravy Development Environment
***

1.	Build Gravy:

	$ ant jar - or - $ gradle jar

2. Set GRAVY_HOME environment variable

3. Add $GRAVY_HOME/bin to your PATH

4. Set JAVA_HOME environment variable (it must reference a Java 7 JDK)


## Quick Start
***

Create an example app:

	$ gravy create [app-name] example

This command creates an example application in a folder named [app-name].  The example application demonstrates many of Gravy features.  The application folder layout is as follows:

	[app-name]                   - root application folder
	    |- application.groovy    - main application script
	    |_ src                   - java and groovy sources   
	    |   |_ main
	    |   |   |- java
	    |   |   |- groovy
	    |   |   |_ resources     - additional resources to add to the application 
	    |   |                      classpath
	    |   |_ test 
	    |       |_ groovy        - test scripts
	    |- view                  - view templates
	    |- conf                  - configuration (config.groovy)
	    |- webroot               - static resources and JSPs
	    |   |_ WEB-INF         
	    |   |     |_ web.xml     - the application deployment descriptor, for most
	    |   |                      Gravy apps there is no need to edit this file
	    |   |- img
	    |   |- css
	    |   |_ js 
	    |
	    |- scripts               - subscripts called by application.groovy
	    |- modules               - application modules 
	    |_ lib                   - application jar files


To run your new Gravy app execute the gravy command:

	$ cd <app-name>
	$ gravy

Point your browser at:

	http://localhost:8080

## The Code

## URI Handlers

### Routes

A route is mapping between a URI pattern and one or more handlers (Closure objects).  You can define a handler for each supported http method (get, head, delete, put, post, and options), you may also define a general purpose handler (Route.handler) to service any and all request methods.

To create a Route use one of the following methods in your application and/or module scripts:

#### Route route(String, Closure)
Create a route with a catch-all handler
Example:

	route('/order/*') { id ->
		out << "order $id"
	}

#### Route route(String)
Example:

	route('/hello/:name').with {
		get = {
			out << "Hellow $name!"
		}
	}
	
#### Route route(Pattern, Closure)
Example:

	route(~/\/hello\/(.*)/) { name ->
		out << "Hello $name!"
	}

#### Route route(Pattern)
Example:

	route(~/\/hello\/(.*)/).with {
		get = {
			out << "Hello ${splat[0]}!"
		}
	}

### Controllers

Controllers are very similar to routes but there are a few notable differences.  First, controllers handle specific URI's rather than patterns.  Second, controllers are ignorant of http method.  Rather than defining handlers for http methods we define actions.  Finally, because we are handling specific URI's we by convention presume that any view rendered by your actions are located in a folder by the same name as the controller.  For example, for the given URI:

	/friendly/controller/greeting
	
The gravy runtime understands '/fiendly/controller' to be the controller name and 'greeting' to be the action name.

To define a controller use one of the following methods:

#### Controller controller(String, Map<String, Closure>)
Example:

	controller('/friendly/controller', [
		greeting: {
			out << 'hello'
		},
		farewell: {
			out << 'good-bye'
		}
	])

#### Map controller(String)
This signature returns the map of controller actions.
Example:

	conroller('/friendly/controller').with {
		greeting = {
			out << 'hello'
		}
		farewell = {
			out << 'good-bye'
		}
	}

#### Tree-Notation
You may also define controllers using a tree-like syntax.  In the following example 'root' denotes the root node of your server URI hierarchy (a.k.a '/').

	root.friendly.controller.with {
		greeting = {  		// http://my-host/friendly/controller/greeting
			out << 'hello'
		}
		farewell = {
			out << 'good-bye'
		}
		in.Spanish.with { 
			greeting = { 	// http://my-host/friendly/controller/in/Spanish/greeting
				out << 'hola'
			}
			farewell = {
				out << 'adios'
			}
		}
	}

### Handler Bindings
The following objects are bound to every route and controller Closure in your application:
	* render(String template, Map model)
		 * render a view
	* renderJson(Map model)
		* render a JSON response to the client (contentType='application/json')
	* forward(String uri)
		* forward request to the given uri
	* redirect(String uri)
		redirect the request to the given uri
	* req - HttpServletRequest
		* Object attr(String attributeName)
			* get the attribute with the given name
		* Object attr(String attributeName, Object attributeValue)
			* set the attribute of the given name and value
		* String parm(String parameterName)
			* get the parameter with the given name
		* toObject(Class)
			* instantiate an object graph for the given class based on the parameters in this request
	* res - HttpServletResponse
	* sess - HttpSession
		* Object attr(String attributeName)
		* Object attr(String attributeName, Object attributeValue)
	* out - PrintWriter
		* this is the HttpServletResponse.getWriter()
	* chain - FilterChain (routes only)

### Scheduled Tasks

Gravy also provides a way to define scheduled tasks as follows:

	def batchMailer = module('batchMailer')
	schedule('*/5 * * * *') {
		log.info "sending batch id ${batchMailer.batchId}"
		batchMailer.sendBatch()
	}

### Script Environment

The Gravy runtime 

### Closure Bindings



### Rendering a View

### Building Blocks

#### application.groovy

For most gravy applications this file is the .. 

#### Modules

Modules . . .

#### Sub-scripts

Both applications and modules may invoke sub-scripts.  Sub-scripts are located in the 'scripts' folder and can be invoked by name, as follows:

	run('myScript')
	// or
	run('myScript', [arg1:val1, arg2:val2 . . .])

The run command is overloaded allowing the calling script to pass in an optional map of values which are bound to the sub-script by key name.


### Script Bindings

The following objects are bound to application.groovy, subscripts (the script folder), and module scripts:

	Bound to all scripts:
	root                    - denotes the root node of the uri hierarchy (aka '/')
	config                  - the groovy ConfigObject 
	log                     - the logger
	REQUEST                 - DispatcherType.REQUEST
	FORWARD                 - DispatcherType.FORWARD
	ERROR                   - DispatcherType.ERROR
	run('<script-name>', [optional parameterList])    
	                        - execute a subscript - scripts folder

	Exclusive to application.groovy:
	module('mod-name')      - load a module
 

For example:

	def orderService = module('orderService')
	. . .
	route('/order/:id').with {
		dispatch = [REQUEST, FORWARD]
		get = {
			log.info "retrieve order id $id"
			def model = orderService.findOrderById(id)
			render('/order/edit.html', model)
		}
		post = {
			log.info "update order id $id"
			def order = req.toObject(Order)
			orderService.update(order)
			render('/order/view.html', [order: order])
		}
		delete = {
			log.warn "unsupported operation"
			out << "DELETE is FORBIDDEN"
		}
	}

### Closure (action) Bindings

The Gravy runtime binds the following objects to the closure delegate prior to its execution.

Routes:

	req - the HttpServletRequest
	res - the HttpServletResponse
	out - HttpServletResponse.writer (PrintWriter)
	chain - the FilterChain

Controllers:

	req - the HttpServletRequest
	res - the HttpServletResponse
	out - the HttpServletResponse.printWriter
	render
	forward

## Development Mode

When running your application in development mode (via the gravy command) 

## War Mode

To bundle your Gravy app for deployment to your web container run the following command:

	$ gravy war

The war-ed application is organized as follows:

	[app-name].war                     - webroot
        |
	    |- WEB-INF         
	    |     |- application.groovy    - main application script
	    |     |- view                  - view templates
	    |     |- conf                  - configuration (config.groovy)
	    |     |- scripts               - subscripts called by application.groovy
	    |     |- modules               - application modules 
	    |     |- lib                   - application jar files
	    |     |_ web.xml     
	    |                    
	    |- img
	    |- css
	    |_ js 



For your infrastructure it's still meat-and-potatoes, but for your developers it's all Gravy.

## Credits:
* [Groovy](http://groovy.codehaus.org/) - Groovy 2.0.1 script engine
* [Jetty](http://www.eclipse.org/jetty/) - embedded Servlet 3.0 web container (dev mode)
* [cron4j](http://www.sauronsoftware.it/projects/cron4j/) - cron task scheduler
* [JNotify](http://jnotify.sourceforge.net/) - dev-mode source monitoring
* [Freemarker](http://freemarker.sourceforge.net/) - for the freemarker template module
* [Scalate](http://scalate.fusesource.org/) - for the scalate template module