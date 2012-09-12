package org.microsauce.gravy.app

import org.microsauce.gravy.app.script.*
import org.microsauce.gravy.*

class RedeploySourceModHandler implements SourceModHandler {
	private ConfigObject config
	private ApplicationContext app

	RedeploySourceModHandler(ConfigObject config, ApplicationContext app) {
		this.config = config
		this.app = app
	}

	void handle() {
		try {
			app.reset()
			Script script = new Script([sourceUri:"${config.appRoot}/application.groovy"])
			new ApplicationScriptDecorator(config, app).decorate(script)
			ScriptUtils.run(script, new GravyDevModeClassLoader(config.appRoot+'/target/classes'))
			app.complete()
		}
		catch(all) {
			all.printStackTrace()
		}
	}
}