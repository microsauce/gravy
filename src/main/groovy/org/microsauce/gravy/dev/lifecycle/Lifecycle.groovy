package org.microsauce.gravy.dev.lifecycle

import groovy.util.GroovyTestSuite
import junit.framework.Test
import junit.textui.TestRunner
import org.codehaus.groovy.runtime.ScriptTestAdapter
import groovy.io.FileType
import java.nio.file.*
import java.nio.file.attribute.*
import org.microsauce.gravy.dev.DevUtils
import org.microsauce.gravy.dev.lifecycle.DependencyResolver
import org.codehaus.groovy.tools.RootLoader

class Lifecycle {

	private AntBuilder ant
	private DependencyResolver resolver
	private String projectBasedir
	private String gravyHome
	private String appName
	private String tempFolder
	private String deployFolder

	private String version
	def private managedLibs
	def private managedModules
	private String author
	private String description

	def private moduleNames = null

	Lifecycle(config) {

		projectBasedir = System.getProperty('user.dir')
		tempFolder = System.getProperty('java.io.tmpdir')

		def sysEnv = System.getenv()
		gravyHome = sysEnv['GRAVY_HOME']
		if (!gravyHome) {
			println 'BUILD FAILED: please set your GRAVY_HOME environment variable.'
			System.exit(1)
		}
		appName = new File(projectBasedir).name
		deployFolder = DevUtils.appDeployPath(projectBasedir) 

		ant = new AntBuilder()

		configure(config)
	}

	private configure(config) {
		config = config ?: new ConfigObject()
		version = config.meta.version				            // defaults to null
		author = config.meta.author								// defaults to null
		description = config.meta.description			 		// defaults to null

		// dependencies
		managedLibs = config.dependencies.libs  				// defaults to null

		managedModules = config.dependencies.modules 	        // defaults to null

		resolver = new DependencyResolver(projectBasedir)
	}

	private listModules() { // this will replace the gravy.modules config

		if ( moduleNames == null ) {
			moduleNames = [] as Set

			def modFolder = new File("${projectBasedir}/modules")
			def modulesFolder = modFolder
			modulesFolder.eachFile { thisFile ->
				if ( thisFile.isDirectory() )
					moduleNames << thisFile.name
			}

			if ( managedModules ) {
				managedModules.each { thisMod ->
					moduleNames << moduleName(thisMod)
				}
			}
		}

		moduleNames
	}

	void installCoreModule(coreModuleName) {
		println '========================================================================='
		println "= install core module: ${coreModuleName.padRight(49)}="
		println '========================================================================='

		ant.sequential {
			copy(todir:"${projectBasedir}/modules/${coreModuleName}") {
				if (exists("${gravyHome}/modules/${coreModuleName}")) {
			    	fileset(dir:"${gravyHome}/modules/${coreModuleName}") {
			    		include(name:'*/**')
			    	}
				} else {
					echo("module $coreModuleName not found")
				}
			}
		}
	}

	void clean() {
		println '========================================================================='
		println '= delete build artifacts                                                ='
		println '========================================================================='

		ant.sequential {
			delete(dir:"${projectBasedir}/target/classes")
		}
	}

	void cleanAll() {
		println '========================================================================='
		println '= delete ALL artifacts (build products and managed dependencies)        ='
		println '========================================================================='

		ant.sequential {
			delete(dir:"${projectBasedir}/target")
		}

		ant.echo "delete deployment folder $deployFolder"
		del deployFolder
	}

	void compile() {

		resolve()
		
		println '========================================================================='
		println '= compile java and groovy sources                                       ='
		println '========================================================================='

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

		compile()

		println '========================================================================='
		println '= execute test scripts                                                  ='
		println '========================================================================='

		def classLoader = testClassLoader()
		def testerClass = classLoader.loadClass('org.microsauce.gravy.dev.lifecycle.Tester')
		def tester = testerClass.newInstance()
		tester.runTests(projectBasedir)
	}

