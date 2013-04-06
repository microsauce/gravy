
Gravy 0.1
===

Gravy is a simple, intuitive framework for rapid development of web applications on the Java
Enterprise (the meat-and-potatoes).  Script applications in Groovy, JavaScript, CoffeeScript,
and/or Ruby and benefit from the productivity and features that each language provides.

Gravy is influenced by Sinatra and express.js style routing.  Here is a taste:

Hello Groovy:
```groovy
get '/hello/:name', {
    res.print "Hello $name!"
}
```
Hello JavaScript:
```js
get('hello/:name', function(req, res) {
    res.print("Hello " + this.name + "!")
})
```
Hello CoffeeScript:
```coffee
get '/hello/:name', (req, res) ->
    res.print "Hello #{@name}!"
```
Hello Ruby:
```ruby
get '/hello/:name' do
    res.print "Hello #{name}!"
end
```

## Features
- Sinatra / Express.js style routing
- Scheduled tasks
- Polyglot (Groovy, Ruby, JavaScript/CoffeeScript)
- 'Web-fragement' modules
- Single-file environment based configuration
- Hot reload
-

## Status

Gravy is currently Alpha software.  The focus of the current milestone (0.1) is to
define and implement the core feature set (routing, scheduled tasks, servlet request/response/session
decorators, modules, resource resolver, single file configuration, dev tools, etc).  0.2 will
include significant refactoring and performance enhancements and enhanced routing features.
0.3 will add Python and finish out the tooling api (package management, etc).

Programming and tooling API's are still evolving, but near completion.

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

A Gravy application can be as simple as a single script (application.groovy|js|coffee|rb), but most Gravy 
applications will also employ modules, views (templates), static content (images, css, js), Java and 
Groovy sources, and custom configuration. To create a sample application which demonstrates each of 
these additional components run the following command:

	$ gravy create [app-name] sample

This command generates a sample application in a folder named [app-name].  The application folder layout 
is as follows:

	[app-name]                                - root application folder
	    |- application.(groovy|js|coffee|rb)  - main application script
	    |_ src                                - java and groovy sources
	    |   |_ main
	    |   |   |- java
	    |   |   |- groovy
	    |   |   |_ resources                  - static resources to add to the application
	    |   |                                   classpath
	    |   |_ test 
	    |       |_ groovy                     - groovy test scripts
	    |       |_ javascript                 - javascript/coffeescript test scripts
	    |       |_ ruby                       - ruby test scripts
	    |- view                               - view templates
	    |- conf                               - configuration (config.groovy)
	    |- webroot                            - static resources (html, css, js, images, etc)
	    |   |_ WEB-INF         
	    |       |_ web.xml                    - the application deployment descriptor, for most
	    |
	    |- modules                            - modules (application fragments)
	    |_ lib                                - jar files, JS/Ruby libraries

To run your new Gravy app execute the gravy commands:

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
to a Servlet 3.0 'web fragment') and/or they export functionality to the 'app' module.  Module exports are bound to 
the app script by module-name.

## The Finer Points



## More to come . . .