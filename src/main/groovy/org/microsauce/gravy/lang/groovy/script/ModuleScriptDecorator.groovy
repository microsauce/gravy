package org.microsauce.gravy.lang.groovy.script

import static org.microsauce.gravy.util.PathUtil.*
import groovy.transform.CompileStatic

import org.microsauce.gravy.app.*
import org.microsauce.gravy.context.Context


class ModuleScriptDecorator extends ScriptDecorator {

	ModuleScriptDecorator(ConfigObject config, Context context) {
		super(config, context)
	}

	void decorate(Script script) { // TODO refactor this to GroovyModule and delete this class
		super.decorate(script)

		extractModule script.name
//		script.classLoader = getClassLoader()

		def (moduleFolder, moduleUri) = getModuleFolder(script.name, config.appRoot)
		script.sourceUri = moduleUri+"${SLASH}application.groovy"
		if (moduleFolder.exists()) {
			script.binding.config = modConfig config, script.name, moduleUri
			script.roots << moduleUri
			script.roots << moduleUri+"${SLASH}scripts"

			def libFolderUri = moduleUri+"${SLASH}lib"
			def libFolder = new File(libFolderUri)

			if (libFolder.exists()) {
				libFolder.eachFile { file ->
					script.classPathUris << file.absolutePath
				}
			}
			def classesFolderUri = moduleUri+"${SLASH}classes"
			def classesFolder = new File(classesFolderUri)
			if (classesFolder.exists()) {
				script.classPathUris << classesFolder.absolutePath
			}
		}

		script.binding << [
			run : { name, scriptBinding = null ->
				def subScript = new Script(
					[sourceUri: name+'.groovy', binding: [config:config, app:app], roots: script.roots, classLoader: script.classLoader])
				new ScriptDecorator(config, context).decorate(subScript)
				ScriptUtils.run(subScript)
			}
		]

	}

	def private extractModule(modName) {
		def modPath = "${config.appRoot}${SLASH}WEB-INF${SLASH}modules${SLASH}${modName}"
		def modFile = new File(modPath)
		def modJarPath = modPath+'.jar'
		def modJar = new File(modJarPath)

		if ( modJar.exists() ) {
			if ( modFile.exists() ) 
				modFile.deleteDir()

			def ant = new AntBuilder()			
			ant.unzip(src: "$modJarPath", dest: "$modPath")

			modJar.delete()
		}
	}

	def getModuleFolder(moduleName, appRoot) {
		def moduleUri = "${appRoot}${SLASH}WEB-INF${SLASH}modules${SLASH}$moduleName"
		def moduleFolder = new File(moduleUri)
		[moduleFolder, moduleUri]
	}

	def modConfig(appConfig, modName, moduleUri) {
		if ( modName == 'app' ) return appConfig

		def modConfig = appConfig[modName]
		def configFile = new File(moduleUri+'/conf/config.groovy')
		if ( configFile.exists() ) {
			modConfig = new ConfigSlurper(appConfig.env ?: 'prod').parse(configFile.toURL())
			modConfig = modConfig.merge(appConfig[modName])
		}
		else modConfig = appConfig[modName]

		modConfig.appRoot = appConfig.appRoot

		modConfig
	}

}