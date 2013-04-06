package org.microsauce.gravy.dev.observer

import org.microsauce.gravy.module.config.ConfigLoader
import org.microsauce.gravy.app.script.*
import org.microsauce.gravy.module.Module
import groovy.transform.CompileStatic

class RedeploySourceModHandler implements SourceModHandler {

    Module app

    RedeploySourceModHandler(Module app) {
        this.app = app
    }

    @CompileStatic
    void handle() {
        try {
            // reload the config
            String environment = System.getProperty('gravy.env') ?: 'prod'
            ConfigLoader.reset()
            ConfigLoader configLoader = ConfigLoader.initInstance(environment, app.folder)
            app.config =  configLoader.appConfig

            // reload the module
            app.context.clearApplicationServices()
            app.load()
        }
        catch (all) {
            all.printStackTrace()
        }
    }
}