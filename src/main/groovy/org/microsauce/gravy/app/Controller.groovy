package org.microsauce.gravy.app

/**
* This class encapsulates a controller context.
*/
class Controller {
	String name
	Map<String,Closure> actions = [:]
}