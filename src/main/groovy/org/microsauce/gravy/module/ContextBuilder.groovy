package org.microsauce.gravy.module

import groovy.transform.CompileStatic
import org.testng.log4testng.Logger

import java.util.regex.Matcher
import java.util.regex.Pattern

import org.microsauce.gravy.context.Context


class ContextBuilder {

    Logger log = Logger.getLogger(ContextBuilder.class)
    private static final Pattern SCRIPT_NAME = ~/application\.([a-zA-Z0-9]+)/

    File appRoot
    String env = env

    ContextBuilder(File appRoot, String env) {
        this.appRoot = appRoot
        this.env = env
    }

    @CompileStatic Context build() {
		Context context = new Context()
		Module app = instantiateApplication(context)
        context.app = app
        Map<String,Module> modules = instantiateModules(context, app)
		context.modules << context.app
		context.modules.addAll(modules.values())
		
        Module.moduleLoadOrder(modules.keySet(), app.config.moduleOrder).each { String moduleName ->
                log.info "\tloading module ${moduleName}"
            modules[moduleName].load()
        }

        app.load()
        context
    }


    @CompileStatic
    private Map<String, Module> instantiateModules(Context context, Module app) {
        Map<String, Module> modules = [:]

        for (modFolder in ContextBuilder.listModules()) {
            Module module = instantiateModule(context, modFolder, app)
            if (module) modules[module.name] = module
        }

        modules
    }

    @CompileStatic
    private Module instantiateModule(Context context, File modFolder, Module app) {
        String modName = modFolder.name
        log.info "instantiating module $modName"
        File applicationScript = applicationScript(modFolder)
        Matcher matcher = applicationScript.name =~ SCRIPT_NAME
        matcher.find()

        String fileExtension = matcher.group(1)
        ModuleFactory moduleFactory = ModuleFactory.getInstance fileExtension
        if (moduleFactory == null)
            throw new Exception("unable to find module loader for file name ${fileExtension}.")

        Module thisModule = moduleFactory.createModule(context, modFolder, applicationScript, app ? false : true)
        thisModule.app = app
        thisModule
    }

    @CompileStatic
    private File applicationScript(File moduleFolder) {
        List<File> application = []

        moduleFolder.eachFile { File file ->
            if (file.isFile() && (
            file.name.equals('application.groovy') ||
                    file.name.equals('application.coffee') ||
                    file.name.equals('application.js') ||
                    file.name.equals('application.rb')
            ))
            application << file
        }

        if (application.size() == 0)
            throw new Exception("An application script is not defined in module ${moduleFolder.name}.")
        else if (application.size() > 1)
            throw new Exception("There are multiple application scripts defined in module ${moduleFolder.name}.")

        application[0]
    }

    @CompileStatic
    static List<File> listModules() {
        // return all except 'app'
        File modulesFolder = new File(System.getProperty('gravy.moduleRoot'))

        List<File> folders = []
        modulesFolder.eachDir { File dir ->
            if (dir.name != 'app') folders << dir
        }
        folders
    }

    @CompileStatic
    static List<File> listAllModules(File moduleRoot) {
        List<File> folders = []
        moduleRoot.eachDir { File dir ->
            folders << dir
        }
        folders
    }

    private Module instantiateApplication(Context context) {
        File appFolder = new File(new File(System.getProperty('gravy.moduleRoot')), 'app') // TODO refactor as moduleRoot File property
        instantiateModule(context, appFolder, null)
    }

}
