package org.microsauce.gravy.util.script.groovy

import static org.microsauce.gravy.util.PathUtil.*
import org.microsauce.gravy.app.*
import org.microsauce.gravy.context.ApplicationContext;
import org.microsauce.gravy.*

class ScriptDecorator {

	protected ConfigObject config
	protected ApplicationContext app

	ScriptDecorator(ConfigObject config, ApplicationContext app) {
		this.config = config
		this.app = app
	}

	void decorate(Script script) {
		script.binding << [
			config : config,
			root : app.root
		]
	}

	def protected getClassLoader() {
		if ( config.gravy.refresh ) 
			return new GravyDevModeClassLoader("${config.appRoot}${SLASH}WEB-INF${SLASH}classes")
		else
			return this.getClass().getClassLoader()
	}
}