package org.microsauce.gravy.context.groovy

import org.microsauce.gravy.context.Action;

class GroovyAction implements Action {

	private Closure actionClosure

	GroovyAction(Closure closure) {
		this.actionClosure = closure
	}

	Object execute(Map binding, List args = null) {
		if ( args == null ) args = []
		Closure closure = actionClosure.clone()
		closure.delegate = binding as Binding
		closure.resolveStrategy = Closure.DELEGATE_FIRST
		closure.call(args.size == 1 ? args[0] : args)
	}


}