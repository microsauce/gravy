
Servlet 3.0 (the meat and potatoes) & Groovy (the Gravy)
===
***

This README is a work in progress, more to come . . .

Gravy is a framework for developing web applications on Servlet 3.0 compliant web containers.  Taking inspiration from Sinatra and Express.js, Gravy is an attempt to bring dynamic server scripting into the Java space, in particular, war deployments to Servlet 3.0 compliant web containers.

For your infrastructure it's just meat-and-potatoes, but for the developer it's all Gravy.


## Features
***
* Rapid application development
	* Groovy
	* Auto hot code-swapping
* Powerful Sinatra style routing 
	* Regular expressions
	* Embedded uri parameters
* Elegant controller syntax
* Modular design
* Multiple templating options (four and counting):
	* gstring module- an enhanced GStringTemplateEngine
		* layouts
		* internationalization
		* XSS sanitizing
	* freemarker module
	* scalate module 
		* jade
		* mustache
		* scaml
	* JSP/JSTL - the old stand-bye
* Environment based configuration
* Integrated build system

## System Requirements
***
* Development Environment Requirements
	* ant or gradle
	* Java 7 JDK
	* JAVA_HOME must refer to your Java 7 JDK

* Deployment Environment Requirements
	* Java 7 JDK
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

	$ gravy create <app-name> example

This command creates an example application in a folder named <app-name>.  The example application demonstrates many Gravy features.  The application folder layout is as follows:

	<app-name>                   - root application folder
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

	$ gravy

Point your browser to:

	http://localhost:8080

### Sample Code

#### Define Routes

	app.route('/hello/:name') { // http://<your-host>/hello/Steve
		out << "Hello $name!"
	}

	app.route('/order/:id').with { // http://<your-host>/order/1
		get = {                    // http GET method
			// get an order
		}
		delete = {                 // http DELETE method
			// delete an order
		}
		post = {                   // http POST method
			// save or update an order
		}
	}

#### Controller Notation

	app.friendly.controller.with {
		greeting = {  // http://<your-host>/friendly/controller/greeting
			out << 'Hello!'
		}
		farewell = {  // http://<your-host>/friendly/controller/farewell
			out << 'Good-bye :('
		} 
	}


## The Fundementals

### Building Blocks

#### application.groovy

#### modules



### ApplicationContext

The ApplicationContext class is the center-piece of the framework.  Every script, module, java class, and groovy class work together to build a complete service context . . .

app.route(uriPattern)



### Script Bindings

The following objects are bound to application.groovy, subscripts (the script folder), and module scripts:

	Bound to all scripts:
	app                     - the appliction context
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
	app.route('/order/:id', [REQUEST, FORWARD]).with {
		get = {
			log.info "retrieve order id $id"
			def model = orderService.findOrderById(id)
			render('/order/edit.html', model)
		}
		post = {
			log.info "update order id $id"
			def order = req.toObject(Order)
			orderService.update(order)
			render('/order/view/html', [order: order])
		}
		delete = {
			log.warn "unsupported operation"
			forwardMethod('GET')
		}
	}

### Closure (action) Bindings

All route and controller actions are defined as zero argument closures.  The Gravy runtime binds the following objects to the closure delegate prior to its execution.

Routes:

	req - the HttpServletRequest
	res - the HttpServletResponse
	out - HttpServletResponse.writer (PrintWriter)
	chain - the FilterChain

Controllers:

	req - the HttpServletRequest
	res - the HttpServletResponse
	out - the HttpServletResponse.printWriter


## Credits:
* [Groovy](http://groovy.codehaus.org/) - Groovy 2.0.1 script engine
* [Jetty](http://www.eclipse.org/jetty/) - embedded Servlet 3.0 web container (dev mode)
* [cron4j](http://www.sauronsoftware.it/projects/cron4j/) - cron task scheduler
* [JNotify](http://jnotify.sourceforge.net/) - dev-mode source monitoring
* [Freemarker](http://freemarker.sourceforge.net/) - for the freemarker template module
* [Scalate](http://scalate.fusesource.org/) - for the scalate template module