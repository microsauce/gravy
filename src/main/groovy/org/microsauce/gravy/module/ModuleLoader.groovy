package org.microsauce.gravy.module

import org.microsauce.gravy.context.Context

abstract class ModuleLoader {
	
	protected Module module // TODO this makes the previous 2 properties redundant
	
	abstract Context load(Context context, Map bindings)
}
