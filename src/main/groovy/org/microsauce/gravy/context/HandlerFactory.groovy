package org.microsauce.gravy.context

import org.microsauce.gravy.context.groovy.GroovyHandlerFactory
import org.microsauce.gravy.context.groovy.GroovyServiceFactory
import org.microsauce.gravy.context.javascript.JSHandlerFactory
import org.microsauce.gravy.context.javascript.JSServiceFactory

abstract class HandlerFactory {
	
	static Map<Class<? extends ServiceFactory>, HandlerFactory> HANDLER_FACTORIES;
	
	static {
		HANDLER_FACTORIES = [:]
		HANDLER_FACTORIES[GroovyServiceFactory.class] = new GroovyHandlerFactory()
		HANDLER_FACTORIES[JSServiceFactory.class] = new JSHandlerFactory()
	}
	
	static HandlerFactory getHandlerFactory(Class serviceFactory) {
		HANDLER_FACTORIES[serviceFactory]
	}
	
	abstract Handler makeHandler(Object rawHandler, Object scriptContext)
	
}
