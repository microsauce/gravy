package org.microsauce.gravy.app

import org.microsauce.gravy.dev.Lifecycle

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