
Gravy
===

This README is a work in progress, more to come . . .

Gravy is a dynamic server scripting framework for the Groovy language on the Java enterprise.  Its design focuses on modularity and rapid development.

The Java Enterprise (the meat-and-potatoes) and Groovy (the Gravy).

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

A Gravy application can be as simple as a single script (application.groovy), but most Gravy applications will also employ modules, views (templates), static content (images, css, js), Java and Groovy sources, and custom configuration.  To create an example application which demonstrates each of these additional conponents run the following command:

	$ gravy create [app-name] example

This command creates the example application in a folder named [app-name].  The application folder layout is as follows:

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
	    |- scripts               - sub-scripts called by application.groovy
	    |- modules               - application modules 
	    |_ lib                   - application jar files


To run your new Gravy app execute the gravy commands:

	$ cd <app-name>
	$ gravy

Point your browser at:

	http://localhost:8080

Gravy is a rapid development environment.  All source files are automatically re-compiled and re-deployed upon modification.  Make a change, refresh your browser, and benefit from the instant feedback.

## Pour it On

### Routes

A route is a mapping between a URI pattern and one or more handlers (Closure objects).  URI patterns can be defined as strings with wildcards and named parameters (:parmName) or as regular expressions.  You can define a handler for each supported http method (get, head, delete, put, post, and options) or you may define a general purpose handler (Route.handler) to service any and all request methods.  In Java enterprise terms routes are (basically) filters.  They are matched in the order they are defined.  They may issue a response or hand control to the next Route in the chain (via chain.doFilter()).

Examples:

Named URI parameter and wildcard pattern assigned to closure parameter:

	// http:/hostname/Steve/is/Cool
	// yields: 'Steve is Cool'
	route '/:name/is/*', { adjective ->
		out << "$name is $adjective"
	}

Named URI parameter:

	// GET /hello/Steve
	// yields: 'Hello Steve!'
	route('/hello/:name').with {
		get = {
			out << "Hello $name!"
		}
	}
	
Regular expression with anonymous grouping accessable via 'splat':

	// GET /hello/Jimmy
	// yields: 'Hello Jimmy!'
	route(~/\/hello\/(.*)/).with {
		get = {
			out << "Hello ${splat[0]}!"
		}
	}

Regular expression with grouping pattern assigned to closure parameter 'name':

	// http:/hostname/hello/Suzie
	// yields: 'Hello Suzie!'
	route ~/\/hello\/(.*)/, { name ->
		out << "Hello $name!"
	}

### Controllers

Controllers are very similar to routes but there are a few notable differences.  First, controllers handle specific URI's rather than patterns.  Secondly, controllers are ignorant of http method, rather than defining handlers for http methods we define named actions.  

A controller URI has two parts, the controller name and the action name. The action name is the right most node of the URI path, the controller name is everything to the left of the action name.  For example, given the following URI:

	/friendly/controller/greeting

The controller name is '/friendly/controller', and the action name is 'greeting'.  This tidbit will be useful for our discussion of views below.

Examples:

Create a controller by setting the action mapping:

	controller('/friendly/controller', [
		greeting: {
			out << 'hello'
		},
		farewell: {
			out << 'good-bye'
		}
	])

Create a contoller returning the action mapping and initializing via Groovy 'with' method:

	conroller('/friendly/controller').with {
		greeting = {
			out << 'hello'
		}
		farewell = {
			out << 'good-bye'
		}
	}

You can also define a controller by root-branch notation as follows:

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

