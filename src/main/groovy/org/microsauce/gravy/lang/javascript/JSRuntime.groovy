package org.microsauce.gravy.lang.javascript

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j
import org.apache.log4j.Logger

import org.mozilla.javascript.Context
import org.mozilla.javascript.Scriptable
import org.mozilla.javascript.tools.shell.Global
import org.ringojs.engine.RhinoEngine
import org.ringojs.engine.RingoConfig
import org.ringojs.repository.FileRepository
import org.ringojs.repository.FileResource
import org.ringojs.repository.Repository
import org.ringojs.repository.ZipRepository

@Log4j
abstract class JSRuntime {

    RhinoEngine engine
    Scriptable global


    @CompileStatic JSRuntime(List<File> roots) {

        String ringoJarPath = null
        String appRoot = System.getProperty("gravy.appRoot")
        if (appRoot)
            ringoJarPath = appRoot + '/lib/ringo-modules.jar'

        else if (System.getenv()['GRAVY_HOME'])
            ringoJarPath = System.getenv()['GRAVY_HOME'] + '/lib/ringojs/ringo-modules.jar'

        Repository ringoRepo = new ZipRepository(ringoJarPath)
        RingoConfig config = new RingoConfig(ringoRepo)

        if (roots) {
            roots.each { File thisRoot ->
                if (config == null) config = new RingoConfig(new FileRepository(thisRoot))
                else config.addModuleRepository(new FileRepository(thisRoot))
            }
        }

        engine = new RhinoEngine(config, null)
        global = engine.getScope()
        global.put('out', global, System.out)
        global.put('devMode', global, System.getProperty('gravy.devMode'))
        getCoreScripts().each { String thisScript ->
            engine.runScript(thisScript, [] as String[])
        }
    }

    @CompileStatic Object run(String scriptUri, Map<String, Object> binding) {
        Object returnValue = null
        if (binding) {
            binding.each { String key, Object value ->
                global.put(key, global, value)
            }
        }

        // evaluate application.js
        engine.runScript(scriptUri, [] as String[])
        Scriptable services = (Scriptable) global.get('exp', global)
        returnValue = services
        returnValue
    }

    @CompileStatic Object run(File script, Map<String, Object> binding) {

        Object returnValue = null
        if (binding) {
            binding.each { String key, Object value ->
                global.put(key, global, value)
            }
        }

        // evaluate application.js
        FileResource fileResource = new FileResource(script)
        engine.runScript(fileResource, [] as String[])
        Scriptable services = (Scriptable) global.get('exp', global)
        returnValue = services
        returnValue
    }

    @CompileStatic void appendRoots(List<File> roots) {

        roots.each { File thisRoot ->
            FileRepository frepo = new FileRepository(thisRoot)
            engine.config.repositories.add(frepo)
        }
    }

    @CompileStatic void prependRoots(List<File> roots) {
        roots.reverse().each { File thisRoot ->
            FileRepository frepo = new FileRepository(thisRoot)
            engine.config.repositories.add(0,frepo)
        }
    }

    abstract String[] getCoreScripts()
}
