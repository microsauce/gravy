package org.microsauce.gravy.app

class ActionUtils {
	static void call(Closure action, Map binding) {
		call action, binding, []
	}
	static void call(Closure action, Map binding, List args) {
		cloneAndBind(action, binding).call(args)
	}

	static Closure cloneAndBind(Closure action, Map binding) {
		Closure closure = action.clone()
		closure.delegate = binding as Binding
		closure.resolveStrategy = Closure.DELEGATE_FIRST
		return closure
	}
}