<table>
	<tr>
		<td><b>render</b></td>
		<td><b>void render(String template, Map model)</b></td>
	</tr>
	<tr>
		<td></td>
		<td>Render a view</td>
	</tr>
	<tr>
		<td colspan='2'>Example:<br/><pre>render('/order/edit.html', [order: myOrder])</pre></td>
	</tr>
	<tr>
		<td><b>renderJson</b></td>
		<td><b>void renderJson(Map model)</b></td>
	</tr>
	<tr>
		<td></td>
		<td>Render a JSON response</td>
	</tr>
	<tr>
		<td colspan='2'>Example:<br/>
			<pre>renderJson([order: myOrder])</pre></td>
	</tr>
	<tr>
		<td><b>forward</b></td>
		<td><b>void forward(String uri)</b></td>
	</tr>
	<tr>
		<td></td>
		<td>Forward request to the given uri</td>
	</tr>
	<tr>
		<td colspan='2'>Example:<br/><pre>forward('/order/listing')</pre></td>
	</tr>
	<tr>
		<td><b>redirect</b></td>
		<td><b>void redirect(String uri)</b></td>
	</tr>
	<tr>
		<td></td>
		<td>Redirect the request to the given uri</td>
	</tr>
	<tr>
		<td colspan='2'>Example:<br/><pre>redirect('http://anotherUrl')</pre></td>
	</tr>
	<tr>
		<td><b>req</b></td>
		<td><a href='http://docs.oracle.com/javaee/6/api/javax/servlet/http/HttpServletRequest.html'>HttpServletRequest</a></td>
	</tr>
	<tr>
		<td></td>
		<td>
			The request object.  The Gravy runtime adds a few convenience methods to the class:<br/>
	<pre>
	Object attr(String attributeName)
		Get the attribute with the given name
	Object attr(String attributeName, Object attributeValue)
		Set the attribute of the given name and value
	String parm(String parameterName)
		Get the parameter with the given name
	T toObject(Class<T>)
		Instantiate an object graph for the given class based on the parameters in this request
	</pre>
		</td>
	</tr>
	<tr>
		<td colspan='2'>Example:<br/><pre>req.attr('userId')</pre></td>
	</tr>
	<tr>
		<td><b>res</b></td>
		<td><a href='http://docs.oracle.com/javaee/6/api/javax/servlet/http/HttpServletResponse.html'>HttpServletResponse</a></td>
	</tr>
	<tr>
		<td></td>
		<td>The response object</td>
	</tr>
	<tr>
		<td colspan='2'>Example:<br/><pre>res.contentType = 'text/plain'</pre></td>
	</tr>
	<tr>
		<td><b>sess/b></td>
		<td><a href='http://docs.oracle.com/javaee/6/api/javax/servlet/http/HttpSession.html'>HttpSession</a></td>
	</tr>
	<tr>
		<td></td>
		<td>
			The session object.  The Gravy runtime adds two convenience methods to the class:<br/>
	<pre>
	Object attr(String attributeName)
		Get the attribute with the given name
	Object attr(String attributeName, Object attributeValue)
		Set the attribute of the given name and value
	</pre>
		</td>
	</tr>
	<tr>
		<td colspan='2'>Example:<br/><pre>sess.attr('userId', 17)</pre></td>
	</tr>
	<tr>
		<td><b>out</b></td>
		<td><a href='http://docs.oracle.com/javase/6/docs/api/java/io/PrintWriter.html'>PrintWriter</a></td>
	</tr>
	<tr>
		<td></td>
		<td>The response writer</td>
	</tr>
	<tr>
		<td colspan='2'>Example:<br/><pre>out &lt;&lt; 'Hello World!'</pre></td>
	</tr>
	<tr>
		<td><b>chain</b></td>
		<td><a href='http://docs.oracle.com/javaee/6/api/javax/servlet/FilterChain.html'>FilterChain</a></td>
	</tr>
	<tr>
		<td></td>
		<td>The requests filter chain.  <b>Available in route handlers only.</b></td>
	</tr>
	<tr>
		<td colspan='2'>Example:<br/><pre>chain.doFilter()</pre></td>
	</tr>		
</table>

### Rendering a View

Gravy provides a number of options for view templating in your application:

	- gstring module
	- freemarker module
	- scalate module
	- JSP/JSTL

#### gstring module

To enable the gstring module add 'gstring' to the gravy modules list in config.groovy.

	gravy {
		modules = ['gstring']
	}

