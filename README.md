
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
	* gstring module - an enhanced GStringTemplateEngine
		* layouts
		* internationalization
		* XSS sanitizing
	* freemarker module
	* scalate module 
* Environment based configuration
* Integrated development lifecycle tools
	* clean, compile, test, war

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

This command creates an example application in a folder named <app-name>.  The example application demonstrates many of Gravy features.  The application folder layout is as follows:

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

	$ cd <app-name>
	$ gravy

Point your browser at:

	http://localhost:8080

## The Code

### Routes

Routes can be defined as regular expressions (Pattern objects) or as strings with wildcards (*) and/or named parameters (:parameterName).  You may also define a single optional parameter.

Examples:

	// route with wildcards bound to closure parameters
	route('/fileshare/*.*') { fileName, extension ->
		out << "you have requested $fileName with extension $extension"
	}

	// Pattern route with named grouping
	route(~/\/hello\/(.*)/) { name ->
		out << "Hello $name"
	}

	// route with optional named parameter
	route('/order/?:id?').with {
		get = {
			render('/order/view.html' [order : orderService.findById(id)])
		}
		post = {
			def order = req.toObject(Order)
			orderService.save(order)
			render('/order/listing.html', [listing: orderService.listing(sess['customerNumber'])])
		}
	}

### Controllers

Controllers are static URI mappings

	// Tree Syntax

	def cartService = module('cartService')
	root.my.shopingcart.with {
		listItems = {
			def cart = sess.attr['cart']
			render('list.html', [cart : cart])
		}

		item.with {

			add = {
				def newCartItem = req.toObject(CartItem)
				sess.attr['cart'].addCartItem(newCartItem)
				forward('/my/shoppingcart/listItems')
			}
			delete = {
				def cartItem = req.toObject(CartItem)
				sess.attr['cart'].removeCartItem(cartItem)
				forward('/my/shoppingcart/listItems')
			}
		}
	}

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


For your infrastructure it's still meat-and-potatoes, but for the developer it's all Gravy.

## Credits:
* [Groovy](http://groovy.codehaus.org/) - Groovy 2.0.1 script engine
* [Jetty](http://www.eclipse.org/jetty/) - embedded Servlet 3.0 web container (dev mode)
* [cron4j](http://www.sauronsoftware.it/projects/cron4j/) - cron task scheduler
* [JNotify](http://jnotify.sourceforge.net/) - dev-mode source monitoring
* [Freemarker](http://freemarker.sourceforge.net/) - for the freemarker template module
* [Scalate](http://scalate.fusesource.org/) - for the scalate template module