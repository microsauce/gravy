package org.microsauce.gravy.module.config

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j


// TODO delete me
@Log4j
class Config {

    private static Config instance

    @CompileStatic
    static Config getInstance(String environment) {

        if (!instance) instance = new Config(environment)
        return instance
    }

    @CompileStatic
    static Config getInstance() {
        if (!instance) new Exception("configuration not properly initialized")
        return instance
    }

    def private ConfigObject config

    private Config(String environment) {
        init(environment)
    }

    @CompileStatic
    private void init(String environment) {
        File configFile = new File(System.getProperty('gravy.appRoot') + '/WEB-INF/modules/app/conf/config.groovy')
        try {
            if (configFile.exists())
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

    }

    @CompileStatic
    ConfigObject get() { config }

    @CompileStatic
    Properties toProperties() { config.toProperties() }

    private void completeConfigDev(config) {
        def appRoot = System.getProperty('gravy.appRoot')

        config.appRoot = appRoot

        config.jetty.contextPath = System.getProperty('jetty.cp') ?: config.jetty.contextPath ?: '/'
        config.jetty.port = System.getProperty('jetty.port') ?: config.jetty.port ?: 8080
        config.jetty.host = System.getProperty('jetty.host') ?: config.jetty.host ?: 'localhost'

        config.gravy.refresh = System.getProperty('gravy.refresh') ?: config.gravy.refresh ?: true
        config.gravy.viewUri = System.getProperty('gravy.viewUri') ?: config.gravy.viewUri ?: '/view/renderer'
        config.gravy.errorUri = System.getProperty('gravy.errorUri') ?: config.gravy.errorUri ?: '/error'

        //
        // type conversions
        //
        config.jetty.port = config.jetty.port instanceof String ? Integer.parseInt(config.jetty.port) : config.jetty.port
    }

    private void completeConfigWar(config) {
        def appRoot = System.getProperty('gravy.appRoot')
        config.appRoot = appRoot
        config.gravy.viewUri = config.gravy.viewUri ?: '/view/renderer'
        config.gravy.errorUri = config.gravy.errorUri ?: '/error'
        config.gravy.refresh = false

    }
}