package org.microsauce.gravy.lang.groovy.script

import static org.microsauce.gravy.util.PathUtil.*

import org.microsauce.gravy.*
import org.microsauce.gravy.app.*
import org.microsauce.gravy.context.Context
import org.microsauce.gravy.dev.GravyDevModeClassLoader


class ScriptDecorator {

	protected ConfigObject config
	protected Context context

	ScriptDecorator(ConfigObject config, Context context) {
		this.config = config
		this.context = context
	}

	void decorate(Script script) {
		script.binding << [
			config : config
		]
	}

//	def protected getClassLoader() {
//		if ( config.gravy.refresh ) 
//			return new GravyDevModeClassLoader("${config.appRoot}${SLASH}WEB-INF${SLASH}classes")
//		else
//			return this.getClass().getClassLoader()
//	}
}