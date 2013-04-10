package org.microsauce.gravy.context.ruby;

import java.io.InputStream;

import org.jruby.RubyIO;
import org.jruby.RubyObject;
import org.jruby.embed.ScriptingContainer;
import org.microsauce.gravy.context.ServletFacade;
import org.microsauce.gravy.context.Handler;
import org.microsauce.gravy.lang.object.GravyType;

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
    public Object doExecute(ServletFacade facade) {
        return container.callMethod(callBack, "invoke",
                new Object[] {
                        facade
//                        wrapper.getReq(GravyType.RUBY),
//                        wrapper.getRes(GravyType.RUBY),
//                        wrapper.getUriParamMap(),
//                        wrapper.getUriParamValues(),
//                        wrapper.getJson(),          // TODO verify
//                        wrapper.getRequestParams() // TODO verify what is 'requestParams'
                });
    }


}
