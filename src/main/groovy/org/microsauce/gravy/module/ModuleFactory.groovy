package org.microsauce.gravy.module

import groovy.transform.CompileStatic

import org.codehaus.groovy.tools.LoaderConfiguration
import org.codehaus.groovy.tools.RootLoader
import org.microsauce.gravy.context.Context
import org.microsauce.gravy.context.ServiceFactory
import org.microsauce.gravy.module.groovy.GroovyModuleFactory
import org.microsauce.gravy.module.javascript.JSModuleFactory
import org.microsauce.gravy.runtime.GravyTemplateServlet


abstract class ModuleFactory {

	static Map FACTORY_TYPES = [
		'groovy' : GroovyModuleFactory.class,
		'js' :  JSModuleFactory.class //,
		//'coffee' : CoffeeModuleFactory.class
	]
	
	@CompileStatic
	static ModuleFactory getInstance(String type) {
		Class moduleClass = FACTORY_TYPES[type]
		(ModuleFactory)moduleClass.newInstance()
	}
	
	@CompileStatic
	static ConfigObject loadModuleConfig(File moduleFolder, String env) {
		// TODO decide whether or not to locate the config file in the config folder or in the root module folder
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
		if (System.getProperty('gravy.devMode')) {
			def appRoot = System.getProperty('gravy.appRoot')
	
			config.appRoot 				= appRoot
	
			config.jetty.contextPath 	= System.getProperty('jetty.contextPath') ?: config.jetty.contextPath ?: '/'
			config.jetty.port 			= System.getProperty('jetty.port') ?: config.tomcat.port ?: 8080
			config.jetty.host 			= System.getProperty('jetty.host') ?: config.tomcat.host ?: 'localhost'
	
			config.gravy.refresh		= System.getProperty('gravy.refresh') ?: config.gravy.refresh ?: true
//			config.gravy.viewUri		= System.getProperty('gravy.viewUri') ?: config.gravy.viewUri ?: '/view/renderer'
//			config.gravy.errorUri		= System.getProperty('gravy.errorUri') ?: config.gravy.errorUri ?: '/error'
	
			//
			// type conversions
			//
			config.jetty.port = config.jetty.port instanceof String ? Integer.parseInt(config.jetty.port) : config.jetty.port
		} else {
			def appRoot = System.getProperty('gravy.appRoot')
			config.appRoot 				= appRoot
			config.gravy.viewUri		= config.gravy.viewUri ?: '/view/renderer'
			config.gravy.errorUri		= config.gravy.errorUri ?: '/error'
			config.gravy.refresh		= false
		}
	}

	
	
	@CompileStatic
	Module createModule(Context context, File moduleFolder, ConfigObject appConfig, String env, Boolean isApp) {

		// create classloader and load module class		
		ClassLoader cl = createModuleClassLoader(moduleFolder)
		Class moduleClass = cl.loadClass(moduleClassName())
		Module module = (Module) moduleClass.newInstance()
		
		// initialize module
		module.context = context
		module.name = moduleFolder.name
		module.scriptFile = new File(moduleScriptName())
		module.isApp = isApp
		module.classLoader = cl
		module.folder = moduleFolder
		module.moduleConfig = loadModuleConfig(moduleFolder, env)
		module.applicationConfig = appConfig
		module.viewRoots = GravyTemplateServlet.roots

		if ( appConfig != null && appConfig[module.name] instanceof ConfigObject )
			module.config = module.moduleConfig.merge((ConfigObject)appConfig[module.name])
		else
			module.config = module.moduleConfig
		// module.bindings 
		module.serviceFactory = ServiceFactory.getFactory(module.class)
		module
	}
	
	@CompileStatic
	private ClassLoader createModuleClassLoader(File moduleFolder) {
		
		List<URL> classpath = []
		LoaderConfiguration loaderConf = new LoaderConfiguration()
		// module lib -- web-inf/moduleName/lib
		File modLib = new File(moduleFolder, 'lib')
		if ( modLib.exists() ) {
			modLib.eachFile { File thisLib ->
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
