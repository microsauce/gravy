package org.microsauce.gravy.app

import java.util.regex.Pattern
import javax.servlet.DispatcherType
import groovy.transform.CompileStatic


class Route {

	Pattern uriPattern
	Class binding
	List<String> params
	List<DispatcherType> dispatch

	Closure handler 
	Closure get = null
	Closure head = null 
	Closure put = null 
	Closure post = null 
	Closure delete = null 
	Closure options = null 


	EnumSet<DispatcherType> getDispatches() {
		EnumSet.copyOf(dispatch)
	}

	Closure getAction(String method) {
		Closure thisAction = (Closure)this[method]
		if ( !thisAction )
			thisAction = handler
		if ( !thisAction ) thisAction = {
			out << "no handler defined for uri $uriPattern method $method"
		}

		thisAction
	}

}