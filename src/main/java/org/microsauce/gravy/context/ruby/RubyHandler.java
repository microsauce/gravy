package org.microsauce.gravy.context.ruby;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jruby.RubyIO;
import org.jruby.RubyObject;
import org.jruby.embed.ScriptingContainer;
import org.microsauce.gravy.context.GravyServletWrapper;
import org.microsauce.gravy.context.Handler;
import org.microsauce.gravy.lang.object.CommonObject;
import org.microsauce.gravy.lang.object.GravyType;
import org.microsauce.gravy.module.Module;
import org.microsauce.gravy.runtime.patch.GravyHttpServletRequest;
import org.microsauce.gravy.runtime.patch.GravyHttpServletResponse;
import org.microsauce.gravy.runtime.patch.GravyHttpSession;
import org.microsauce.gravy.runtime.patch.GravyRequestProxy;
import org.microsauce.gravy.runtime.patch.GravyResponseProxy;
import org.microsauce.gravy.runtime.patch.GravySessionProxy;

/**
 * @author microsuace
 */
class RubyHandler extends Handler {

    ScriptingContainer container;
    RubyObject callBack;

    public RubyHandler(RubyObject callBack, ScriptingContainer container) {
        this.container = container;
        this.callBack = callBack;
    }

    public Object doExecute(Object params) {
        return container.callMethod(callBack, "invoke", params);
    }

    protected Object wrapInputStream(InputStream inputStream) {
        return new RubyIO(container.getProvider().getRuntime(), inputStream);
    }

    @Override
    public Object doExecute(GravyServletWrapper wrapper) {
        return container.callMethod(callBack, "invoke",
                new Object[] {
                        wrapper.getReq(GravyType.RUBY),
                        wrapper.getRes(GravyType.RUBY),
                        wrapper.getParamMap(),
                        wrapper.getParamList(),
                        wrapper.getJson(),          // TODO verify
                        wrapper.getParams() // TODO verify what is 'params'
                });
    }


}
