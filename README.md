
Gravy 0.1
===

Gravy is simple, expressjs inspired framework for developing MVC web applications
on the Java Enterprise (the meat-and-potatoes).  API's are available for Groovy,
JavaScript, CoffeeScript, and/or Ruby.

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
- Express style routing
- Polyglot (Groovy, Ruby, JavaScript/CoffeeScript)
- Modules (application fragments)
- Single-file environment based configuration
- Scheduled tasks
- Hot reload

## Status & Roadmap

The focus of the current milestone (0.1) is to define and implement the core feature 
set (routing, scheduled tasks, middleware, servlet request/response/session decorators, 
modules, resource resolver, single file configuration, dev tools, etc).  0.2 will include 
significant refactoring and performance and routing enhancements.  0.3 will finish out 
the tooling api (package/dependency management, etc), with a 1.0 beta to follow.

## Getting Started

### Prerequisites

	* Java 7 JDK
	* JAVA_HOME must refer to your Java 7 JDK
	* ant or gradle

### Installation
```
	$ git clone https://github.com/microsauce/gravy.git
	$ cd gravy
	$ ant OR $ gradle
	$ export GRAVY_HOME=$PWD
	$ PATH=$PATH:$GRAVY_HOME/bin
```

### 'hello gravy'

Create your first app:

1. Create your application file:
```
    $ mkdir myGravyApp ; cd myGravyApp ; echo -e "get '/hello/:name', { res.print \042Hello \$name! \042 }" > application.groovy
```

2. Start the gravy dev server:
```
    $ gravy
```

3. Point your browser at:

	http://localhost:8080/hello/You

## Rapid Development

All source files are automatically re-deployed upon modification.  Make a change, refresh your browser, and 
benefit from the quick feedback.

## routes

In Gravy, a route is a chain of callbacks that is assembled to service a particular HTTP request URI and method.  A route consists
of an end-point (the ultimate destination of the route) and any number of intermediate callbacks (middleware).  A route 
may contain callbacks defined in any application module (and therefore may be polyglot, more on this later). 

### end-points

There are two types of end-points: callbacks and static content (files).  End-point callbacks are defined via the 'get', 'post',
'put', 'delete' ('del' in JavaScript), and 'all' methods.  These names correspond to the http methods of the same name.  The
method call is of the form:
```
get|post|put|delete|all(uriPattern [,middlewareCallback...], endPointCallback)
```

When no end-point callback matches the given URI and method Gravy presumes the request URI refers to a static resource.

Examples:

Given the following callback definitions:
```groovy
    use '/sandwich/order/*', {
        req.next()
        res.print " and a bag of chips"
    }

    get '/sandwich/order/:name', {
        res.print "here is your $name sandwich"
    }

```
Request header: "GET /sandwich/order/ham-and-cheese" will result in the following response:
```
    here is your ham-and-cheese sandwhich and a bag of chips
```

#### route / uri patterns
Gravy currently supports route patterns consisting of named parameters (in the form :paramName) and wildcards (*).


### middleware api

use([uriPattern,] callback)

param(uriParamName, callback)

```rb
get '/sandwhich/:meat/:cheese/:customer' do
  res.print "Order Up!  One #{meat} & #{cheese} sandwhich for #{customer}."
end
```
### middleware

What is middleware?  Middleware is software that handles cross-cutting concerns such as logging, data
loading, caching, etc ('filters' in the standard JEE world, but with more flexibility).  There are 
several ways to define middleware in gravy:      

use, param, additional end point callbacks

### uri patterns

parameters, wildcards, splat, callback parameters, regular expressions

### route assembly

Route chains are assembled in the following order:

1.  'param' middleware in the order each parameter appears in the matching uri pattern
2.  'use' middleware in the order it was defined in the application script(s)
3.  Middleware callbacks passed directly to the get, post, put, delete methods
4.  Naturally, the end-point comes last.

One final note on route assembly.  A route chain does not require an end-point callback (when an end-point 
callback is not defined gravy will attempt to load the given uri as a static resource (a file)). For example:
```js
use('/images/*', function(req,res) {
   log.info('image uri: ' + req.getRequestURI());
   req.next();
});
```

## servlet API

Every callback has a reference to a request object (req) and a response object (res).  These objects decorate the
underlying HttpServletRequest and HttpServletResponse with useful methods, properties and, dare I say, syntactic
~~sugar~~ gravy.

### req - see also: [HttpServletRequest](http://docs.oracle.com/javaee/6/api/javax/servlet/http/HttpServletRequest.html)
The req object models an http request and is based on JEE HttpServletRequest.  Gravy decorates the req object with the
following properties methods: 
* params - a hash of request parameters
* form - request form parameters
* query - request query parameters
* json - gravy auto binds 'application/json' content to req.json
* sess - the session object
* forward(forwardUri) - forward the request to the given uri
* next() - execute the next callback in the route chain
* input - the request input stream

### sess - see also: [HttpSession](http://docs.oracle.com/javaee/6/api/javax/servlet/http/HttpSession.html)

### res - see also: [HttpServletResponse](http://docs.oracle.com/javaee/6/api/javax/servlet/http/HttpServletResponse.html)
The res object models an http response and is based on HttpServletResponse.  Gravy decorates the res object with the 
following properties/methods:
* render(viewUri, model) - render a view for the given view uri and model
* renderJson(model) - serialize the given object (model) send response as application/json
* redirect(redirectURL) - redirect the client to the given url
* print(str) - print a string to the response output stream
* write(data) - write data to the response output stream
* out - the response output stream

### request/session attributes
Request and session attributes are accessible by name via the dot operator:
```
req.firstName
req.lastName = 'Foo'
```
hash notation:
```
sess['firstName']
```

Attributes can also be set/retrieved in the standard JEE way via req.getAttribute()|setAttribute():
```js
req.getAttribute('firstName');
```
```ruby
req.set_attribute('lastName', bar)
```

## polyglot routes
As noted above, route chains may be polyglot, containing callbacks defined in more than one language. 

## io
The response output stream is flushed by the gravy runtime at the completion of the route chain and is 
closed by the servlet runtime at the completion of the request.

## views

res.render

## modules (app fragments)

A Gravy application is composed of one or more modules.  Modules define a set of web services (similar in concept 
to a Servlet 3.0 'web fragment').

### configuration

Gravy uses the groovy ConfigSlurper to load the configuration file (conf.groovy).  conf.groovy should be defined in the
root of your application/module folder.    

For more information see [ConfigSlurper](http://groovy.codehaus.org/ConfigSlurper)


## error handling

## cron tasks

```
schedule(cronString,callback)
```

## project structure


## the gravy command

