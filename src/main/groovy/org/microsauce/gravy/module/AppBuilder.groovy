package org.microsauce.gravy.module

import org.microsauce.gravy.app.script.*
import org.microsauce.gravy.context.ApplicationContext;
import org.microsauce.gravy.util.script.groovy.ModuleScriptDecorator;
import org.microsauce.gravy.util.script.groovy.Script;
import org.microsauce.gravy.util.script.groovy.ScriptUtils;
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import groovy.json.JsonBuilder
import groovy.util.logging.Log4j
import org.microsauce.gravy.*
import static org.microsauce.gravy.util.PathUtil.*

@Log4j
class AppBuilder {

	def config
	def roots
	def compilerConfig
	def applicationContext
	def scriptContext
	def classLoader

	AppBuilder(config) {
		this.config = config
		applicationContext = ApplicationContext.getInstance(config)
	}

	def buildContext() {
		
		//
		// load modules listed in the gravy configuration
		//
		def moduleResults = loadModules()

		//
		// prepare and execute application.groovy
		//

		if (new File("${config.appRoot}${SLASH}WEB-INF${SLASH}modules${SLASH}app${SLASH}application.groovy").exists()) {
			Script appScript = new Script([name: 'app',sourceUri:"${config.appRoot}${SLASH}WEB-INF${SLASH}modules${SLASH}app${SLASH}application.groovy"])
			new ModuleScriptDecorator(config, applicationContext).decorate(appScript)
			appScript.binding.putAll moduleResults
			ScriptUtils.run(appScript)
		}

		//
		// finalize the context
		//
		applicationContext.complete()
		applicationContext
	}

	def private loadModules() {
		def results = [:]
		if (config.gravy.modules) {
			def moduleDecorator = new ModuleScriptDecorator(config, applicationContext)
		
			for (modName in listModules()) { 
				log.info "loading module $modName"
				def module = new Script([name:modName])
				moduleDecorator.decorate(module)
				results[modName] = ScriptUtils.run(module) 
				if ( results[modName] == applicationContext ) results[modName] = null

				applicationContext.modCache[modName] = results[modName]
			}
		}

		results
	}

	def private listModules() {
		// return all except 'app'
		def modulesFolder = new File("${config.appRoot}${SLASH}WEB-INF${SLASH}modules")
		def moduleNames = []
		modulesFolder.eachDir { dir ->
			if ( dir.name != 'app' ) moduleNames << dir.name
		}
		moduleNames
	}

}