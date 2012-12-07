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
	public EnterpriseService makeEnterpriseService(Object scriptContext, String uriPattern, Map<String, Object> methodHandlers, List<DispatcherType> dispatch) {

		EnterpriseService service = new EnterpriseService()
		Map<String, Object> parseRoute = RegExUtils.parseRoute(uriPattern)
		
		service.handlerFactory = HandlerFactory.getHandlerFactory(this.class) // TODO
		service.uriPattern = (Pattern) parseRoute.uriPattern
		service.uriString = uriPattern
	 	service.params = parseRoute.params as List<String>
		service.dispatch = dispatch
		
		doMakeEnterpriseService(scriptContext, service, methodHandlers)
		
		return service;
	}
	
	abstract EnterpriseService doMakeEnterpriseService(Object scriptContext, EnterpriseService service, Map<String, Object> methodHandlers)

	abstract CronService makeCronService(Object scriptContext, String cronString, Object rawHandler)
	
}
