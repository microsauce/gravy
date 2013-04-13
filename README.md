
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
- Scheduled tasks
- Polyglot (Groovy, Ruby, JavaScript/CoffeeScript)
- Modules
- Single-file environment based configuration
- Hot reload

## Roadmap

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

2. Add a route to your file, for example:
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

In gravy, a route is a software chain that is built to service a particular HTTP request URI pattern and method. For example:
```rb
use '/arithmetic/*' do
    log.info "we're about to do some arithmetic: #{req.request_uri}"
    req.next
end

get '/arithmetic/add/:addend1/:addend2' do
    res.print "the sum: #{addend1} + #{addend2} = <b>#{addend1.to_i+addend2.to_i+counter}</b>"
end
```

## servlet API

## middleware

## configuration

### modules (app fragments)

A Gravy application is composed of one or more modules.  Modules define a set of web services (similar in concept 
to a Servlet 3.0 'web fragment').

## errors

## the gravy command