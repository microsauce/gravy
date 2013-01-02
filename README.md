
Gravy
===

Gravy is a framework for rapid development of web applications in Groovy, JavaScript, or CoffeeScript 
on the Java Enterprise (a.k.a the meat-and-potatoes).  

Hello Groovy:

	get '/hello/:name', { 
		out << "Hello $name!"
	}

Hello JavaScript:

	get('hello/:name', function(req, res) {
		res.write("Hello " + this.name + "!")
	})

Hello CoffeeScript:

	get '/hello/:name', (req, res) ->
		res.write "Hello #{@name}!"
		
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

A Gravy application can be as simple as a single script (application.groovy|js|coffee), but most Gravy 
applications will also employ modules, views (templates), static content (images, css, js), Java and 
Groovy sources, and custom configuration. To create a sample application which demonstrates each of 
these additional components run the following command:

	$ gravy create [app-name] sample

This command generates a sample application in a folder named [app-name].  The application folder layout 
is as follows:

	[app-name]                             - root application folder
	    |- application.(groovy|js|coffee)  - main application script
	    |_ src                             - java and groovy sources   
	    |   |_ main
	    |   |   |- java
	    |   |   |- groovy
	    |   |   |_ resources               - static resources to add to the application 
	    |   |                                classpath
	    |   |_ test 
	    |       |_ groovy                  - groovy test scripts
	    |       |_ javascript              - javascript/coffeescript test scripts
	    |- view                            - view templates
	    |- conf                            - configuration (config.groovy)
	    |- webroot                         - static resources (html, css, js, images, etc)
	    |   |_ WEB-INF         
	    |       |_ web.xml                 - the application deployment descriptor, for most
	    |
	    |- scripts                         - groovy sub-scripts/JS libraries
	    |- modules                         - application modules
	    |_ lib                             - application jar files

To run your new Gravy app execute the gravy command:

	$ cd <app-name>
	$ gravy

Point your browser at:

	http://localhost:8080

### Rapid Development

Gravy is a rapid development environment.  All source files are automatically re-compiled and re-deployed upon 
modification.  Make a change, refresh your browser, and benefit from the instant feedback.

## The Big Picture

### Modules

A Gravy application is composed of one or more modules.  Modules define a set of web services (similar in concept 
to a Servlet 3.0 'web fragment') or they export functionality to the 'app' module (but not both).  Module 
exports are bound to the app script by module-name.  There is no support for cross language exports.

## The Finer Points



## More to come . . .