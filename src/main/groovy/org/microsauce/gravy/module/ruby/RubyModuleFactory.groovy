package org.microsauce.gravy.module.ruby

import groovy.transform.CompileStatic
import org.microsauce.gravy.lang.object.GravyType
import org.microsauce.gravy.module.ModuleFactory

class RubyModuleFactory extends ModuleFactory {

    @Override
    @CompileStatic
    public GravyType type() {
        GravyType.RUBY
    }

    @Override
	@CompileStatic
	public String moduleClassName() {
		'org.microsauce.gravy.module.ruby.RubyModule'
	}

	@Override
	@CompileStatic
	public String moduleScriptName() {
		'application.rb'
	}

}

