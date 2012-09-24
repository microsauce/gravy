package org.microsauce.gravy.dev

import groovy.util.GroovyTestSuite
import junit.framework.Test
import junit.textui.TestRunner
import org.codehaus.groovy.runtime.ScriptTestAdapter
import groovy.io.FileType

class Lifecycle {

	private AntBuilder ant
	private String basedir
	private String gravyHome
	private String appName

	Lifecycle() {
		basedir = System.getProperty('user.dir')
//		System.setProperty('includeantruntime', 'false')
//		def configFile = new File("${basedir}/conf/config.groovy")
//println "configFile $configFile"		
//		if ( configFile.exists() ) {
//			def conf = new ConfigSlurper().parse(configFile.toURL())
//			modules = conf.gravy.modules ?: []
//		}
//		else
//			modules = []

		def sysEnv = System.getenv()
		gravyHome = sysEnv['GRAVY_HOME']
		if (!gravyHome) {
			println 'BUILD FAILED: please set your GRAVY_HOME environment variable.'
			System.exit(1)
		}
		appName = new File(basedir).name

		ant = new AntBuilder()
	}

	void clean() {
		ant.sequential {
			delete(dir:"${basedir}/target")
		}
	}

	void compile() {
		clean()

		ant.sequential {
			mkdir(dir:"${basedir}/target/classes")

			//
			// define classpath
			//
			echo 'build classpath'
			path(id:"build.classpath") {
				fileset(dir:"${gravyHome}/lib") {
					include(name:'**/*.jar')
				}
				if (exists("${basedir}/lib")) {
					fileset(dir:"${basedir}/lib") {
						include(name:'**/*.jar')
					}
				}
				pathelement(path:"${basedir}/target/classes")
			}

			// compile java
			if (exists("${basedir}/src/main/java")) {
				echo 'compile java'
				javac(
						destdir:"${basedir}/target/classes",
						classpathref:"build.classpath",
						deprecation:"off") {
					src(path:"${basedir}/src/main/java")
				}
			} else {
				echo 'no java sources found'
			}

			// compile groovy
			if (exists("${basedir}/src/main/groovy")) {
				echo 'compile groovy'
				taskdef(
					name:'groovyc', 
					classname:'org.codehaus.groovy.ant.Groovyc', 
					classpathref:'build.classpath')
				groovyc(
					destdir:"${basedir}/target/classes",
					srcdir:"${basedir}/src/main/groovy",
					listfiles:'true') {
					classpath(refid:'build.classpath')
				}
			} else {
				echo 'no groovy sources found'
			}

			// copy resources
			if (exists("${basedir}/src/main/resources")) {
			    copy(todir:"${basedir}/target/classes") {
			    	fileset(dir:"${basedir}/src/main/resources") {
			    		include(name:'*/**')
			    	}
			    }
		    }
		}
	}

	boolean test() {

		compile()

		def allTests = new GroovyTestSuite()
		if ( exists("${basedir}/src/test/groovy") ) {
			def testScripts = getTestScripts("${basedir}/src/test/groovy")
			testScripts.each { thisScript ->
				allTests.addTest(new
					ScriptTestAdapter(allTests.compile(thisScript), [] as String[]))
			}

			if (allTests.countTestCases() < 1) return 0

			TestRunner.run(allTests).wasSuccessful()
		} else {
			ant.echo "there are no unit test scripts defined for application $appName"
			return true
		}
	}

	def private getTestScripts(testFolder) {
		def testScripts = []
		new File(testFolder).eachFileRecurse(FileType.FILES) { thisFile ->
			if (thisFile.name.endsWith('.groovy'))
				testScripts << thisFile.absolutePath
		}

		testScripts
	}

	void war() {
		war(null)
	}

