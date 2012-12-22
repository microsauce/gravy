#!/usr/bin/env groovy

import org.microsauce.gravy.dev.lifecycle.Lifecycle;
import org.microsauce.gravy.util.CommandLine;

//
// parse command line
//
def subCommands = [
	'clean',
	'resolve',
	'compile',
	'test',
	'assemble',
	'run',
	'war',
	'create',
	'mod-ify',
	'list-mods',
	'install-mod',
	'jar-mod'
]
def commandLine = new CommandLine(args, subCommands)
commandLine.defaultCommand = 'run'
def command = commandLine.command
if (commandLine.hasOption('help')) {
	def help = '''
gravy [clean|resolve|compile|test|assemble|run|war] [env dev|prod|other] [conf propertyName=propertyValue]

 Lifecycle Goals:
=++++++++++++++++=
clean         - delete all build products
resolve       - resolve dependencies (libraries and modules)
compile       - compile all Java and Groovy sources (output to target/classes) - depends on clean
test          - execute test scripts - defined in src/test/(groovy|javascript) - depends on compile
assemble      - assemble the application - depends on test / compile
run           - run your Gravy application in dev mode (this is the default goal) - depends on assemble
war           - package your application as a web archive [appName].war in the target folder - depends on assemble

 Tools:
=++++++=
*create-gv [app-name]    - create a new Groovy application
*create-js [app-name]    - create a new JavaScript application
*create-cs [app-name]    - create a new CoffeeScript application
*sample [app-name]       - create a sample application

list-mods               - list available core modules
install-mod [mod-name]  - install a core module in this application
mod-ify	                - package this application as module jar
jar-mod [mod-name]      - package the named module as a jar

 Flags:
=++++++=
env           - specify the execution environment ('dev' by default)
conf          - configure an application property on the command line, overriding config.groovy
skip-tests    - for the impatient

'''
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
	def lifecycle = new Lifecycle(getConfigObject())
	lifecycle.createApp(name, commandLine.hasOption('example'))
	System.exit(0)
}
if (commandLine.hasOption('list-mods')) {
	def lifecycle = new Lifecycle(getConfigObject())
	listCoreModules(gravyHome)
	System.exit(0)
}
if (commandLine.hasOption('install-mod')) {
	def name = commandLine.optionValue('install-mod')
	if ( name == null ) {
		println 'please provide a core module name'
		System.exit(0)
	}
	def lifecycle = new Lifecycle(getConfigObject())
	lifecycle.installCoreModule(name)
	System.exit(0)
}
if (commandLine.hasOption('create-mod')) {
	def name = commandLine.optionValue('create-mod')
	if ( name == null ) {
		println 'please provide a module name'
		System.exit(0)
	}
	def lifecycle = new Lifecycle(getConfigObject())
	lifecycle.createMod(name)
	System.exit(0)
}
if (commandLine.hasOption('jar-mod')) {
	def name = commandLine.optionValue('jar-mod')
	if ( name == null ) {
		println 'please provide a module name'
		System.exit(0)
	}
	def lifecycle = new Lifecycle(getConfigObject(name)) //
	lifecycle.jarMod(name)
	System.exit(0)
}
if (commandLine.hasOption('mod-ify')) {
	def lifecycle = new Lifecycle(getConfigObject())
	lifecycle.modIfyApp()
	System.exit(0)
}

//
// Goals
//
if (commandLine.hasOption('war')) {
	def name = commandLine.optionValue('name')
	def lifecycle = new Lifecycle(getConfigObject())
	lifecycle.war(name, commandLine.hasOption('skip-test'))
	System.exit(0)
}
if (commandLine.hasOption('assemble')) {
	def lifecycle = new Lifecycle(getConfigObject())
	lifecycle.assemble()
	System.exit(0)
}
if (commandLine.hasOption('test')) {
	def configObject = getConfigObject()

	def lifecycle = new Lifecycle(getConfigObject()) 
	lifecycle.test()
	System.exit(0)
}
if (commandLine.hasOption('compile')) {
	def lifecycle = new Lifecycle(getConfigObject())
	lifecycle.compile()
	System.exit(0)
}
if ( commandLine.hasOption('resolve') ) {
	def lifecycle = new Lifecycle(getConfigObject())
	lifecycle.resolve()
}
if (commandLine.hasOption('clean')) {
	def lifecycle = new Lifecycle(getConfigObject())
	lifecycle.cleanAll() // delete build products and managed dependencies
	System.exit(0)
}

//
// default goal is run.  compile app sources and bootstrap the server.
//
if ( command == 'run' ) {   
	def lifecycle = new Lifecycle(getConfigObject())
	lifecycle.assemble()

	//
	// start the dev server
	//
	def gse = new GroovyScriptEngine(["$gravyHome/bin/scripts"] as String[])
	gse.run('bootstrap.groovy', [args : args] as Binding)
}

//
// methods
//
ConfigObject getConfigObject() {
	def projectFolder = System.getProperty('user.dir')
	def configFile = new File("${projectFolder}/conf/config.groovy")
	
	if ( configFile.exists() )
		return new ConfigSlurper().parse(configFile.toURL())
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

