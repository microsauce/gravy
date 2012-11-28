package org.microsauce.gravy.module.config

import groovy.util.logging.Log4j

@Log4j
class Config {

	def private static instance
	
	def static getInstance(environment) { 
		
		if (!instance) instance = new Config(environment)
		return instance
	}

	def static getInstance() {
		if (!instance) new Exception("configuration not properly initialized")
		return instance
	}

	def private ConfigObject config

	private Config(environment) {
		def configFile = new File(System.getProperty('gravy.appRoot')+'/WEB-INF/modules/app/conf/config.groovy')
		try {
			if ( configFile.exists() ) 
				config = new ConfigSlurper(environment).parse(configFile.toURL())
		}
		catch (all) {
			all.printStackTrace()	
			log.error 'error loading environment.groovy', all
		}
		finally {
			if (!config) config = new ConfigObject()
		}

		if (System.getProperty('gravy.devMode')) 
			completeConfigDev config
		else 
			completeConfigWar config

		config.gravy.env = environment
	}

	def get() {config}

	def toProperties() {config.toProperties()}

	private void completeConfigDev(config) {
		def appRoot = System.getProperty('gravy.appRoot') 

		config.appRoot 				= appRoot

		config.jetty.contextPath 	= System.getProperty('jetty.contextPath') ?: config.jetty.contextPath ?: '/'
		config.jetty.port 			= System.getProperty('jetty.port') ?: config.tomcat.port ?: 8080
		config.jetty.host 			= System.getProperty('jetty.host') ?: config.tomcat.host ?: 'localhost'

		config.gravy.refresh		= System.getProperty('gravy.refresh') ?: config.gravy.refresh ?: true
		config.gravy.viewUri		= System.getProperty('gravy.viewUri') ?: config.gravy.viewUri ?: '/view/renderer'
		config.gravy.errorUri		= System.getProperty('gravy.errorUri') ?: config.gravy.errorUri ?: '/error'

		//
		// type conversions
		//
		config.jetty.port = config.jetty.port instanceof String ? Integer.parseInt(config.jetty.port) : config.jetty.port
	}

	private void completeConfigWar(config) {
		def appRoot = System.getProperty('gravy.appRoot')
		config.appRoot 				= appRoot
		config.gravy.viewUri		= config.gravy.viewUri ?: '/view/renderer'
		config.gravy.errorUri		= config.gravy.errorUri ?: '/error'
		config.gravy.refresh		= false

	}
}