
Gravy 0.1
===

Gravy is an intuitive, expressjs inspired framework for rapid development of web applications
on the Java Enterprise (the meat-and-potatoes).  Gravy applications can be written in Groovy,
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

Gravy is currently Alpha software.  The focus of the current milestone (0.1) is to
define and implement the core feature set (routing, scheduled tasks, middleware,
servlet request/response/session decorators, modules, resource resolver, single file
configuration, dev tools, etc).  0.2 will include significant refactoring and performance
and routing enhancements.  0.3 will finish out the tooling api (package/dependency
management, etc), with a 1.0 beta to follow.

## Getting Started

### Prerequisites

	* Java 7 JDK
	* JAVA_HOME must refer to your Java 7 JDK
	* ant or gradle

### Installation

	$ git clone https://github.com/microsauce/gravy.git
	$ cd gravy
	$ ant OR $ gradle
	$ export GRAVY_HOME=$PWD
	$ PATH=$PATH:$GRAVY_HOME/bin

### 'hello gravy'

Create your first app:

1. Create your application file:

    $ mkdir myGravyApp ; cd myGravyApp ; echo -e "get '/hello/:name', { res.print \042Hello \$name! \042 }" > application.groovy

2. Start the gravy dev server:

    $ gravy

3. Point your browser at:

	http://localhost:8080/hello/You

### Rapid Development

Gravy is a rapid development environment.  All source files are automatically re-deployed upon
modification.  Make a change, refresh your browser, and benefit from the instant feedback.

## routes

In Gravy, a route is a chain of callbacks that is assembled to service a HTTP request URI and method.  A route consist
of an end-point (the ultimate destination of the route) and any number of intermediate callbacks (middleware).

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

#### uri patterns
Gravy currently supports uri patterns consisting of named parameters (in the form :paramName) and wildcards (*).

TODO
optional parameters - delay to M2
uri params are determined by the end-point pattern (what about static end-points?)

### middleware api

use([uriPattern,] callback)

param(uriParamName, callback)

```rb
get '/sandwhich/:meat/:cheese/:customer' do
  res.print "Order Up!  One #{meat} & #{cheese} sandwhich for #{customer}."
end
```
### middleware

use, param, additional end point callbacks

### uri patterns

parameters, wildcards, splat, callback parameters, regular expressions

### route assembly

Route chains are assembled in the following order:

1.  'param' middleware in the order each parameter appears in the matching uri pattern
2.  'use' middleware in the order it was defined in the application script(s)
3.  Middleware callbacks passed directly to the get, post, put, delete methods
4.  Naturally, the end-point comes last.

One final note on route assembly.  A route chain does not require an end-point (when an end-point callback is not defined
gravy will attempt to load the given uri as a static resource (a file)). For example:
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

### req
* params - a hash of request parameters
* form -
* query -
* json -
* sess - the session object
* forward(forwardUri) - forward the request to the given uri
* next() - execute the next callback in the route chain
* input - the request input stream

For additional information regarding 'req' see:

### sess

See:

### res
render(viewUri, model) - render a view for the given view uri and model
renderJson(model) - serialize the given object (model) send response as application/json
redirect(redirectURL) - redirect the client to the given url
print(str) - print a string to the response output stream
write(data) - write data to the response output stream
out - the response output stream

For additional information regarding 'res' see:

## request/session attributes

Request and session attributes are accessible by name via the dot operator:
```
req.firstName
```
hash notation:
```
sess['firstName']
```
or the getAttribute method:
```js
req.getAttribute('firstName');
```
```ruby
req.get_attribute('firstName')
```

## io
The response stream is flushed by the gravy runtime following every callback and is closed by the servlet runtime at the
completion of the request.

## views

res.render

## modules (app fragments)

A Gravy application is composed of one or more modules.  Modules define a set of web services (similar in concept 
to a Servlet 3.0 'web fragment').

### configuration

Gravy uses the groovy ConfigSlurper to load configuration files (conf.groovy).  conf.groovy should be defined in the
root of your application/module folder.  The app module configuration can set properties

For more information see #


## error handling

## cron tasks

## project structure

## the gravy command