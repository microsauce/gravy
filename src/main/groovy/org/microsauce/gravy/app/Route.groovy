package org.microsauce.gravy.app

import java.util.regex.Pattern
import javax.servlet.DispatcherType

class Route {

	//
	// dispatch types
	//
	Pattern uriPattern
	Class binding
	Set<String> params
	EnumSet<DispatcherType> dispatch
	Closure action
}