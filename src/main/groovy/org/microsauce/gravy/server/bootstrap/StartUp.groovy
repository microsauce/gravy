// TODO move this to correct folder - rename to Startup

package org.microsauce.gravy.server.bootstrap

import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.codehaus.groovy.control.CompilerConfiguration
import org.microsauce.gravy.ErrorHandler
import org.microsauce.gravy.app.ApplicationContext
import org.microsauce.gravy.app.config
import org.microsauce.gravy.app.GravyDecorator
import org.microsauce.gravy.app.AppBuilder
import org.microsauce.gravy.server.runtime.*
import org.microsauce.gravy.server.runtime.FilterWrapper
import org.microsauce.gravy.server.runtime.ServerFactory
import org.microsauce.gravy.server.runtime.ServletWrapper
import org.microsauce.util.CommandLine
import groovy.util.logging.Log4j
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.apache.log4j.*
import groovy.util.ConfigObject
import org.microsauce.gravy.app.*
import org.microsauce.gravy.dev.DevUtils


@Log4j
class StartUp {
	// TODO appRoot = / of deployment
// TODO appRoot - deploy path
// TODO resoureRoot - 
	def static main(args) {

		//
		// parse command line
		//
		def commandLine = new CommandLine(args)
		def environment = (commandLine.optionValue('env') ?: System.getProperty('gravy.env')) ?: 'dev'

		System.setProperty('gravy.env', environment)
		System.setProperty('gravy.devMode', 'true')

		def port = commandLine.optionValue 'port'
		def hostName = commandLine.optionValue 'host'
		def clConfig = commandLineEnv(commandLine)

		def projectPath = System.getProperty('user.dir')
		System.setProperty('gravy.appRoot', DevUtils.appDeployPath(projectPath) ) //System.getProperty('user.dir'))

		clConfig.each {key, value ->
			System.setProperty(key, value)
		}
		def config = config.getInstance(environment).get()

		//
		// start the application server
		//
		startApplicationServer(config)
	}

	def static commandLineEnv(commandLine) {
		def clConfiguration = [:]
		def clConf = commandLine.listOptionValue('conf')
		if(clConf.size() > 0) {
			for (option in clConf) {
				def keyValue = option.split('=')
				if (keyValue.length < 2) throw Exception("unable to parse command line configuration: -conf ${option}")
				clConfiguration[keyValue[0]] = keyValue[1]
			}
		}

		clConfiguration
	}

	def static startApplicationServer(ConfigObject config) {

		//
		// instantiate the server
		//
		def server = ServerFactory.getServer(config) 

		//
		// initialize the server
		//
		server.initialize()

		//
		// cleanup code
		//
		addShutdownHook {
		    log.info 'Server is shutting down . . .'
		    server.stop()
		    log.info 'Shutdown complete.'
		}

		server.start()
	}
}