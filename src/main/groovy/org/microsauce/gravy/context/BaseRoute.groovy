package org.microsauce.gravy.context

class BaseRoute {
	
	Closure action 
	Closure get = null
	Closure head = null 
	Closure put = null 
	Closure post = null 
	Closure delete = null 
	Closure options = null 
}