	void war(warNm, modules, skipTests = false) {

		def warName = warNm ?: appName

		compile()
		if (!skipTests) if (!test()) return

		ant.sequential {

			def tempWar = "${basedir}/target/${warName}"
			if (exists("${tempWar}")) delete(dir:"${tempWar}")
			if (exists("${tempWar}.war")) delete(dir:"${tempWar}.war")

			mkdir(dir:"${tempWar}")
			if (exists("${basedir}/webroot")) {
			    copy(todir:"${tempWar}") {
			    	fileset(dir:"${basedir}/webroot") {
			    		include(name:'*/**')
			    	}
			    }
			}

		    if ( !exists("${basedir}/webroot/WEB-INF/web.xml") ) {
		    	copy(todir:"${tempWar}") {
		    		fileset(dir:"${gravyHome}/bin/scripts/essentials/webroot") {
		    			include(name:'WEB-INF/**')
		    		}
		    	}
		    }

		    copy(todir:"${tempWar}/WEB-INF") {
		    	fileset(dir:"${basedir}") {
		    		include(name:'scripts/**')
		    		include(name:'view/**')
		    		include(name:'conf/**')
		    		include(name:'modules/**')
		    		include(name:'application.groovy')
		    	}
		    }
		    copy(file:"${basedir}/application.groovy", todir:"${tempWar}/WEB-INF")

		    // class files and resources
		    mkdir(dir:"${tempWar}/WEB-INF/lib")
		    mkdir(dir:"${tempWar}/WEB-INF/classes")
		    if (new File("${basedir}/target/classes").exists()) {
			    copy(todir:"${tempWar}/WEB-INF/classes") {
			    	fileset(dir:"${basedir}/target/classes") {
			    		include(name:'*/**')
			    	}
			    }
		    }
		    // libraries
		    if (new File("${basedir}/lib").exists()) {
			    copy(todir:"${tempWar}/WEB-INF/lib") {
			    	fileset(dir:"${basedir}/lib") {
			    		include(name:'*/**')
			    	}
			    }
		    }
			copy(todir:"${tempWar}/WEB-INF/lib", flatten:'true') {
		    	fileset(dir:"${gravyHome}/lib") {
		    		include(name:'*/**')
		    		exclude(name:'jetty8/**')
		    		exclude(name:'jnotify/**')
		    	}
		    }

		    // copy configured core-modules
			if ( modules.size() > 0 ) {

				for (coreMod in ['gstring', 'freemarker', 'scalate']) {
				    if ( modules.contains(coreMod) ) {
						echo "copy $coreMod module"				    	
						copy(todir:"${tempWar}/WEB-INF/modules") {
							fileset(dir:"${gravyHome}/modules") {
					    		include(name:"${coreMod}/**")
					    	}
						}
				    }
			    }
			}

			zip(destfile:"${tempWar}.war", basedir:"${tempWar}")
			delete(dir:"${tempWar}")
		}

	}

	void jarMod(modName) {

		if ( exists("${basedir}/modules/${modName}") ) {
			def tempJar = "${basedir}/target/${modName}"

			clean()

			ant.sequential {
				mkdir(dir:"${tempJar}")
			    copy(todir:"${tempJar}") {
			    	fileset(dir:"${basedir}/modules/${modName}") {
			    		include(name:'*/**')
			    	}
			    }

			    zip(destfile:"${tempJar}.jar", basedir:"${tempJar}")
				delete(dir:"${tempJar}")
			}

		} else {
			println "module '${modName}' does not exist"
		}

	}

	void appToMod(jrName) {

		def jarName = jrName ?: appName

		if (!exists("${basedir}/application.groovy")) {
			println "a jar-able requires application.groovy be defined"
			return
		}

		if (exists("${basedir}/view") || exists("${basedir}/webroot")) {
			println "please note: view and webroot are not included in module jar"
		}

		compile()
		ant.sequential {
			// rename application.groovy as module.groovy
			def tempJar = "${basedir}/target/${jarName}"
			if (exists("${tempJar}")) delete(dir:"${tempJar}")
			if (exists("${tempJar}.jar")) delete(dir:"${tempJar}.jar")

			mkdir(dir:"${tempJar}")

			// copy root script
		    copy(file:"${basedir}/application.groovy", tofile:"${tempJar}/module.groovy")

		    // class files and resources
		    if (new File("${basedir}/target/classes").exists()) {
			    mkdir(dir:"${tempJar}/classes")
			    copy(todir:"${tempJar}/classes") {
			    	fileset(dir:"${basedir}/target/classes") {
			    		include(name:'*/**')
			    	}
			    }
		    }

		    // scripts
		    if (new File("${basedir}/scripts").exists()) {
			    mkdir(dir:"${tempJar}/scripts")
			    copy(todir:"${tempJar}/scripts") {
			    	fileset(dir:"${basedir}/scripts") {
			    		include(name:'*/**')
			    	}
			    }
		    }

		    // libraries
		    if (new File("${basedir}/lib").exists()) {
			    mkdir(dir:"${tempJar}/lib")
			    copy(todir:"${tempJar}/lib") {
			    	fileset(dir:"${basedir}/lib") {
			    		include(name:'*/**')
			    	}
			    }
		    }

			zip(destfile:"${tempJar}.jar", basedir:"${tempJar}")
			delete(dir:"${tempJar}")
		}
	}

	void createApp(appName, example = false) {
		if ( appName == null ) {
			println 'please specify an application name'
			return
		}

		def sourceFolder = example ? 
			"${gravyHome}/bin/scripts/exampleApp" : "${gravyHome}/bin/scripts/emptyApp"

		// copy app folder to ./${appName}
		ant.sequential {
		    mkdir(dir:"./${appName}")
		    copy(todir:"./${appName}") {
		    	fileset(dir:"${sourceFolder}") {
		    		include(name:'*/**')
		    	}
		    }
		}
	}

	void createMod(modName) {
		if ( modName == null ) {
			println 'please specify a module name'
			return
		}

		def sourceFolder = "${gravyHome}/bin/scripts/emptyMod"

		// copy module folder to ./${modName}
		ant.sequential {
		    mkdir(dir:"./modules/${modName}")
		    copy(todir:"./modules/${modName}") {
		    	fileset(dir:"${sourceFolder}") {
		    		include(name:'*/**')
		    	}
		    }
		}
	}

	private boolean exists(String path) {
		new File(path).exists()
	}
}