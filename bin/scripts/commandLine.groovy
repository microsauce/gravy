#!/usr/bin/env groovy

import org.microsauce.util.CommandLine
import org.microsauce.gravy.dev.Lifecycle

//
// parse command line
//
def commandLine = new CommandLine(args)
if (commandLine.hasOption('help')) {
	def help = '''gravy [clean|compile|test|run|war] [env dev|prod|other] [conf propertyName=propertyValue]

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
skip-tests    - for the lazy'''
	println help
	System.exit(0)
}

// environment validation
def sysEnv = System.getenv()
def gravyHome = sysEnv['GRAVY_HOME'] 
def javaHome = sysEnv['JAVA_HOME']

if (!javaHome || !gravyHome) {
	println 'Error: both JAVA_HOME and GRAVY_HOME must be set'
	System.exit(1)
}

//
// Tools
//
if (commandLine.hasOption('create')) {
	def name = commandLine.optionValue('create')
	if ( name == null ) {
		println 'please provide an app name'
		System.exit(0)
	}
	def lifecycle = new Lifecycle()
	lifecycle.createApp(name, commandLine.hasOption('example'))
	System.exit(0)
}
if (commandLine.hasOption('create-mod')) {
	def name = commandLine.optionValue('create-mod')
	if ( name == null ) {
		println 'please provide a module name'
		System.exit(0)
	}
	def lifecycle = new Lifecycle()
	lifecycle.createMod(name)
	System.exit(0)
}
if (commandLine.hasOption('jar-mod')) {
	def name = commandLine.optionValue('jar-mod')
	if ( name == null ) {
		println 'please provide a module name'
		System.exit(0)
	}
	def lifecycle = new Lifecycle()
	lifecycle.jarMod(name)
	System.exit(0)
}
if (commandLine.hasOption('app-to-mod')) {
	def name = commandLine.optionValue('app-to-mod')
	def lifecycle = new Lifecycle()
	lifecycle.appToMod(name)
	System.exit(0)
}

//
// Goals
//
if (commandLine.hasOption('deploy')) {
	def conf =new ConfigSlurper().parse(new File('./conf/config.groovy').toURL())
	def lifecycle = new Lifecycle()
	lifecycle.deploy(conf.gravy.modules ?: [])
	System.exit(0)
}
if (commandLine.hasOption('war')) {
	def conf =new ConfigSlurper().parse(new File('./conf/config.groovy').toURL())
	def name = commandLine.optionValue('name')
	def lifecycle = new Lifecycle()
	lifecycle.war(name, conf.gravy.modules ?: [], commandLine.hasOption('skip-test'))
	System.exit(0)
}
if (commandLine.hasOption('test')) {
	def tester = new Lifecycle()
	tester.test()
	System.exit(0)
}
if (commandLine.hasOption('compile')) {
	def lifecycle = new Lifecycle()
	lifecycle.compile()
	System.exit(0)
}
if (commandLine.hasOption('clean')) {
	def lifecycle = new Lifecycle()
	lifecycle.clean()
	System.exit(0)
}

//
// default goal is run.  compile app sources and bootstrap the server.
//
def lifecycle = new Lifecycle()
def conf =new ConfigSlurper().parse(new File('./conf/config.groovy').toURL())
lifecycle.deploy(conf.gravy.modules ?: [])


//
// start the dev server
//
def gse = new GroovyScriptEngine(["$gravyHome/bin/scripts"] as String[])
gse.run('bootstrap.groovy', [args : args] as Binding)
