package org.microsauce.gravy.app

import org.microsauce.gravy.app.script.*
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import groovy.json.JsonBuilder
import groovy.util.logging.Log4j
import org.microsauce.gravy.*
import static com.microsauce.util.PathUtil.*

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
		loadModules()

		//
		// prepare and execute application.groovy
		//
		if (new File("${config.appRoot}${SLASH}application.groovy").exists()) {
			Script appScript = new Script([sourceUri:"${config.appRoot}${SLASH}application.groovy"])
			new ApplicationScriptDecorator(config, applicationContext).decorate(appScript)
			ScriptUtils.run(appScript, getClassLoader())
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
			for (modName in config.gravy.modules) {
				log.info "loading module $modName"
				def module = new Script([name:modName])
				moduleDecorator.decorate(module)
				results[modName] = ScriptUtils.run(module, getClassLoader())
				if ( results[modName] == applicationContext ) results[modName] = null

				applicationContext.modCache[modName] = results[modName]
			}
		}

		results
	}

	def private getClassLoader() {
		if ( !classLoader ) {
			if ( config.gravy.refresh ) 
				classLoader = new GravyDevModeClassLoader("${config.appRoot}${SLASH}target${SLASH}classes")
			else
				classLoader = this.getClass().getClassLoader()
		}

		classLoader
	}

}