	private ClassLoader testClassLoader() {
		def urls = [] 
		def target = new File("${projectBasedir}/target/classes")
		if ( target.exists() ) {
			urls << new File("${projectBasedir}/target/classes").toURL()
		}
		target = new File("${projectBasedir}/target/lib")
		if ( target.exists() ) {
			new File("${projectBasedir}/target/lib").eachFile { thisFile ->
				urls << thisFile.toURI().toURL()
			}
		}
		new File("${gravyHome}/lib/junit").eachFile { thisFile ->
			urls << thisFile.toURI().toURL()
		}
		new File("${gravyHome}/lib/groovy").eachFile { thisFile ->
			urls << thisFile.toURI().toURL()
		}
		new File("${gravyHome}/lib").eachFile { thisFile ->
			if ( thisFile.name.endsWith('.jar') )
				urls << thisFile.toURI().toURL()
		}

		new RootLoader(urls as URL[], this.class.classLoader)
	}

	void resolve() { // resolve managed dependencies

		clean()

		println '========================================================================='
		println '= resolve dependencies                                                  ='
		println '========================================================================='

		if ( managedLibs && managedLibs.size() > 0 ) {
			managedLibs.each { thisLib ->
				def jar = jarName thisLib
				if ( !exists("${projectBasedir}/target/lib/${jar}") ) {
					resolver.installDependency thisLib, "${projectBasedir}/target/lib" 
					if ( exists("${projectBasedir}/lib") ) {
						ant.copy(todir:"${projectBasedir}/target/lib") {
							fileset(dir:"${projectBasedir}/lib") {
					    		include(name:'*/**')
							}
						} // ant.copy
					} // if exists lib
				}
			}
		}

		if ( managedModules && managedModules.size() > 0 ) {
			managedModules.each { thisMod ->
				def modName = moduleName thisMod
				if ( !exists("${projectBasedir}/target/modules/${modName}") )
					resolver.installModule thisMod
			}
		}
	}

	private String moduleName(String mavenCoordinates) {
		if ( mavenCoordinates == null || mavenCoordinates == '' ) return null
		validMavenCoordinates mavenCoordinates

		mavenCoordinates.split(':')[1]		
	}

	private String jarName(String mavenCoordinates) {
		if ( mavenCoordinates == null || mavenCoordinates == '' ) return null
		validMavenCoordinates mavenCoordinates

		String[] coords = mavenCoordinates.split(':')
		coords[1]+'-'+coords[2]+'.jar'
	}

	private void validMavenCoordinates(String mavenCoordinates) {
		if (mavenCoordinates == null || mavenCoordinates == '') return
		if ( !(mavenCoordinates ==~ /[a-zA-Z0-9\-]+:[a-zA-Z0-9\-]+:[0-9\.]+/) )
			throw new Exception("invalid maven coordinates ${mavenCoordinates}.  Valid coordinates are of the form group:artifactId:version")
	}

	/*
		deploy to the user tmp folder
	*/
	void assemble() {
		assemble(false)
	}

	private deploySrcArtifacts() {
		if ( exists(projectBasedir+'/target/classes') ) {
			ant.jar(destfile:"${deployFolder}/WEB-INF/modules/app/lib/${appName}.jar", basedir:"${projectBasedir}/target/classes") 
		}
	}

	private jarSrcArtifacts() {
		if ( exists(projectBasedir+'/target/classes') ) {
			ant.jar(destfile:"${projectBasedir}/lib/${appName}.jar", basedir:"${projectBasedir}/target/classes") 
		}
	}

	void assemble(boolean skipCompile) {

		if ( !skipCompile )
			compile()

		println '========================================================================='
		println '= assembling application                                                ='
		println '========================================================================='

		def modules = listModules()

		del deployFolder
		folder deployFolder+'/WEB-INF/view'
		folder deployFolder+'/WEB-INF/lib' 

		modules.each { mod ->
			linkMod mod
		}

		linkMod null // app
		deploySrcArtifacts()
		ant.copy(todir:"${deployFolder}/WEB-INF/lib", flatten:'true') {
			fileset(dir:"${gravyHome}/lib") {
		    		include(name:'*/**')
		    		exclude(name:'jetty8/**')
		    		exclude(name:'jnotify/**')
			}
		}
	}

