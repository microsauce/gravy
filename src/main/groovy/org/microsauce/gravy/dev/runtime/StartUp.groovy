// TODO move this to correct folder - rename to Startup

package org.microsauce.gravy.dev.runtime

import groovy.util.logging.Log4j

import org.apache.log4j.*
import org.microsauce.gravy.app.*
import org.microsauce.gravy.dev.DevUtils
import org.microsauce.gravy.module.config.Config
import org.microsauce.gravy.server.runtime.*
import org.microsauce.gravy.util.CommandLine


@Log4j
class StartUp {

    def static main(args) {

        //
        // parse command line
        //
        def commandLine = new CommandLine(args)
        def environment = (commandLine.optionValue('env') ?: System.getProperty('gravy.env')) ?: 'dev'

        System.setProperty('gravy.env', environment)
        System.setProperty('gravy.devMode', 'true')

        def port = commandLine.optionValue 'port'
        if (port) System.setProperty('jetty.port', port)
        def host = commandLine.optionValue 'host'
        if (host) System.setProperty('jetty.host', host)
        def cp = commandLine.optionValue 'cp'
        if (cp) System.setProperty('jetty.cp', cp)
        def clConfig = commandLineEnv(commandLine)

        def projectPath = System.getProperty('user.dir')

        clConfig.each { key, value ->
            System.setProperty(key, value)
        }
        def config = Config.getInstance(environment).get()

        //
        // start the application server
        //
        startApplicationServer(config)
    }

    def static commandLineEnv(commandLine) {
        def clConfiguration = [:]
        def clConf = commandLine.listOptionValue('conf')
        if (clConf.size() > 0) {
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