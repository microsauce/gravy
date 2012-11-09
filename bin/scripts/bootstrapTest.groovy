import groovy.util.GroovyTestSuite
import junit.framework.Test
import junit.textui.TestRunner
import org.codehaus.groovy.runtime.ScriptTestAdapter
import groovy.io.FileType

//addFolder("${projectBasedir}/lib")
//addResource("${projectBasedir}/target/classes")

println "cl: ${this.class.classLoader}"
println "cl: ${this.class.classLoader.parent}"
println "cl: ${this.class.classLoader.parent.parent}"
println "cl: ${this.class.classLoader.parent.parent.parent}"

def ant = new AntBuilder()
def allTests = new GroovyTestSuite()
def testFolder = new File ("${projectBasedir}/src/test/groovy")
if ( testFolder.exists() ) {
	def testScripts = getTestScripts("${projectBasedir}/src/test/groovy")
	testScripts.each { thisScript ->
		allTests.addTest(new
			ScriptTestAdapter(allTests.compile(thisScript), [] as String[]))
	}

	if (allTests.countTestCases() < 1) return true

	return TestRunner.run(allTests).wasSuccessful()
} else {
	ant.echo "there are no unit test scripts defined for application $appName"
	return true
}

def private getTestScripts(testFolder) {
	def testScripts = []
	new File(testFolder).eachFileRecurse(FileType.FILES) { thisFile ->
		if (thisFile.name.endsWith('.groovy'))
			testScripts << thisFile.absolutePath
	}

	testScripts
}

def addFolder(folder) {
	new File(folder).eachFile { file ->
		if (file.name.endsWith('.jar')) {
			println "\t${file.absolutePath}"
			addResource(file.absolutePath)
		}
	}
}

def addResource(url) {
	this.getClass().classLoader.rootLoader.addURL(new File(url).toURL())
}

