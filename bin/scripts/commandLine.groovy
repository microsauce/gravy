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
resolve       - resolve dependencies
compile       - compile all Java and Groovy sources (output to target/classes) - depends on clean
test          - execute all test scripts defined in src/test/groovy - depends on compile
assemble      - assemble the application - depends on test / compile
run           - run your Gravy application in dev mode (this is the default goal) - depends on assemble
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
	def lifecycle = new Lifecycle(getConfig())
	lifecycle.createApp(name, commandLine.hasOption('example'))
	System.exit(0)
}
if (commandLine.hasOption('list-mods')) {
	def lifecycle = new Lifecycle(getConfig())
	listCoreModules(gravyHome)
	System.exit(0)
}
if (commandLine.hasOption('install-mod')) {
	def name = commandLine.optionValue('install-mod')
	if ( name == null ) {
		println 'please provide a core module name'
		System.exit(0)
	}
	def lifecycle = new Lifecycle(getConfig())
	lifecycle.installCoreModule(name)
	System.exit(0)
}
if (commandLine.hasOption('create-mod')) {
	def name = commandLine.optionValue('create-mod')
	if ( name == null ) {
		println 'please provide a module name'
		System.exit(0)
	}
	def lifecycle = new Lifecycle(getConfig())
	lifecycle.createMod(name)
	System.exit(0)
}
if (commandLine.hasOption('jar-mod')) {
	def name = commandLine.optionValue('jar-mod')
	if ( name == null ) {
		println 'please provide a module name'
		System.exit(0)
	}
	def lifecycle = new Lifecycle(modConfig(name)) //
	lifecycle.jarMod(name)
	System.exit(0)
}
if (commandLine.hasOption('mod-ify')) {
	def lifecycle = new Lifecycle(getConfig())
	lifecycle.modIfyApp()
	System.exit(0)
}
// TODO instantiate Lifecycle via newLifecycle

//
// Goals
//
if (commandLine.hasOption('war')) {
//	def conf =new ConfigSlurper().parse(new File('./conf/config.groovy').toURL())
	def name = commandLine.optionValue('name')
	def lifecycle = new Lifecycle(getConfig())
	lifecycle.war(name, commandLine.hasOption('skip-test'))
	System.exit(0)
}
if (commandLine.hasOption('assemble')) {
//	def conf =new ConfigSlurper().parse(new File('./conf/config.groovy').toURL())
	def lifecycle = new Lifecycle(getConfig())
	lifecycle.assemble()
	System.exit(0)
}
if (commandLine.hasOption('test')) {
//println "rootLoader: ${this.class.classLoader.rootLoader}"
	def lifecycle = new Lifecycle(getConfig()) //new Lifecycle(getConfig())
	lifecycle.test()
//	tester.test() 
	System.exit(0)
}
if (commandLine.hasOption('compile')) {
	def lifecycle = new Lifecycle(getConfig())
	lifecycle.compile()
	System.exit(0)
}
if ( commandLine.hasOption('resolve') ) {
	def lifecycle = new Lifecycle(getConfig())
	lifecycle.resolve()
}
if (commandLine.hasOption('clean')) {
	def lifecycle = new Lifecycle(getConfig())
	lifecycle.cleanAll() // delete build products and managed dependencies
	System.exit(0)
}

//
// default goal is run.  compile app sources and bootstrap the server.
//
def lifecycle = new Lifecycle(getConfig())
//def conf =new ConfigSlurper().parse(new File('./conf/config.groovy').toURL())
lifecycle.assemble()

//
// start the dev server
//
def gse = new GroovyScriptEngine(["$gravyHome/bin/scripts"] as String[])
gse.run('bootstrap.groovy', [args : args] as Binding)


//
// methods
//
Properties getConfig() {
	def projectFolder = System.getProperty('user.dir')
	def configFile = new File("${projectFolder}/conf/config.groovy")
	if ( configFile.exists() )
		return new ConfigSlurper().parse(new File("${projectFolder}/conf/config.groovy").toURL()).toProperties()
	else return null
}

Properties modConfig(modName) {
	def projectFolder = System.getProperty('user.dir')
	def configFile = new File("${projectFolder}/modules/${modName}/conf/config.groovy")
	if ( configFile.exists() )
		return new ConfigSlurper().parse(new File("${projectFolder}/conf/config.groovy").toURL()).toProperties()
	else return null
}

ConfigObject coreModConfig(modConfFile) {
//	def configFile = new File("${projectFolder}/modules/${modName}/conf/config.groovy")
	if ( modConfFile.exists() )
		return new ConfigSlurper().parse(modConfFile.toURL())
	else return null
}

void listCoreModules(gravyHome) {
	println ''
	println "   Name (Version)              Description"
	println "   ================================================================================================="
	new File("${gravyHome}/modules").eachDir { modDir ->
		def modConf = new File(modDir, '/conf/config.groovy')
		def configObject = coreModConfig(modConf)
		if ( !configObject ) {
			println "${modDir.name}"
		} else {
			def description = configObject.meta.description
			def version = configObject.meta.version
			def author = configObject.meta.author

			println "   ${(modDir.name+' ('+version+')').padRight(25) } - ${description}"
		}
	}
	println ''
}

//Lifecycle newLifecycle(properties) {
//	def gcl = new GroovyClassLoader(this.getClass().getClassLoader().rootLoader)
//	def clazz = gcl.loadClass('org.microsauce.gravy.dev.Lifecycle')
//	clazz.getConstructor(Properties.class).newInstance(properties)
//}



