package org.microsauce.gravy.module.config

import groovy.transform.CompileStatic

/**
 * @author microsauce
 */
class ConfigLoader {

    private static ConfigLoader instance

    static ConfigLoader initInstance(String env, File appFolder) {
        if ( !instance ) instance = new ConfigLoader(env, appFolder)
        instance
    }

    static ConfigLoader getInstance() {
        if ( !instance ) new RuntimeException('ERROR: ConfigLoader not initialized.')
        instance
    }

    ConfigObject appConfig
    String env

    private ConfigLoader(String env, File appFolder) {
        this.env = env
        loadAppConfig(appFolder)
    }

    @CompileStatic private ConfigObject loadAppConfig(File appFolder) {
        appConfig = loadConfig(appFolder)
        setDefaults(appConfig)
    }

    @CompileStatic ConfigObject loadModConfig(File moduleFolder) {
        ConfigObject modConfig = loadConfig(moduleFolder)
        if ( appConfig[moduleFolder.name] ) {
            modConfig = (ConfigObject)modConfig.merge((ConfigObject) appConfig[moduleFolder.name])
        }
        setDefaults(modConfig)
        modConfig
    }

    @CompileStatic private ConfigObject loadConfig(File modFolder) {
        ConfigObject configObject
        File configFile = new File(modFolder, 'conf.groovy')
        if ( configFile.exists() )
            configObject = new ConfigSlurper(env ?: 'prod').parse(configFile.toURI().toURL())
        else configObject = new ConfigObject()

        configObject
    }

    private void setDefaults(ConfigObject config) {
        // TODO modRoot, webRoot ???
        def appRoot = System.getProperty('gravy.appRoot')
        config.appRoot = appRoot
        config.gravy.view.renderUri = config.gravy.view.renderUri ?: '/view/freemarker'
        config.gravy.view.documentRoot = config.gravy.view.documentRoot ?: '/WEB-INF/view'
        config.gravy.view.errorUri = config.gravy.view.errorUri ?: '/error'

        if ( System.getProperty('gravy.devMode') ) {
            config.jetty.contextPath = System.getProperty('jetty.contextPath') ?: config.jetty.contextPath ?: '/'
            config.jetty.port = System.getProperty('jetty.port') ?: config.tomcat.port ?: 8080
            config.jetty.host = System.getProperty('jetty.host') ?: config.tomcat.host ?: 'localhost'
            config.jetty.port = config.jetty.port instanceof String ? Integer.parseInt(config.jetty.port) : config.jetty.port
        }
    }
}
