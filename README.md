
Gravy
===

Gravy is a dynamic server scripting framework for the Java enterprise.  Its design focuses on modularity and rapid development.

Groovy:

	get '/hello/:name', { 
		out << "Hello $name!"
	}

CoffeeScript:

	get '/hello/:name', (req, res) ->
		res.write "Hello #{@name}!"
		
JavaScript:

	get('hello/:name', function(req, res) {
		res.write("Hello " + this.name + "!")
	})

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

