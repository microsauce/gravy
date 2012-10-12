package org.microsauce.gravy.dev

import groovy.util.GroovyTestSuite
import junit.framework.Test
import junit.textui.TestRunner
import org.codehaus.groovy.runtime.ScriptTestAdapter
import groovy.io.FileType
import java.nio.file.*
import java.nio.file.attribute.*
import org.microsauce.gravy.dev.DevUtils

class Lifecycle {

	private AntBuilder ant
	private String projectBasedir
	private String gravyHome
	private String appName
	private String tempFolder
	private String deployFolder

	Lifecycle() {
		projectBasedir = System.getProperty('user.dir')
		tempFolder = System.getProperty('java.io.tmpdir')

		def sysEnv = System.getenv()
		gravyHome = sysEnv['GRAVY_HOME']
		if (!gravyHome) {
			println 'BUILD FAILED: please set your GRAVY_HOME environment variable.'
			System.exit(1)
		}
		appName = new File(projectBasedir).name
		deployFolder = DevUtils.appDeployPath(projectBasedir) // tempFolder+'/gravyDeployments/'+appName

		ant = new AntBuilder()
	}

	void clean() {
		println '========================================================================='
		println '= delete build artifacts                                                ='
		println '========================================================================='

		ant.sequential {
			delete(dir:"${projectBasedir}/target")
		}
	}

	void compile() {
		println '========================================================================='
		println '= compile java and groovy sources                                       ='
		println '========================================================================='

		clean()

		ant.sequential {
			mkdir(dir:"${projectBasedir}/target/classes")

			//
			// define classpath
			//
			echo 'build classpath'
			path(id:"build.classpath") {
				fileset(dir:"${gravyHome}/lib") {
					include(name:'**/*.jar')
				}
				if (exists("${projectBasedir}/lib")) {
					fileset(dir:"${projectBasedir}/lib") {
						include(name:'**/*.jar')
					}
				}
				pathelement(path:"${projectBasedir}/target/classes")
			}

			// compile java
			if (exists("${projectBasedir}/src/main/java")) {
				echo 'compile java'
				javac(
						destdir:"${projectBasedir}/target/classes",
						classpathref:"build.classpath",
						deprecation:"off") {
					src(path:"${projectBasedir}/src/main/java")
				}
			} else {
				echo 'no java sources found'
			}

			// compile groovy
			if (exists("${projectBasedir}/src/main/groovy")) {
				echo 'compile groovy'
				taskdef(
					name:'groovyc', 
					classname:'org.codehaus.groovy.ant.Groovyc', 
					classpathref:'build.classpath')
				groovyc(
					destdir:"${projectBasedir}/target/classes",
					srcdir:"${projectBasedir}/src/main/groovy",
					listfiles:'true') {
					classpath(refid:'build.classpath')
				}
			} else {
				echo 'no groovy sources found'
			}

			// copy resources
			if (exists("${projectBasedir}/src/main/resources")) {
			    copy(todir:"${projectBasedir}/target/classes") {
			    	fileset(dir:"${projectBasedir}/src/main/resources") {
			    		include(name:'*/**')
			    	}
			    }
		    }
		}
	}

