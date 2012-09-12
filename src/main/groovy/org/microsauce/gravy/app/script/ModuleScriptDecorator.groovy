package org.microsauce.gravy.app.script

import org.microsauce.gravy.app.*

class ModuleScriptDecorator extends ScriptDecorator {

	ModuleScriptDecorator(ConfigObject config, ApplicationContext app) {
		super(config, app)
	}

	void decorate(Script script) {
		super.decorate(script)

		extractModule script.name

		def (moduleFolder, moduleUri) = getModuleFolder(script.name, config.appRoot)
		script.sourceUri = moduleUri+'/module.groovy'
		if (moduleFolder.exists()) {
			script.roots << moduleUri
			script.roots << moduleUri+'/scripts'

			def libFolderUri = moduleUri+'/lib'
			def libFolder = new File(libFolderUri)

			if (libFolder.exists()) {
				libFolder.eachFile { file ->
					script.classPathUris << file.absolutePath
				}
			}
			def classesFolderUri = moduleUri+'/classes'
			def classesFolder = new File(classesFolderUri)
			if (classesFolder.exists()) {
				script.classPathUris << classesFolder.absolutePath
			}
		}

		script.binding << [
			run : { name, scriptBinding = null ->
				def subScript = new Script([sourceUri: name+'.groovy', binding: [config:config, app:app], roots: script.roots])
				new ScriptDecorator(config, app).decorate(subScript)
				ScriptUtils.run(subScript)
			}
		]

	}

	def private extractModule(modName) {
		def modPath = "${config.appRoot}/modules/${modName}"
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
		def moduleUri = appRoot+'/modules/'+moduleName
		def moduleFolder = new File(moduleUri)
		if (moduleFolder.exists()) return [moduleFolder, moduleUri]
		else {
			moduleUri = System.getenv()['GRAVY_HOME']+'/modules/'+moduleName 
			return [new File(moduleUri), moduleUri]
		}
	}

}