The gstring module uses an enhanced version of the GStringTemplateEngine.  It has been enhanced with the following features:

	1. Layouts
	2. i18n
	3. XSS sanitization

##### Layouts

All gstring layouts must be defined in the <tt>view/.layouts</tt> folder.  A layout is a template that has one or more named sections.  Any view utilizing a layout must provide content for each section.   Consider the following layout and view:

view/.layout/catalog/main.html - the layout

	<html>
	<body>
		<div class="header">
			<#section header>
		</div>
		<div class="item">
			<#section itemInfo>
		</div>
		<div class="footer">
			<#section footer>
		</div>
	</body>
	</html>

view/catalog/itemDetail.html - the view

	<#layout catalog/main.html>

	<#section header>
	<h1>Page Header</h1>

	<#section itemInfo>
	<ul>
		<li>foo - $1.00</li>
		<li>bar - $2.00</li>
	</ul>
	
	<#section footer>
	<i>copyright acme inc.</i>


### Scheduled Tasks

Gravy also provides a way to define scheduled tasks as follows:

	def batchMailer = module('batchMailer')
	schedule('*/5 * * * *') { // cron string
		log.info "sending batch id ${batchMailer.batchId}"
		batchMailer.sendBatch()
	}


### Script Bindings

The following objects are bound to application.groovy, subscripts (the script folder), and module scripts:

	URI Handler objects:
	routes 					- create a new route
	controller 				- create a new 
	root                    - denotes the root node of the uri hierarchy (aka '/')

	config                  - the application config object 
	log                     - the application logger
	run() 					- execute a subscript - scripts folder

	Route / Filter Dispatch Types:
	REQUEST                 - DispatcherType.REQUEST
	FORWARD                 - DispatcherType.FORWARD
	ERROR                   - DispatcherType.ERROR
	ASYNC					- DispatcherType.ASYNC
	INCLUDE					- DispatcherType.INCLUDE

	Exclusive to application.groovy:
	module('mod-name')      - load a module programmatically
 

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

## Configuration



## The <tt>gravy</tt> Command

	gravy [clean|compile|test|run|war] [env dev|prod|other] [conf propertyName=propertyValue]

	Goals:
	clean         - delete all build products
	compile       - compile all Java and Groovy sources (output to target/classes) - depends on clean
	test          - execute all test scripts defined in src/test/groovy - depends on compile
	run           - run your Gravy application in dev mode (this is the default goal) - depends on compile
	war           - bundle your application as a web archive [appName].war in the target folder - depends on test

	Flags:
	env           - specify the execution environment ('dev' by default)
	conf          - configure an application property on the command line,
	                overriding config.groovy
	skip-tests    - for the lazy

	Tools:
	create [appName]      - create a new application with the given name
	create-mod [modName]  - create a new module with the given name
	jar-mod               - bundle a module as a jar file
	app-to-mod [modName]  - convert an application into a module, optional name parameter (defaults to app name)



## Preparing Your Application for Deployment

Gravy applications are meant for deployment as web archives.  To package your applicaiton as a WAR file use the <tt>gravy war</tt> command.

### Prerequisites

	* A Servlet 3.0 compliant web container

### gravy war

The war command produces a web archive in the 'target' sub-folder of your application:

	$ gravy war

The packaged application is organized as follows:

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

## Modules

Modules are the primary mechanism for code reuse in Gravy applications.

## Sloganeering 

	"For your infrastructure it's still meat-and-potatoes, but for your developers it's all Gravy."

## Credits:

* [Groovy](http://groovy.codehaus.org/) - Groovy 2.0.1 script engine
* [Jetty](http://www.eclipse.org/jetty/) - Embedded Servlet 3.0 web container (dev mode)
* [cron4j](http://www.sauronsoftware.it/projects/cron4j/) - Cron task scheduler
* [JNotify](http://jnotify.sourceforge.net/) - Dev-mode source monitoring
* [Freemarker](http://freemarker.sourceforge.net/) - The freemarker module template engine
* [Scalate](http://scalate.fusesource.org/) - The scalate module template engine