	boolean test() {

		println '========================================================================='
		println '= execute test scripts                                                  ='
		println '========================================================================='

		compile()

		def allTests = new GroovyTestSuite()
		if ( exists("${projectBasedir}/src/test/groovy") ) {
			def testScripts = getTestScripts("${projectBasedir}/src/test/groovy")
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

	/*
		deploy to the system tmp folder
	*/
	void deploy(modules) {

		println '========================================================================='
		println '= deploy application                                                    ='
		println '========================================================================='

		compile()

		del deployFolder
		folder deployFolder+'/WEB-INF/view'
		folder deployFolder+'/WEB-INF/lib' // TODO hmm do I need this, load app like a mod - I DO need it for core jars (groovy etc)

		modules.each { mod ->
			linkMod mod
		}

		linkMod null  
		ant.copy(todir:"${deployFolder}/WEB-INF/lib", flatten:'true') {
			fileset(dir:"${gravyHome}/lib") {
		    		include(name:'*/**')
		    		exclude(name:'jetty8/**')
		    		exclude(name:'jnotify/**')
			}
		}
		if ( exists(projectBasedir+'/target/classes') ) 
			link deployFolder+'/WEB-INF/classes', projectBasedir+'/target/classes'
	}

	private void linkMod(modName) {

		def projectModPath = modName ? projectBasedir+'/modules/'+modName : projectBasedir
		def coreModPath = modName ? gravyHome+'/modules/'+modName : projectBasedir
		def modPath = exists(projectModPath) ? projectModPath : coreModPath
		modName = modName ?: 'app'

		def webInfMod = deployFolder+'/WEB-INF/modules/'+modName
		folder webInfMod

		def appScriptPath = modPath+'/application.groovy'
		if ( exists(appScriptPath) ) link webInfMod+'/application.groovy', appScriptPath

		// copy deployment descriptor to deployment WEB-INF folder
		//
		def webXml = gravyHome+'/bin/scripts/essentials/webroot/WEB-INF/web.xml'
		ant.copy(file:"$webXml", todir:"${deployFolder}/WEB-INF") 

		def webroot = modPath + '/webroot'
		if ( exists(webroot) ) {
			def modFolder = deployFolder+'/'+modName
			link modFolder, webroot
		}
		def viewroot = modPath+'/view'
		if ( exists(viewroot) ) {
			def viewFolder = deployFolder+'/WEB-INF/view'
			link viewFolder+'/'+modName, viewroot
		}
		def confroot = modPath+'/conf'
		if ( exists(confroot) ) {
			def confFolder = webInfMod+'/conf'
			link confFolder, confroot
		}
		def scriptsroot = modPath+'/scripts'
		if ( exists(scriptsroot) ) {
			def scriptsFolder = webInfMod+'/scripts'
			link scriptsFolder, scriptsroot
		}
		def libroot = modPath+'/lib'
		if ( exists(libroot) ) {
			def libFolder = webInfMod+'/lib'
			link libFolder, libroot
		}
	}

	private void link(link, target) {
		if ( exists(link) ) 
			new File(link).delete()

		Path linkPath   = FileSystems.getDefault().getPath(link)
		Path targetPath = FileSystems.getDefault().getPath(target)
		FileAttribute[] fileAttr = new FileAttribute[0]
		Files.createSymbolicLink(linkPath, targetPath, fileAttr)
	}

	private void folder(folder) {
		if ( exists(folder) ) 
			new File(folder).delete()

		new File(folder).mkdirs()
	}

	private void del(file) {
		if ( exists(file) ) 
			new File(file).delete()
	}

	void war() {
		war(null)
	}

	void war(warNm, modules, skipTests = false) {

		println '========================================================================='
		println '= bundle application as war                                             ='
		println '========================================================================='

		deploy(modules)

		def warName = warNm ?: appName

		if (!skipTests) if (!test()) return

		ant.sequential {

			def tempWar = deployFolder
			def warFile = "${projectBasedir}/target/${warName}.war"

			zip(destfile:"${warFile}", basedir:"${tempWar}")
		}

	}

	void jarMod(modName) {

		if ( exists("${projectBasedir}/modules/${modName}") ) {
			def tempJar = "${projectBasedir}/target/${modName}"

			clean()

			ant.sequential {
				mkdir(dir:"${tempJar}")
			    copy(todir:"${tempJar}") {
			    	fileset(dir:"${projectBasedir}/modules/${modName}") {
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

		if (!exists("${projectBasedir}/application.groovy")) {
			println "application.groovy must be defined"
			return
		}

		if (exists("${projectBasedir}/view") || exists("${projectBasedir}/webroot")) {
			println "please note: view and webroot are not included in module jar"
		}

		compile()
		ant.sequential {
			// rename application.groovy as module.groovy
			def tempJar = "${projectBasedir}/target/${jarName}"
			if (exists("${tempJar}")) delete(dir:"${tempJar}")
			if (exists("${tempJar}.jar")) delete(dir:"${tempJar}.jar")

			mkdir(dir:"${tempJar}")

			// copy root script
		    copy(file:"${projectBasedir}/application.groovy", tofile:"${tempJar}/module.groovy")

		    // class files and resources
		    if (new File("${projectBasedir}/target/classes").exists()) {
			    mkdir(dir:"${tempJar}/classes")
			    copy(todir:"${tempJar}/classes") {
			    	fileset(dir:"${projectBasedir}/target/classes") {
			    		include(name:'*/**')
			    	}
			    }
		    }

		    // scripts
		    if (new File("${projectBasedir}/scripts").exists()) {
			    mkdir(dir:"${tempJar}/scripts")
			    copy(todir:"${tempJar}/scripts") {
			    	fileset(dir:"${projectBasedir}/scripts") {
			    		include(name:'*/**')
			    	}
			    }
		    }

		    // libraries
		    if (new File("${projectBasedir}/lib").exists()) {
			    mkdir(dir:"${tempJar}/lib")
			    copy(todir:"${tempJar}/lib") {
			    	fileset(dir:"${projectBasedir}/lib") {
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