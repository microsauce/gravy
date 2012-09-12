package org.microsauce.gravy.app

import java.util.regex.Pattern
import javax.servlet.DispatcherType
import groovy.transform.CompileStatic

class Route {

	//
	// dispatch types
	//
	Pattern uriPattern
	Class binding
	Set<String> params
	EnumSet<DispatcherType> dispatch
	Closure action // TODO rename default
//	Closure defaultAction = 

	Closure get = null
	Closure head = null 
	Closure put = null 
	Closure post = null 
	Closure delete = null 
	Closure options = null 

//	@CompileStatic
	Closure getAction(String method) {
		Closure thisAction = (Closure)this[method]
		if ( !thisAction )
			thisAction = action
		if ( !thisAction ) thisAction = {
			out << "no action defined for uri $uriPattern method $method"
		}

		thisAction
	}

}