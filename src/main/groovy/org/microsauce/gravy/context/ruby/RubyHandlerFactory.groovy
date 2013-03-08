package org.microsauce.gravy.context.ruby

import groovy.transform.CompileStatic

import org.jruby.RubyObject
import org.jruby.embed.ScriptingContainer
import org.microsauce.gravy.context.Handler
import org.microsauce.gravy.context.HandlerFactory

class RubyHandlerFactory extends HandlerFactory {

    @Override
    @CompileStatic
    public Handler makeHandler(Object rawHandler, Object scriptContext) {
        new RubyHandler((RubyObject) rawHandler, (ScriptingContainer) scriptContext)
    }

}
