package org.microsauce.gravy.context

import org.jruby.RubyModule
import org.microsauce.gravy.context.groovy.GroovyHandlerFactory
import org.microsauce.gravy.context.javascript.JSHandlerFactory
import org.microsauce.gravy.context.ruby.RubyHandlerFactory
import org.microsauce.gravy.module.groovy.GroovyModule
import org.microsauce.gravy.module.javascript.JSModule

abstract class HandlerFactory {

    static Map<Class<? extends ServiceFactory>, HandlerFactory> HANDLER_FACTORIES;

    static {
        HANDLER_FACTORIES = [:]
        HANDLER_FACTORIES[GroovyModule.class.name] = new GroovyHandlerFactory()
        HANDLER_FACTORIES[JSModule.class.name] = new JSHandlerFactory()
        HANDLER_FACTORIES[org.microsauce.gravy.module.ruby.RubyModule.class.name] = new RubyHandlerFactory()
    }

    static HandlerFactory getHandlerFactory(String moduleClassName) {
        HANDLER_FACTORIES[moduleClassName]
    }

    abstract Handler makeHandler(Object rawHandler, Object scriptContext)

}
