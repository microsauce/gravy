package org.microsauce.gravy.dev.observer

import org.microsauce.gravy.context.ApplicationContext
import org.microsauce.gravy.dev.lifecycle.Lifecycle

class BuildSourceModHandler implements SourceModHandler {
	private ConfigObject config
	private ApplicationContext app

	BuildSourceModHandler(ConfigObject config, ApplicationContext app) {
		this.config = config
		this.app = app
	}

	void handle() {
		try {
			def builder = new Lifecycle()
			builder.compile()
		}
		catch(all) {
			all.printStackTrace()
		}
	}
}