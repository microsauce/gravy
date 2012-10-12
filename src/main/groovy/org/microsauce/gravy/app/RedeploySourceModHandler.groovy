package org.microsauce.gravy.app

import org.microsauce.gravy.app.script.*
import org.microsauce.gravy.*
import static com.microsauce.util.PathUtil.*

class RedeploySourceModHandler implements SourceModHandler {
	private ConfigObject config
	private ApplicationContext app

	RedeploySourceModHandler(ConfigObject config, ApplicationContext app) {
		this.config = config
		this.app = app
	}

	void handle() {
		try {
println "RedeploySourceModHandler --------------------------------------"			
			app.reset()
			Script script = new Script([name: 'app', sourceUri:"${config.appRoot}${SLASH}WEB-INF${SLASH}modules${SLASH}app${SLASH}application.groovy"])
			new ModuleScriptDecorator(config, app).decorate(script)
			ScriptUtils.run(script) 
			app.complete()
		}
		catch(all) {
			all.printStackTrace()
		}
	}
}