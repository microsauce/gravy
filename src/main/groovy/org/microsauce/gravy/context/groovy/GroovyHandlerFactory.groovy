package org.microsauce.gravy.context.groovy

import groovy.transform.CompileStatic

import org.microsauce.gravy.context.Handler
import org.microsauce.gravy.context.HandlerFactory

class GroovyHandlerFactory extends HandlerFactory {

	@Override
	@CompileStatic
	public Handler makeHandler(Object rawHandler, Object scriptContext) {
		new GroovyHandler((Closure)rawHandler)
	}

}
