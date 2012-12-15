package org.microsauce.gravy.module

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j

import org.codehaus.groovy.tools.LoaderConfiguration
import org.codehaus.groovy.tools.RootLoader
import org.microsauce.gravy.context.Context
import org.microsauce.gravy.context.ServiceFactory
import org.microsauce.gravy.module.groovy.GroovyModuleFactory
import org.microsauce.gravy.module.javascript.JSModuleFactory

//@Log4j // TODO for some reason I get compile errors with this annotation
abstract class ModuleFactory {

	static Map FACTORY_TYPES = [
		'groovy' : GroovyModuleFactory.class,
		'js' :  JSModuleFactory.class,
		'coffee' : JSModuleFactory.class
	]
	
	@CompileStatic static ModuleFactory getInstance(String type) {
		Class moduleClass = FACTORY_TYPES[type]
		(ModuleFactory)moduleClass.newInstance()
	}
	
	@CompileStatic 	static ConfigObject loadModuleConfig(File moduleFolder, String env) {
		File configFolder = new File(moduleFolder, 'conf')
		File configFile = new File(configFolder, 'config.groovy')
		ConfigObject modConfig = null
		if ( configFile.exists() ) {
			modConfig = new ConfigSlurper(env ?: 'prod').parse(configFile.toURI().toURL())
		}
			
		modConfig = modConfig ?: new ConfigObject()
		completeConfig modConfig
		modConfig
	}
	
	private static void completeConfig(ConfigObject config) {
		def appRoot = System.getProperty('gravy.appRoot')
		config.appRoot 				= appRoot
		config.gravy.refresh		= false
		config.gravy.view.renderUri		= config.gravy.view.renderUri	 ?: '/view/gstring'
		config.gravy.view.documentRoot  = config.gravy.view.documentRoot ?: '/WEB-INF/view'
		config.gravy.view.errorUri		= config.gravy.view.errorUri	 ?: '/error'
		config.gravy.serializeAttributes= config.gravy.serializeAttributes ?: true
		
		if (System.getProperty('gravy.devMode')) {
			config.gravy.refresh		= true // TODO is this used ??? 
			config.jetty.contextPath 	= System.getProperty('jetty.contextPath') ?: config.jetty.contextPath ?: '/'
			config.jetty.port 			= System.getProperty('jetty.port') ?: config.tomcat.port ?: 8080
			config.jetty.host 			= System.getProperty('jetty.host') ?: config.tomcat.host ?: 'localhost'
			config.jetty.port = config.jetty.port instanceof String ? Integer.parseInt(config.jetty.port) : config.jetty.port
		}
	}
	
	@CompileStatic
	Module createModule(Context context, File moduleFolder, File appScript, ConfigObject appConfig, String env, Boolean isApp) {

		//
		// disable a module without un-installing/deleting it
		//
		if ( appConfig && ((ConfigObject)appConfig[moduleFolder.name]).disabled == true ) return null
		
		// create classloader and load module class		
		ClassLoader cl = createModuleClassLoader(moduleFolder)
		Class moduleClass = cl.loadClass(moduleClassName())
		Module module = (Module) moduleClass.newInstance()
		
		// initialize module
		module.moduleConfig = loadModuleConfig(moduleFolder, env)
		if ( appConfig != null && appConfig[moduleFolder.name] instanceof ConfigObject ) {
			module.config = module.moduleConfig.merge((ConfigObject)appConfig[moduleFolder.name])
		}	
		else
			module.config = module.moduleConfig
			
		// non-config values
		module.context = context
		module.name = moduleFolder.name
		module.scriptFile = new File(moduleScriptName())
		module.isApp = isApp
		module.classLoader = cl
		module.folder = moduleFolder
		
		// config values
		ConfigObject gravyConfig = (ConfigObject)module.config.gravy
		ConfigObject gravyViewConfig = (ConfigObject) gravyConfig.view
		module.renderUri = gravyViewConfig.renderUri
		module.applicationConfig = appConfig
		module.errorUri = gravyViewConfig.errorUri
		module.serializeAttributes = gravyConfig.serializeAttributes

		module.scriptFile = appScript
		module.serviceFactory = new ServiceFactory(module)

		module
	}
	
	@CompileStatic private ClassLoader createModuleClassLoader(File moduleFolder) {

		List<URL> classpath = []
		LoaderConfiguration loaderConf = new LoaderConfiguration()
		
		if ( System.getProperty('gravy.devMode') ) {
			loaderConf.addFile(new File(System.getProperty('user.dir')+'/target/classes'))
		}	
		
		// module lib -- web-inf/moduleName/lib
		File modLib = new File(moduleFolder, 'lib')
		if ( modLib.exists() ) {
			modLib.eachFile { File thisLib ->
				println "adding ${thisLib.absolutePath} to classpath"
				loaderConf.addFile thisLib
			}
		}
		new RootLoader(loaderConf)
	}
	
	/**
	 * The module subclass to instantiate and initialize
	 * 
	 * @return
	 */
	abstract String moduleClassName()
	
	/**
	 * The module script name
	 * 
	 * @return
	 */
	abstract String moduleScriptName()
	
}
