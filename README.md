
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

3. Set JAVA_HOME environment variable (it must reference a Java 7 JDK)

## Quick Start
***

Create your app:

	$ gravy create-app <app-name>

### App Layout

	<app-name>                   - root application folder
	    |- application.groovy    - main application script
	    |_ src                   - java and groovy sources   
	    |   |_ main
	    |   |   |- java
	    |   |   |- groovy
	    |   |   |_ resources     - additional resources to add to the application 
	    |   |                      classpath
	    |   |_ test              - test scripts
	    |- view                  - view templates
	    |- conf
	    |- webroot               - static resources and JSPs
	    |     |_ WEB-INF         
	    |     |     |_ web.xml   - the application deployment descriptor, for most
	    |     |                    Gravy apps this file can be ignored
	    |     |- img
	    |     |- css
	    |     |_ js 
	    |- scripts               - subscripts called by application.groovy              
	    |- modules               - application modules 
	    |_ lib                   - application jar files

### Script Bindings

In scope in application.groovy, subscripts (the script folder), and module scripts:

	app - the appliction context
	config - the groovy ConfigObject 
	log - the logger
	REQUEST -
	FORWARD -
	ERROR - 

### Closure (action) Bindings

All route and controller actions are defined as zero argument closures.  The Gravy runtime binds the following objects to the closure delegate prior to its execution.

Routes:

	req - the HttpServletRequest
	res - the HttpServletResponse
	out - the HttpServletResponse.printWriter
	chain - the FilterChain

Controllers:

	req - the HttpServletRequest
	res - the HttpServletResponse
	out - the HttpServletResponse.printWriter

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

## Credits:
* [Groovy](http://groovy.codehaus.org/) - Groovy 2.0.1 script engine
* [Jetty](http://www.eclipse.org/jetty/) - embedded Servlet 3.0 web container (dev mode)
* [cron4j](http://www.sauronsoftware.it/projects/cron4j/) - cron task scheduler
* [JNotify](http://jnotify.sourceforge.net/) - dev-mode source monitoring
* [Freemarker](http://freemarker.sourceforge.net/) - for the freemarker template module
* [Scalate](http://scalate.fusesource.org/) - for the scalate template module