package org.microsauce.gravy.context.groovy

import groovy.transform.CompileStatic

import org.microsauce.gravy.context.CronService
import org.microsauce.gravy.context.EnterpriseService
import org.microsauce.gravy.context.Service
import org.microsauce.gravy.context.ServiceFactory

class GroovyServiceFactory extends ServiceFactory {

	
	@CompileStatic
	public EnterpriseService doMakeEnterpriseService(Object scriptContext, EnterpriseService service, Map<String, Object> methodHandlers) {
		methodHandlers.each { String method, Object rawHandler ->
			service.handlers.put(method, new GroovyHandler((Closure)rawHandler))
		}

		service
	}

	@Override
	@CompileStatic
	public CronService makeCronService(Object scriptContext, String cronString, Object rawHandler) {
		CronService service = new CronService()
		service.handlers.put('default', new GroovyHandler((Closure)rawHandler))
		service.cronString = cronString
		return service;
	}

}
