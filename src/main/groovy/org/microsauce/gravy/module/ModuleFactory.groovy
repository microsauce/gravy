package org.microsauce.gravy.module

import groovy.transform.CompileStatic

import org.apache.log4j.Logger
import org.codehaus.groovy.tools.LoaderConfiguration
import org.codehaus.groovy.tools.RootLoader
import org.microsauce.gravy.context.Context
import org.microsauce.gravy.context.ServiceFactory
import org.microsauce.gravy.lang.object.GravyType
import org.microsauce.gravy.module.config.ConfigLoader
import org.microsauce.gravy.module.groovy.GroovyModuleFactory
import org.microsauce.gravy.module.javascript.JSModuleFactory
import org.microsauce.gravy.module.ruby.RubyModuleFactory

abstract class ModuleFactory {

    Logger log = Logger.getLogger(ModuleFactory.class)

    static JSModuleFactory jsModuleFactory = new JSModuleFactory()
    static Map<String, ModuleFactory> FACTORY_TYPES = [
            'groovy': new GroovyModuleFactory(),
            'js': jsModuleFactory,
            'coffee': jsModuleFactory,
            'rb': new RubyModuleFactory()
    ]

    @CompileStatic
    static ModuleFactory getInstance(String type) {
        FACTORY_TYPES[type]
    }

    @CompileStatic
    Module createModule(Context context, File moduleFolder, File appScript, Boolean isApp) {

        //
        // disable a module without un-installing/deleting it
        //
        ConfigLoader configLoader = ConfigLoader.getInstance()
        ConfigObject modConfig = configLoader.loadModConfig(moduleFolder)
        if (modConfig && modConfig.disabled) return null

        // create module classloader and instantiate the module object
        ClassLoader cl = createModuleClassLoader(moduleFolder)
        Class moduleClass = cl.loadClass(moduleClassName())
        Module module = (Module) moduleClass.newInstance()

        module.config = modConfig

        // non-config properties
        module.type = type()
        module.context = context
        module.name = moduleFolder.name
        module.scriptFile = new File(moduleScriptName())
        module.isApp = isApp
        module.classLoader = cl
        module.folder = moduleFolder
        module.scriptFile = appScript
        module.serviceFactory = new ServiceFactory(module)

        // config properties
        ConfigObject gravyConfig = (ConfigObject) module.config.gravy
        ConfigObject gravyViewConfig = (ConfigObject) gravyConfig.view
        module.renderUri = gravyViewConfig.renderUri
        module.applicationConfig = configLoader.appConfig
        module.errorUri = gravyViewConfig.errorUri
        module.moduleLogger = Logger.getLogger(module.name)

        initializeRuntime(module)

        module
    }

    void initializeRuntime(Module module) {}

    @CompileStatic
    private ClassLoader createModuleClassLoader(File moduleFolder) {
//        if ( moduleClassName() == 'org.microsauce.gravy.module.ruby.RubyModule' )
//            return this.class.classLoader

        log.info "initialize ${moduleFolder.name} classloader . . ."
        List<URL> classpath = []
        LoaderConfiguration loaderConf = new LoaderConfiguration()

        if (System.getProperty('gravy.devMode')) {
            loaderConf.addFile(new File(System.getProperty('user.dir') + '/target/classes'))
        }

        // module lib -- web-inf/moduleName/lib - relevent for Groovy modules only
        File modLib = new File(moduleFolder, 'lib')
        if (modLib.exists()) {
            modLib.eachFile { File thisLib ->
                log.info "\tadding ${thisLib.absolutePath} to classpath"
                loaderConf.addFile thisLib
            }
        }
        new RootLoader(loaderConf)
    }

    /**
     * The module subclass to instantiate and initialize
     *
     * @return
     */
    abstract String moduleClassName()

    /**
     * The module script name
     *
     * @return
     */
    abstract String moduleScriptName()

    /**
     * The module name
     */
    abstract GravyType type()
}
