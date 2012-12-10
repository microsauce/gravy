package org.microsauce.gravy.context.javascript

import groovy.transform.CompileStatic

import org.microsauce.gravy.context.CronService
import org.microsauce.gravy.context.EnterpriseService
import org.microsauce.gravy.context.ServiceFactory
import org.mozilla.javascript.NativeObject
import org.mozilla.javascript.ScriptableObject

class JSServiceFactory extends ServiceFactory {
	
	@Override
	@CompileStatic
	EnterpriseService doMakeEnterpriseService(Object scriptContext, EnterpriseService service, Map<String, Object> methodHandlers) {
		methodHandlers.each { String method, Object rawHandler ->
			service.handlers.put(method, new JSHandler((NativeObject)rawHandler, (ScriptableObject) scriptContext))
		}

		service
	}

	@Override
	@CompileStatic
	CronService makeCronService(Object scriptContext, String cronString, Object rawHandler) {
		CronService service = new CronService()
		service.handlers.put('default', new JSHandler((NativeObject)rawHandler, (ScriptableObject) scriptContext))
		service.cronString = cronString
		return service;
	}

}
