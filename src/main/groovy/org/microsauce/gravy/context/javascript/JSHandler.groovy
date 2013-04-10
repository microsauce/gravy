package org.microsauce.gravy.context.javascript

import groovy.transform.CompileStatic
import org.microsauce.gravy.lang.object.GravyType
import org.ringojs.wrappers.Stream
import org.microsauce.gravy.context.Handler
import org.microsauce.gravy.context.ServletFacade
import org.mozilla.javascript.Context
import org.mozilla.javascript.NativeFunction
import org.mozilla.javascript.NativeObject
import org.mozilla.javascript.ScriptableObject

/**
 *
 * @author microsuace
 *
 */
class JSHandler extends Handler {

    ScriptableObject scope
    NativeFunction callBack

    NativeFunction executeHandler
    Context ctx

    JSHandler(NativeFunction callBack, ScriptableObject scope) {
        this.callBack = callBack
        this.scope = scope
        this.executeHandler = scope.get('executeHandler', scope)
    }

    Object wrapInputStream(InputStream inputStream) {
        return new Stream(scope, inputStream, null)
    }

    @Override
    @CompileStatic
    public Object doExecute(Object params) {
        ctx = org.mozilla.javascript.Context.enter()
        ctx.setLanguageVersion(Context.VERSION_1_8)
        try {
            return callBack.call(ctx, scope, scope, params as Object[])
        }
        finally {
            ctx.exit()
        }
    }

    @Override
    @CompileStatic
    public Object doExecute(ServletFacade facade) {

        ctx = org.mozilla.javascript.Context.enter()
        try {
            // TODO rewrite pass the wrapper in alone
            executeHandler.call ctx, scope, scope, [
                    callBack,
                    facade] as Object[]
        }
        catch (Throwable t) {
            t.printStackTrace()
        }
        finally {
            ctx.exit()
        }
    }


}
