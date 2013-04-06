package org.microsauce.gravy.module.javascript

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j
import org.microsauce.gravy.context.Handler
import org.microsauce.gravy.lang.javascript.GravyJSRuntime
import org.microsauce.gravy.lang.javascript.JSRuntime
import org.microsauce.gravy.lang.javascript.JSSerializer;
import org.microsauce.gravy.module.Module
import org.mozilla.javascript.Context
import org.mozilla.javascript.NativeFunction
import org.mozilla.javascript.ScriptableObject
import org.ringojs.repository.FileResource
import org.ringojs.wrappers.ScriptableMap
import org.ringojs.wrappers.Stream


@Log4j
class JSModule extends Module {

    JSRuntime jsRuntime

    @Override
    @CompileStatic
    protected Object doLoad() {

        if ( name != 'app' )
            jsRuntime.appendRoots([this.folder, new File(folder, '/lib')] as List<File>)
        scriptContext = jsRuntime.global
        JSSerializer.initInstance(jsRuntime.global)

        Map<String, Object> jsBinding = [:]
        jsBinding['j_module'] = this
        jsBinding['j_config'] = config
        jsBinding['j_logger'] = moduleLogger

        jsRuntime.run(scriptFile, jsBinding)
    }

    Object wrapInputStream(InputStream inputStream) {
        return new Stream(scriptContext, inputStream, null)
    }

}