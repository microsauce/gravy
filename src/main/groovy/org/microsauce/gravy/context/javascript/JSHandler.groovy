package org.microsauce.gravy.context.javascript

import groovy.transform.CompileStatic
import org.microsauce.gravy.runtime.patch.ServletWrapper
import org.ringojs.wrappers.Stream

import java.lang.reflect.Proxy

import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession

import org.microsauce.gravy.context.Handler
import org.microsauce.gravy.context.GravyServletWrapper
import org.microsauce.gravy.lang.object.CommonObject
import org.microsauce.gravy.lang.object.GravyType
import org.microsauce.gravy.module.Module
import org.microsauce.gravy.runtime.patch.GravyHttpServletRequest
import org.microsauce.gravy.runtime.patch.GravyHttpServletResponse
import org.microsauce.gravy.runtime.patch.GravyHttpSession
import org.microsauce.gravy.runtime.patch.GravyRequestProxy
import org.microsauce.gravy.runtime.patch.GravyResponseProxy
import org.microsauce.gravy.runtime.patch.GravySessionProxy
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
    public Object doExecute(GravyServletWrapper wrapper) {

        ctx = org.mozilla.javascript.Context.enter()
        try {
            executeHandler.call ctx, scope, scope, [
                    callBack,
                    wrapper.getReq(module.type),
                    wrapper.getRes(module.type),
                    wrapper.paramMap,
                    wrapper.paramList,
                    wrapper.json,      // TODO verify this was more generic before 'objectBinding'
                    wrapper.params] as Object[]
        }
        catch (Throwable t) {
            t.printStackTrace()
        }
        finally {
            ctx.exit()
        }
    }


}
