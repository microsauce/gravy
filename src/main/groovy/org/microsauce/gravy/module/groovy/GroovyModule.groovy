package org.microsauce.gravy.module.groovy

import groovy.transform.CompileStatic

import org.microsauce.gravy.lang.groovy.api.GroovyAPI
import org.microsauce.gravy.lang.groovy.script.Script
import org.microsauce.gravy.lang.groovy.script.ScriptDecorator
import org.microsauce.gravy.lang.groovy.script.ScriptUtils
import org.microsauce.gravy.lang.object.CommonObject
import org.microsauce.gravy.lang.object.GravyType
import org.microsauce.gravy.module.Module


class GroovyModule extends Module {

    @CompileStatic
    protected Object doLoad() {
        ConfigObject root = new ConfigObject()
        Map exp = [:]
        Map binding = [:]
        binding.root = root
        binding.conf = config

        // create, initialize, and execute the script
        Script script = new Script()
        script.binding.putAll(binding)
        script.binding.exp = exp
        script.classLoader = classLoader
        script.name = name
        script.sourceUri = scriptFile.absolutePath
        if (folder.exists()) {
            script.roots << folder.absolutePath
            File scriptsFolder = new File(folder, '/lib')
            if (scriptsFolder.exists())
                script.roots << folder.absolutePath + "/lib"
        }
        addClosure script.binding
        GroovyAPI.module = this

        ScriptUtils.run script
    }

    void addClosure(Map binding) {
        binding.run = { name, scriptBinding = null ->
            def subScript = new Script(
                    [sourceUri: name + '.groovy', binding: [config: config, app: app], roots: script.roots, classLoader: script.classLoader])
            new ScriptDecorator(config, context).decorate(subScript)
            ScriptUtils.run(subScript)
        }
    }

}
