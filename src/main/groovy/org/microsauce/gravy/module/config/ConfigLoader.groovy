package org.microsauce.gravy.module.config

import groovy.transform.CompileStatic

/**
 * @author microsauce
 */
class ConfigLoader {

    private static ConfigLoader instance

    static ConfigLoader initInstance(String env, File appFolder) {
        instance = new ConfigLoader(env, appFolder)
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
println "1 modConfig: $modConfig"
        if ( appConfig[moduleFolder.name] ) {
println "2"
            modConfig = (ConfigObject)modConfig.merge((ConfigObject) appConfig[moduleFolder.name])
println "3"
        }
println "4 modConfig: $modConfig"
        setDefaults(modConfig)
println "5 modConfig: $modConfig"
        modConfig
    }

    @CompileStatic private ConfigObject loadConfig(File modFolder) {
        ConfigObject configObject
        File configFile = new File(modFolder, 'conf.groovy')
println "configFile: $configFile"
println "exists: ${configFile.exists()}"
        if ( configFile.exists() )
            configObject = new ConfigSlurper(env ?: 'prod').parse(configFile.toURI().toURL())
        else configObject = new ConfigObject()

        configObject
    }

    private void setDefaults(ConfigObject config) {
        def appRoot = System.getProperty('gravy.appRoot')
        config.appRoot = appRoot
        config.gravy.refresh = false
        config.gravy.view.renderUri = config.gravy.view.renderUri ?: '/view/freemarker'
        config.gravy.view.documentRoot = config.gravy.view.documentRoot ?: '/WEB-INF/view'
        config.gravy.view.errorUri = config.gravy.view.errorUri ?: '/error'

        if ( System.getProperty('gravy.devMode') ) {
            config.gravy.refresh = true // TODO is this used ???
            config.jetty.contextPath = System.getProperty('jetty.contextPath') ?: config.jetty.contextPath ?: '/'
            config.jetty.port = System.getProperty('jetty.port') ?: config.tomcat.port ?: 8080
            config.jetty.host = System.getProperty('jetty.host') ?: config.tomcat.host ?: 'localhost'
            config.jetty.port = config.jetty.port instanceof String ? Integer.parseInt(config.jetty.port) : config.jetty.port
        }
    }
}