	void deploy(String warName, String deployPath) {
		war(warName, deployPath)

		println '========================================================================='
		println '= deploying application                                                 ='
		println '========================================================================='

		def warPath = "${projectBasedir}/target/${warName}.war"
		ant.copy(file:"$warPath", todir:"$deployPath") 
	}

	private void linkMod(modName) {

		def managedModPath = modName ? projectBasedir+'/target/modules/'+modName : projectBasedir
		def projectModPath = modName ? projectBasedir+'/modules/'+modName : projectBasedir
		def coreModPath = modName ? gravyHome+'/modules/'+modName : projectBasedir
		def modPath = exists(managedModPath) ? managedModPath : projectModPath
		if ( modPath == projectModPath )
			modPath = exists(projectModPath) ? projectModPath : coreModPath
		modName = modName ?: 'app'

		def webInfMod = deployFolder+'/WEB-INF/modules/'+modName
		folder webInfMod

//		def appScriptPath = modPath+'/application.groovy'
//		if ( exists(appScriptPath) ) link webInfMod+'/application.groovy', appScriptPath
		new File(modPath).eachFile { file ->
			if ( file.name.startsWith('application.') ) {
				link webInfMod+'/'+file.name, file.absolutePath
			}
		}
		
		// copy deployment descriptor to deployment WEB-INF folder
		// TODO web.xml needs to be installed in webroot/WEB-INF on first startup
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
		File f = new File(file)
		if ( f.exists() ) {
			if ( f.isDirectory() )
				deleteFolder( file )
			else
				f.delete()
		}
	}

	private void deleteFolder(String folderPath) {
	    Path path = FileSystems.getDefault().getPath(folderPath)
	    DirectoryStream<Path> files = Files.newDirectoryStream( path )

	    if(files!=null) { 
	        for(Path entry in files) {
	            if(Files.isDirectory(entry) && !Files.isSymbolicLink(entry)) {
	                deleteFolder(entry.toFile().absolutePath)
	            } else {
	                Files.delete(entry)
	            }
	        }
	    }
	    Files.delete(path)
	}
	

	void war(warNm, skipTests = false) {

		def modules = listModules()
		def warName = warNm ?: appName

		if (!skipTests) if (!test()) return

		assemble true

		println '========================================================================='
		println '= bundle application as war                                             ='
		println '========================================================================='

		ant.sequential {

			def tempWar = deployFolder
			def warFile = "${projectBasedir}/target/${warName}.war"

			zip(destfile:"${warFile}", basedir:"${deployFolder}")
		}

	}

	void jarMod(modName) {
		def jarName = modName+(version ? '-'+version : '')+'.jar'

		println '========================================================================='
		println '= create module jar                                                     ='
		println '========================================================================='

		ant.jar(destfile:"${projectBasedir}/target/${jarName}") {
		    fileset(dir:"${projectBasedir}/modules/${name}") {
		        include(name:"application.groovy")
		        include(name:"view/**")
		        include(name:"webroot/**")
		        include(name:"conf/**")
		        include(name:"lib/**")
		        include(name:"scripts/**")
		    }
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

	String modIfyApp() {

		compile()

		println '========================================================================='
		println '= create application module jar                                         ='
		println '========================================================================='

		jarSrcArtifacts()
		def jarName = appName+(version ? '-'+version : '')+'.jar'
		def destFile = "${projectBasedir}/target/${jarName}"
		ant.jar(destfile: "${destFile}") {
		    fileset(dir:"${projectBasedir}") {
		        include(name:"application.groovy")
		        include(name:"view/**")
		        include(name:"webroot/**")
		        include(name:"conf/**")
		        include(name:"lib/**")
		        include(name:"scripts/**")
		    }
		}

		destFile.toString()
	}

	void publish(deployLocation) {
		def jarPath = modIfyApp()

		println '========================================================================='
		println '= publish application module jar                                        ='
		println '========================================================================='

		ant.copy(file:"${jarPath}", todir:"${deployLocation}")
	}

	private boolean exists(String path) {
		new File(path).exists()
	}
}