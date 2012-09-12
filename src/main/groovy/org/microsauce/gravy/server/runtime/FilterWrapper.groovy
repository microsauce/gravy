package org.microsauce.gravy.server.runtime

import javax.servlet.Filter
import javax.servlet.DispatcherType

class FilterWrapper {
	Filter filter
	String mapping
	EnumSet<DispatcherType> dispatch
}