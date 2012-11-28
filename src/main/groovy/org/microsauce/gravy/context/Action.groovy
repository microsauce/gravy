package org.microsauce.gravy.context

interface Action {

	Object execute(Map binding, List arguments)

}