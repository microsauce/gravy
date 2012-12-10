package org.microsauce.gravy.context

import javax.servlet.DispatcherType

import org.microsauce.gravy.module.Module
import org.microsauce.gravy.runtime.ErrorHandler


abstract class Service {
	
	static String GET 		= 'get'
	static String POST 		= 'post'
	static String PUT 		= 'put'
	static String HEAD 		= 'head'
	static String DELETE 	= 'delete'
	static String OPTIONS 	= 'options'
	static String DEFAULT 	= 'default'
	
	Module module
	Class binding
	List<String> params
	List<DispatcherType> dispatch = []

	Map<String, Handler> handlers = [:]
	HandlerFactory handlerFactory
	ErrorHandler errorHandler
	String viewUri

	void setHandler(String method, Object rawHandler, Object scriptContext) {
		Handler handler = handlerFactory.makeHandler rawHandler, scriptContext
		handler.errorHandler = errorHandler
		handlers[method] = handler
	}
}
