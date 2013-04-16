
Gravy 0.1
===

Gravy is an intuitive, expressjs inspired framework for rapid development of web applications
on the Java Enterprise (the meat-and-potatoes).  Gravy applications can be written in Groovy,
JavaScript, CoffeeScript, and/or Ruby.

Here is a taste:

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
- Sinatra / express style routing
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
	$ ant
	$ export GRAVY_HOME=$PWD
	$ PATH=$PATH:$GRAVY_HOME/bin

### 'hello gravy'

Create your first app:

1. Create your application file:

    $ mkdir myGravyApp ; cd myGravyApp ; echo "" > application.groovy

2. Define a service, for example:
```groovy
get '/hello/:name', {
    res.print "Hello $name!"
}
```

3. Start the gravy dev server:

    $ gravy

4. Point your browser at:

	http://localhost:8080/hello/You

### Rapid Development

Gravy is a rapid development environment.  All source files are automatically re-deployed upon
modification.  Make a change, refresh your browser, and benefit from the instant feedback.

## routes

A route is a chain of callbacks that is assembled to service a HTTP request URI and method.  Most routes* consist
of an end-point (the final callback in a chain) and middleware (any number of intermediate callbacks defined to decorate or
validate the request/response or handle any other cross-cutting concern).  Let's start with a simple route, one consisting of a
single callback.  To define this route we must specify one of the four recognized HTTP methods (get, post, put, and delete)**,
a uri pattern, and a callback.  For example:
```rb
get '/sandwhich/:meat/:cheese/:customer' do
  res.print "Order Up!  One #{meat} & #{cheese} sandwhich for #{customer}."
end
```
In this example we have defined a callback that responds to HTTP 'get' requests for uri pattern: '/sandwhich/:meat/:cheese/:customer'
(more on uri patterns below).  For example, this callback will service any of the following urls:
```
http://localhost:8080/sandwhich/ham/swiss/Sarah
http://localhost:8080/sandwhich/italian-beef/provolone/John
http://localhost:8080/sandwhich/turkey/muenster/Steve
```
Let's add another link to this chain.  Suppose, for example, we have a very picky regular at our shop and we want to
ensure we always get his order right.  We could do this will middleware.  There are several ways to define/apply middleware,
we'll start with the 'use' function:
```groovy
use '/sandwhich/*', {
    req.next()
    if ( customer == 'Steve' )
        res.print "<br/><b>Hold the sprouts!!!</b>"
}
```
This middleware tags additional instructions to the order when the customer is 'Steve'.

### end points

get, post, put, delete, static content

### middleware

use, param, additional end point callbacks

### uri patterns

parameters, wildcards, splat, callback parameters, regular expressions

### route assembly

Route chains are assembled according to a predictable set of rules:

1.  'param' middleware is added first in the order each parameter appears in the matching uri pattern*** (note: uri params are determined by the chain end-point pattern, only one end-point per chain)
2.  'use' middleware is added to the chain next in the order it was defined in the application script(s) *** (note: modules are loaded first).
3.  End-point specific middleware (additional callbacks passed to get, post, put, delete functions) are next and are added in the order they are passed to the function.
4.  Naturally, the end-point comes last.

Two final notes on route assembly.  A route chain does not require an end-point.  A end-point can be any static resource, for example:
```js
use('/images/*', function(req,res) {
   log.info('image uri: ' + req.getRequestURI());
   req.next();
});
```

## servlet API

Every callback is given (passed/injected with) a reference to a request object (req) and a response object (res).  These
objects decorate the underlying HttpServletRequest and HttpServletResponse with useful methods and, dare I say,
syntactic ~~sugar~~ gravy.

### req
forward(forwardUri) - forward the request to the given uri
next() - execute the next callback in the route chain
input - the request input stream


### res
render(viewUri, model) - render a view for the given view uri and model
renderJson(model) - serialize the given object (model) as JSON in the response
redirect(redirectURL) - redirect the client to the given url
print(str) - print a string to the response output stream
write(data) - write data to the response output stream
out - the response output stream

## request/session attributes

## io
The response stream is flushed by the gravy runtime following every callback and is closed by the servlet runtime at the
completion of the request.

## views

res.render

## configuration

conf.groovy

## modules (app fragments)

A Gravy application is composed of one or more modules.  Modules define a set of web services (similar in concept 
to a Servlet 3.0 'web fragment').

### configuration

## error handling

## cron tasks

## project structure

## the gravy command