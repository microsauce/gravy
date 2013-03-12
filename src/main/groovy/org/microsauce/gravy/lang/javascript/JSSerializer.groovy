package org.microsauce.gravy.lang.javascript

import groovy.transform.CompileStatic

import org.microsauce.gravy.lang.object.Serializer
import org.mozilla.javascript.Context
import org.mozilla.javascript.NativeFunction
import org.mozilla.javascript.NativeJSON
import org.mozilla.javascript.Scriptable
import org.mozilla.javascript.tools.shell.Global

class JSSerializer implements Serializer {

    static JSSerializer instance

    static JSSerializer getInstance() {
        instance
    }

    static void initInstance(Scriptable global) {
        instance = new JSSerializer()
        instance.scope = global
        instance.parseJson = global.get('parseJson', global)
    }

    Scriptable scope;
    NativeFunction parseJson;

    JSSerializer() {}

    @CompileStatic
    public Object parse(String string) {
        Context currentCtx = Context.getCurrentContext()
        Context ctx = null
        try {
            if (!currentCtx) ctx = Context.enter()
            else ctx = currentCtx
            parseJson.call(ctx, scope, scope, [string] as Object[])
        }
        finally {
            if (!currentCtx) ctx.exit()
        }
    }

    @CompileStatic
    public String toString(Object object) {
        Context currentCtx = Context.getCurrentContext()
        Context ctx = null
        try {
            if (!currentCtx) ctx = Context.enter()
            else ctx = currentCtx
            NativeJSON.stringify(ctx, scope, object, null, null)
        }
        finally {
            if (!currentCtx) ctx.exit()
        }

    }

}
