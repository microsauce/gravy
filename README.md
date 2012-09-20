
Gravy
===

This README is a work in progress, more to come . . .

Gravy is a dynamic server scripting framework for the Groovy language on the Java enterprise.  Its design focuses on modularity and rapid development.

Servlet 3.0 (the meat-and-potatoes) and Groovy (the Gravy).

	route('/hello/:name') { 
		out << "Hello $name!"
	}

## Getting Started

### Prerequisites

	* Java 7 JDK
	* JAVA_HOME must refer to your Java 7 JDK
	* ant or gradle

### Installation

	$ git clone https://github.com/microsauce/gravy.git
	$ cd gravy
	$ ant
	$ export GRAVY_HOME=$PWD
	$ PATH=$PATH:$GRAVY_HOME/bin

### Your First Application

Create the example app:

	$ gravy create [app-name] example

This command creates the example application in a folder named [app-name].  The example application demonstrates many Gravy features.  The application folder layout is as follows:

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


## Routes

A route is a mapping between a URI pattern and one or more handlers (Closure objects).  You can define a handler for each supported http method (get, head, delete, put, post, and options) or you may define a general purpose handler (Route.handler) to service any and all request methods.  In Java enterprise terms routes are (basically) filters.  They are matched in the order they are defined.  They may issue a response or hand control to the next route in the chain (via chain.doFilter()).

	// http:/hostname/Steve/is/Cool
	// yields: 'Steve is Cool'
	route '/:name/is/*', { adjective ->
		out << "$name is $adjective"
	}

	// GET /hello/Steve
	// yields: 'Hello Steve!'
	route('/hello/:name').with {
		get = {
			out << "Hello $name!"
		}
	}
	
	// GET /hello/Jimmy
	// yields: 'Hello Jimmy!'
	route(~/\/hello\/(.*)/).with {
		get = {
			out << "Hello ${splat[0]}!"
		}
	}

	// http:/hostname/hello/Suzie
	// yields: 'Hello Suzie!'
	route ~/\/hello\/(.*)/, { name ->
		out << "Hello $name!"
	}

### Controllers

Controllers are very similar to routes but there are a few notable differences.  First, controllers handle specific URI's rather than patterns.  Secondly, controllers are ignorant of http method, rather than defining handlers for http methods we define named actions.  A controller URI two parts, the controller name and the action name 

	controller('/friendly/controller', [
		greeting: {
			out << 'hello'
		},
		farewell: {
			out << 'good-bye'
		}
	])

	conroller('/friendly/controller').with {
		greeting = {
			out << 'hello'
		}
		farewell = {
			out << 'good-bye'
		}
	}

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
The Gravy runtime binds the following objects to every route handler and controller action in your application:

	render(String template, Map model)
		 render a view

	renderJson(Map model)
		render a JSON response 

	forward(String uri)
		forward request to the given uri

	redirect(String uri)
		redirect the request to the given uri

	req - [HttpServletRequest](http://docs.oracle.com/javaee/6/api/javax/servlet/http/HttpServletRequest.html) - with additional convenience methods:
		Object attr(String attributeName)
			get the attribute with the given name
		Object attr(String attributeName, Object attributeValue)
			set the attribute of the given name and value
		String parm(String parameterName)
			get the parameter with the given name
		toObject(Class)
			instantiate an object graph for the given class based on the parameters in this request

	res - [HttpServletResponse](http://docs.oracle.com/javaee/6/api/javax/servlet/http/HttpServletResponse.html)

	sess - [HttpSession](http://docs.oracle.com/javaee/6/api/javax/servlet/http/HttpSession.html) - with addtional convenience methods:
		Object attr(String attributeName)
			get the attribute with the given name
		Object attr(String attributeName, Object attributeValue)
			set the attribute of the given name and value

	out - [PrintWriter](http://docs.oracle.com/javase/6/docs/api/java/io/PrintWriter.html)
		this is the HttpServletResponse.getWriter()

	chain - [FilterChain](http://docs.oracle.com/javaee/6/api/javax/servlet/FilterChain.html) (routes only)

### Rendering a View



### Scheduled Tasks

Gravy also provides a way to define scheduled tasks as follows:

	def batchMailer = module('batchMailer')
	schedule('*/5 * * * *') { // cron string
		log.info "sending batch id ${batchMailer.batchId}"
		batchMailer.sendBatch()
	}


### Script Bindings

The following objects are bound to application.groovy, subscripts (the script folder), and module scripts:

	Bound to all scripts:
	route
	controller
	root                    - denotes the root node of the uri hierarchy (aka '/')
	config                  - the groovy ConfigObject 
	log                     - the logger
	run('<script-name>', [optional parameterList])    
	                        - execute a subscript - scripts folder
	REQUEST                 - DispatcherType.REQUEST
	FORWARD                 - DispatcherType.FORWARD
	ERROR                   - DispatcherType.ERROR
	ASYNC					- DispatcherType.ASYNC
	INCLUDE					- DispatcherType.INCLUDE

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