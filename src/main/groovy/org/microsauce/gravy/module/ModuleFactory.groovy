package org.microsauce.gravy.module

import groovy.transform.CompileStatic

import org.apache.log4j.Logger
import org.codehaus.groovy.tools.LoaderConfiguration
import org.codehaus.groovy.tools.RootLoader
import org.microsauce.gravy.context.Context
import org.microsauce.gravy.context.ServiceFactory
import org.microsauce.gravy.lang.object.GravyType
import org.microsauce.gravy.module.groovy.GroovyModuleFactory
import org.microsauce.gravy.module.javascript.JSModuleFactory
import org.microsauce.gravy.module.ruby.RubyModuleFactory

abstract class ModuleFactory {

	Logger log = Logger.getLogger(ModuleFactory.class) 
	
	static Map FACTORY_TYPES = [
		'groovy' : GroovyModuleFactory.class,
		'js' :  JSModuleFactory.class,
		'coffee' : JSModuleFactory.class,
		'rb' : RubyModuleFactory.class
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
	
	private static void completeConfig(ConfigObject config) { // TODO move all this back to config
		def appRoot = System.getProperty('gravy.appRoot')
		config.appRoot 				= appRoot
		config.gravy.refresh		= false
		config.gravy.view.renderUri		= config.gravy.view.renderUri	 ?: '/view/gstring'
		config.gravy.view.documentRoot  = config.gravy.view.documentRoot ?: '/WEB-INF/view'
		config.gravy.view.errorUri		= config.gravy.view.errorUri	 ?: '/error'
//		config.gravy.serializeAttributes= config.gravy.serializeAttributes ?: true
		
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
		if ( appConfig ) {
			ConfigObject modConfig = (ConfigObject)appConfig[moduleFolder.name]
			if ( modConfig && modConfig.disabled ) return null
		}
		
		// create module classloader and instantiate the module object		
		ClassLoader cl = createModuleClassLoader(moduleFolder)
		Class moduleClass = cl.loadClass(moduleClassName())
		Module module = (Module) moduleClass.newInstance()
		
		// load module configuration
		module.moduleConfig = loadModuleConfig(moduleFolder, env)
		if ( appConfig != null && appConfig[moduleFolder.name] instanceof ConfigObject )
			module.config = module.moduleConfig.merge((ConfigObject)appConfig[moduleFolder.name])
		else module.config = module.moduleConfig
			
		// non-config properties
        module.type = type()
		module.context = context
		module.name = moduleFolder.name
		module.scriptFile = new File(moduleScriptName())
		module.isApp = isApp
		module.classLoader = cl
		module.folder = moduleFolder
		module.scriptFile = appScript
		module.serviceFactory = new ServiceFactory(module)

		// config properties
		ConfigObject gravyConfig = (ConfigObject)module.config.gravy
		ConfigObject gravyViewConfig = (ConfigObject) gravyConfig.view
		module.renderUri = gravyViewConfig.renderUri
		module.applicationConfig = appConfig
		module.errorUri = gravyViewConfig.errorUri

		module
	}
	
	@CompileStatic private ClassLoader createModuleClassLoader(File moduleFolder) {

		log.info "initialize ${moduleFolder.name} classloader . . ."
		List<URL> classpath = []
		LoaderConfiguration loaderConf = new LoaderConfiguration()
		
		if ( System.getProperty('gravy.devMode') ) {
			loaderConf.addFile(new File(System.getProperty('user.dir')+'/target/classes'))
		}	
		
		// module lib -- web-inf/moduleName/lib
		File modLib = new File(moduleFolder, 'lib')
		if ( modLib.exists() ) {
			modLib.eachFile { File thisLib ->
				log.info "\tadding ${thisLib.absolutePath} to classpath"
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

    /**
     * The module type
     */
	abstract GravyType type()
}
