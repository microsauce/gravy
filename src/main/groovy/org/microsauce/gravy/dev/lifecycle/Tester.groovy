package org.microsauce.gravy.dev.lifecycle

import groovy.util.GroovyTestSuite
import junit.framework.Test
import junit.textui.TestRunner
import org.codehaus.groovy.runtime.ScriptTestAdapter
import groovy.io.FileType

class Tester {

	def runTests(projectBasedir) {
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
	}


	private getTestScripts(testFolder) {
		def testScripts = []
		new File(testFolder).eachFileRecurse(FileType.FILES) { thisFile ->
			if (thisFile.name.endsWith('.groovy'))
				testScripts << thisFile.absolutePath
		}

		testScripts
	}

}