package org.microsauce.gravy.context

import groovy.transform.CompileStatic

import java.util.regex.Pattern

import javax.servlet.DispatcherType

import org.microsauce.gravy.context.groovy.GroovyServiceFactory
import org.microsauce.gravy.context.javascript.JSServiceFactory
import org.microsauce.gravy.module.Module
import org.microsauce.gravy.module.groovy.GroovyModule
import org.microsauce.gravy.module.javascript.JSModule
import org.microsauce.gravy.util.pattern.RegExUtils


// TODO roll all of this into Module 
// TODO this can probably be a concrete class
abstract class ServiceFactory {

	private static Map<Class, ? extends ServiceFactory> FACTORIES
	
	static {
		FACTORIES = [:]
		FACTORIES.put GroovyModule.class, new GroovyServiceFactory()
		FACTORIES.put JSModule.class, new JSServiceFactory()
	}
	
	@CompileStatic
	static ServiceFactory getFactory(Class module) {
		FACTORIES.get(module)
	}
	
	@Override
	@CompileStatic
	public EnterpriseService makeEnterpriseService(Object scriptContext, String uriPattern, Map<String, Object> methodHandlers, List<DispatcherType> dispatch, Module module ) { //ErrorHandler errorHandler, String viewUri) {

		EnterpriseService service = new EnterpriseService()
		Map<String, Object> parseRoute = RegExUtils.parseRoute(uriPattern)
		
		HandlerFactory handlerFactory = HandlerFactory.getHandlerFactory(this.class)		
		service.uriPattern = (Pattern) parseRoute.uriPattern
		service.uriString = uriPattern
	 	service.params = parseRoute.params as List<String>
		service.dispatch = dispatch
		service.viewUri = module.viewUri

		methodHandlers.each { String method, Object rawHandler ->
			Handler handler = handlerFactory.makeHandler(rawHandler, scriptContext)
			handler.errorHandler = module.errorHandler
			handler.viewUri = module.viewUri
			handler.module = module
			service.handlers.put(method, handler)
		}

//		doMakeEnterpriseService(scriptContext, service, methodHandlers)
		
		service
	}
	
	abstract EnterpriseService doMakeEnterpriseService(Object scriptContext, EnterpriseService service, Map<String, Object> methodHandlers)

	abstract CronService makeCronService(Object scriptContext, String cronString, Object rawHandler)
	
}
