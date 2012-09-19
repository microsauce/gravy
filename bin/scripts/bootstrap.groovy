#!/usr/bin/env groovy

import org.microsauce.util.CommandLine
import org.microsauce.gravy.dev.Lifecycle

/**
* Initialize and launch gravy.
*/

// environment validation
def sysEnv = System.getenv()
def gravyHome = sysEnv['GRAVY_HOME'] 
def javaHome = sysEnv['JAVA_HOME']

if (!javaHome || !gravyHome) {
	println 'Error: both JAVA_HOME and GRAVY_HOME must be set'
	System.exit(1)
}

// build classpath
println 'Bootstrapping gravy environment:\n\n'
println 'Building classpath . . .\n'
println '\tgravy jars:'
addFolder("$gravyHome/lib")
addFolder("$gravyHome/lib/jnotify")
addFolder("$gravyHome/lib/freemarker")
addFolder("$gravyHome/lib/jetty8")
addFolder("$gravyHome/lib/jetty8/jsp")
addFolder("$gravyHome/lib/groovy")
addFolder("$javaHome/lib")

//println '\n\tApplication jars:'
//def projectLib = new File('./lib')
//if ( projectLib.exists() ) {
//	addFolder('./lib')
//}
//else println '\tnone'
println ''

//
// parse command line
//
def commandLine = new CommandLine(args)
if (commandLine.hasOption('create')) {
	def name = commandLine.optionValue('create')
	if ( name == null ) {
		println 'please provide an app name'
		System.exit(0)
	}
	def builder = new Lifecycle()
	builder.createApp(name, commandLine.hasOption('example'))
	System.exit(0)
}
if (commandLine.hasOption('create-mod')) {
	def name = commandLine.optionValue('create-mod')
	if ( name == null ) {
		println 'please provide a module name'
		System.exit(0)
	}
	def builder = new Lifecycle()
	builder.createMod(name)
	System.exit(0)
}
if (commandLine.hasOption('jar-mod')) {
	def name = commandLine.optionValue('jar-mod')
	if ( name == null ) {
		println 'please provide a module name'
		System.exit(0)
	}
	def builder = new Lifecycle()
	builder.jarMod(name)
	System.exit(0)
}
if (commandLine.hasOption('app-to-mod')) {
	def name = commandLine.optionValue('app-to-mod')
	def builder = new Lifecycle()
	builder.appToMod(name)
	System.exit(0)
}
if (commandLine.hasOption('war')) {
	def name = commandLine.optionValue('name')
	def builder = new Lifecycle()
	builder.war(name, commandLine.hasOption('skip-test'))
	System.exit(0)
}
if (commandLine.hasOption('test')) {
	def tester = new Lifecycle()
	tester.test()
	System.exit(0)
}
if (commandLine.hasOption('compile')) {
	def builder = new Lifecycle()
	builder.compile()
	System.exit(0)
}
if (commandLine.hasOption('clean')) {
	def builder = new Lifecycle()
	builder.clean()
	System.exit(0)
}

//
// start the dev server
//
this.getClass().classLoader.rootLoader.loadClass('org.microsauce.gravy.server.bootstrap.StartUp')
	.getMethod('main', String[].class)
	.invoke(null, [args as String[]] as Object[])

def addFolder(folder) {
	new File(folder).eachFile { file ->
		if (file.name.endsWith('.jar')) {
			println "\t${file.absolutePath}"
			addResource(file.absolutePath)
		}
	}
}

/*
	add a resource to the root loader
*/
def addResource(url) {
	this.getClass().classLoader.rootLoader.addURL(new File(url).toURL())
}
