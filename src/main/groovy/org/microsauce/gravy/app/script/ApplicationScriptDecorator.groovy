package org.microsauce.gravy.app.script

import org.microsauce.gravy.app.*
import static com.microsauce.util.PathUtil.*

class ApplicationScriptDecorator extends ScriptDecorator {

	private List<String> roots

	ApplicationScriptDecorator(ConfigObject config, ApplicationContext app) {
		super(config, app)
		roots = ["${config.appRoot}${SLASH}.","${config.appRoot}${SLASH}scripts","${config.appRoot}${SLASH}conf"]
	}

	void decorate(Script script) {
		super.decorate(script)

		script.roots = this.roots
		script.binding << app.modCache
		script.binding << [
			module : { name -> 
				def modScript = new Script([name:name])
				new ModuleScriptDecorator(config, app).decorate(modScript)
				ScriptUtils.run(modScript)
			},
			run : { name, scriptBinding = null ->
				if (!scriptBinding) {
					if (!app.loadCache[name]) { 
						def subScript = new Script([sourceUri: name+'.groovy', binding: [config:config, app:app], roots: roots])
						new ScriptDecorator(config, app).decorate(subScript)
						def result = ScriptUtils.run(subScript)
						if ( result == app ) result = null
						app.loadCache[name] = result
					}
					return app.loadCache[name]
				} else {
					scriptBinding << [config:config, app:app]
					def subScript = new Script([sourceUri: name+'.groovy', binding: scriptBinding, roots: roots])
					new ScriptDecorator(config, app).decorate(subScript)
					def result = ScriptUtils.run(subScript)
					if ( result == app ) result = null
					return result
				}
			}
		]
	}

}