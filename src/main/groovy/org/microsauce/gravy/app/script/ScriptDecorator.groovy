package org.microsauce.gravy.app.script

import org.microsauce.gravy.app.*

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
}