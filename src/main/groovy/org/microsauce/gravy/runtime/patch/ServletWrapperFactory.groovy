package org.microsauce.gravy.runtime.patch

import groovy.transform.CompileStatic
import org.jruby.RubyIO
import org.jruby.embed.ScriptingContainer
import org.microsauce.gravy.lang.object.GravyType
import org.microsauce.gravy.module.Module
import org.ringojs.wrappers.Stream
import org.mozilla.javascript.Scriptable

import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession

/**
 * Created with IntelliJ IDEA.
 * User: microsauce
 * Date: 3/18/13
 * Time: 11:48 AM
 * To change this template use File | Settings | File Templates.
 */
class ServletWrapperFactory {

    @CompileStatic static ServletWrapper getWrapper(GravyType type, HttpServletRequest req, HttpServletResponse res, FilterChain chain) {
        switch (type.type) {
            case GravyType.GROOVY.type:
                return getGroovyWrapper(req, res, chain)
            case GravyType.JAVASCRIPT.type:
                return getJavaScriptWrapper(req, res, chain)
            case GravyType.RUBY.type:
                return getRubyWrapper(req, res, chain)
            default:
                return null
        }
    }

    @CompileStatic private static ServletWrapper getGroovyWrapper(HttpServletRequest req, HttpServletResponse res, FilterChain chain) {
        GravyHttpSession gSess = (GravyHttpSession) java.lang.reflect.Proxy.newProxyInstance(
                GravyHttpSession.class.getClassLoader(),
                [GravyHttpSession.class] as Class[],
                new GravySessionProxy(req.session))
        GravyHttpServletRequest gReq = (GravyHttpServletRequest) java.lang.reflect.Proxy.newProxyInstance(
                GravyHttpServletRequest.class.getClassLoader(),
                [GravyHttpServletRequest.class] as Class[],
                new GravyRequestProxy(req, res, gSess, chain))
        GravyHttpServletResponse gRes = (GravyHttpServletResponse) java.lang.reflect.Proxy.newProxyInstance(
                GravyHttpServletResponse.class.getClassLoader(),
                [GravyHttpServletResponse.class] as Class[],
                new GravyResponseProxy(res, req))
        new ServletWrapper([req: gReq, res: gRes])
    }

    @CompileStatic private static ServletWrapper getJavaScriptWrapper(HttpServletRequest req, HttpServletResponse res, FilterChain chain) {
        GravyHttpSession jsSess = (GravyHttpSession) java.lang.reflect.Proxy.newProxyInstance(
                GravyHttpSession.class.getClassLoader(),
                [GravyHttpSession.class] as Class[],
                new GravySessionProxy(req.session))
        GravyHttpServletRequest jsReq = (GravyHttpServletRequest) java.lang.reflect.Proxy.newProxyInstance(
                GravyHttpServletRequest.class.getClassLoader(),
                [GravyHttpServletRequest.class] as Class[],
                new JSRequestProxy(req, res, jsSess, chain))
        GravyHttpServletResponse jsRes = (GravyHttpServletResponse) java.lang.reflect.Proxy.newProxyInstance(
                GravyHttpServletResponse.class.getClassLoader(),
                [GravyHttpServletResponse.class] as Class[],
                new JSResponseProxy(res, req))

        new ServletWrapper([req: jsReq, res: jsRes])
    }

    @CompileStatic private static ServletWrapper getRubyWrapper(HttpServletRequest req, HttpServletResponse res, FilterChain chain) {
        GravyHttpSession rbSess = (GravyHttpSession) java.lang.reflect.Proxy.newProxyInstance(
                GravyHttpSession.class.getClassLoader(),
                [GravyHttpSession.class] as Class[],
                new GravySessionProxy(req.session));
        GravyHttpServletRequest rbReq = (GravyHttpServletRequest) java.lang.reflect.Proxy.newProxyInstance(
                GravyHttpServletRequest.class.getClassLoader(),
                [GravyHttpServletRequest.class] as Class[],
                new RubyRequestProxy(req, res, rbSess, chain));
        GravyHttpServletResponse rbRes = (GravyHttpServletResponse) java.lang.reflect.Proxy.newProxyInstance(
                GravyHttpServletResponse.class.getClassLoader(),
                [GravyHttpServletResponse.class] as Class[],
                new RubyResponseProxy(res, req));

        new ServletWrapper([req: rbReq, res: rbRes])
    }

    /**
     * JS implementation
     */
    static class JSResponseProxy extends GravyResponseProxy {
        @CompileStatic JSResponseProxy(HttpServletResponse res, HttpServletRequest request) {
            super(res, request)
            Module module = (Module)request.getAttribute('_module')
            out = new Stream((Scriptable) module.scriptContext, (OutputStream)out, null)
        }
    }

    static class JSRequestProxy extends GravyRequestProxy {
        @CompileStatic JSRequestProxy(Object target, HttpServletResponse res, HttpSession session, FilterChain chain) {
            super(target, res, session, chain)
            Module module = (Module)((HttpServletRequest)target).getAttribute('_module')
            input = new Stream((Scriptable)module.scriptContext, (InputStream)input, null)
        }
    }

    /**
     * Ruby implementation
     */
    static class RubyResponseProxy extends GravyResponseProxy {
         @CompileStatic public RubyResponseProxy(HttpServletResponse res, HttpServletRequest request) {
            super(res, request);
            Module module = (Module)request.getAttribute('_module')
            ScriptingContainer container = (ScriptingContainer) module.scriptContext
            out = new RubyIO(container.getProvider().getRuntime(), (OutputStream) out);
        }
    }

    static class RubyRequestProxy extends GravyRequestProxy {
        @CompileStatic public RubyRequestProxy(Object target, HttpServletResponse res, HttpSession session, FilterChain chain) {
            super(target, res, session, chain);
            Module module = (Module)((HttpServletRequest)target).getAttribute('_module')
            ScriptingContainer container = (ScriptingContainer) module.scriptContext
            setInput(new RubyIO(container.getProvider().getRuntime(), (InputStream) getInput()));
        }
    }

}
