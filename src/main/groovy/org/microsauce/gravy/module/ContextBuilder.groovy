package org.microsauce.gravy.module

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j

import java.util.regex.Matcher
import java.util.regex.Pattern

import org.microsauce.gravy.context.Context


@Log4j
class ContextBuilder {

	private static final Pattern SCRIPT_NAME = ~/application\.([a-zA-Z0-9]+)/
		
	Module application
	Context context
	File appRoot
	String env = env

	
	ContextBuilder(File appRoot, String env) {
		context = new Context()
		this.appRoot = appRoot
		this.env = env
	}
	
	@CompileStatic
	Context build() {
		Module app = instantiateApplication(env)
		application = app
		Collection<Module> modules = instantiateModules(context, app.moduleConfig, env)
		
		Map<String, Object> moduleBindings = [:]
		for ( thisModule in modules ) {
			thisModule.load()
			moduleBindings[thisModule.name] = thisModule.returnValue
		}
		app.binding = moduleBindings
		app.load()
		app.context
	}

	
	@CompileStatic
	private Collection<Module> instantiateModules(Context context, ConfigObject appConfig, String env) {
		List<Module> modules = []
		
		for (modFolder in ContextBuilder.listModules(appRoot)) { 
			modules << instantiateModule(context, modFolder, appConfig, env, false)
		}

		modules
	}
	
	@CompileStatic
	private Module instantiateModule(Context context, File modFolder, ConfigObject appConfig, String env, Boolean isApp) {
		String modName = modFolder.name
		log.info "instantiating module $modName"
		File application = applicationScript(modFolder)
		Matcher matcher = application.name =~ SCRIPT_NAME
		matcher.find()
	
		String fileExtension = matcher.group(1)// [0][1]
		ModuleFactory moduleFactory = ModuleFactory.getInstance fileExtension
		if ( moduleFactory == null )
			throw new Exception("unable to find module loader for file type ${fileExtension}.")
		moduleFactory.createModule(context, modFolder, appConfig, env, isApp)
	}
	
	@CompileStatic
	private File applicationScript(File moduleFolder) {
		List<File> application = []
		
		moduleFolder.eachFile { File file ->
			if ( file.isFile() && file.name.startsWith('application.') )
				application << file		
		}
		
		if ( application.size() == 0 )
			throw new Exception("An application script is not defined in module ${moduleFolder.name}.")
		else if ( application.size() > 1 )
			throw new Exception("There are multiple application scripts defined in module ${moduleFolder.name}.")
			
		application[0]
	}
	
	@CompileStatic
	static List<File> listModules(File appRoot) {
		// return all except 'app'
		File modulesFolder = new File(appRoot,'/modules')

		List<File> folders = []
		modulesFolder.eachDir { File dir ->
			if ( dir.name != 'app' ) folders << dir
		}
		folders
	}
	
	@CompileStatic
	static List<File> listAllModules(File appRoot) {
		// return all except 'app'
		File modulesFolder = new File(appRoot,'/modules')

		List<File> folders = []
		modulesFolder.eachDir { File dir ->
			folders << dir
		}
		folders
	}
	
	private Module instantiateApplication(String env) {
		File modFolder = new File(appRoot, 'modules')
		File appFolder = new File(modFolder, 'app')
		instantiateModule(context, appFolder, null, env, true)
	}
	
}