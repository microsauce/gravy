
/**
* Bootstrap the Gravy development runtime
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
println ''
println '========================================================================='
println '= build runtime classpath                                               ='
println '========================================================================='
println ''

addFolder("$gravyHome/lib")
addFolder("$gravyHome/lib/jnotify")
addFolder("$gravyHome/lib/jetty8")
addFolder("$gravyHome/lib/jetty8/jsp")
addFolder("$gravyHome/lib/rhino")

// launch
println ''
println '========================================================================='
println '= launch application server                                             ='
println '========================================================================='
println ''
this.getClass().classLoader.rootLoader.loadClass('org.microsauce.gravy.dev.runtime.StartUp')
	.getMethod('main', String[].class)
	.invoke(null, [args as String[]] as Object[])

/*
 	script methods
*/
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
