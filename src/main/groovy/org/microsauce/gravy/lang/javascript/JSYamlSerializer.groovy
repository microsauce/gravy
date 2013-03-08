package org.microsauce.gravy.lang.javascript

import groovy.transform.CompileStatic
import org.microsauce.gravy.lang.object.Serializer
import org.mozilla.javascript.Context
import org.mozilla.javascript.NativeFunction
import org.mozilla.javascript.ScriptableObject
import org.mozilla.javascript.tools.shell.Global
import sun.org.mozilla.javascript.internal.Scriptable

/**
 * Created with IntelliJ IDEA.
 * User: jboone
 * Date: 2/15/13
 * Time: 2:40 PM
 * To change this template use File | Settings | File Templates.
 */
class JSYamlSerializer implements Serializer {

    private static JSYamlSerializer instance
    private ScriptableObject scope
    private NativeFunction parseYaml
    private NativeFunction toYaml

    static void initInstance(Global global) {
        instance = new JSYamlSerializer()
        instance.scope = global
        instance.parseYaml = global.get('parseYaml', global)
        instance.toYaml = global.get('toYaml', global)
    }


    @Override
    @CompileStatic
    Object parse(String string) {
        Context currentCtx = Context.getCurrentContext()
        Context ctx = null
        try {
            if (!currentCtx) ctx = Context.enter()
            else ctx = currentCtx
            return parseYaml.call(ctx, scope, scope, [string] as Object[])
        }
        finally {
            if (!currentCtx) ctx.exit()
        }
    }

    @Override
    @CompileStatic
    String toString(Object object) {
        Context currentCtx = Context.getCurrentContext()
        Context ctx = null
        try {
            if (!currentCtx) ctx = Context.enter()
            else ctx = currentCtx
            toYaml.call ctx, scope, scope, [object] as Object[]
        }
        finally {
            if (!currentCtx) ctx.exit()
        }
    }
}
