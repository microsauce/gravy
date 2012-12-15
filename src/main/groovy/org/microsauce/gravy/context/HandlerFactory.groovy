package org.microsauce.gravy.context

import org.microsauce.gravy.context.groovy.GroovyHandlerFactory
import org.microsauce.gravy.context.javascript.JSHandlerFactory
import org.microsauce.gravy.module.groovy.GroovyModule
import org.microsauce.gravy.module.javascript.JSModule

abstract class HandlerFactory {
	
	static Map<Class<? extends ServiceFactory>, HandlerFactory> HANDLER_FACTORIES;
	
	static {
		HANDLER_FACTORIES = [:]
		HANDLER_FACTORIES[GroovyModule.class] = new GroovyHandlerFactory()
		HANDLER_FACTORIES[JSModule.class] = new JSHandlerFactory()
	}
	
	static HandlerFactory getHandlerFactory(Class moduleClass) {
		HANDLER_FACTORIES[moduleClass]
	}
	
	abstract Handler makeHandler(Object rawHandler, Object scriptContext)
	
}
