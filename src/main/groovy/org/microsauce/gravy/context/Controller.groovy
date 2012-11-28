package org.microsauce.gravy.context

/**
* This class encapsulates a controller context.
*/
class Controller {
	String name
	Map<String,Closure> actions = [:]
}