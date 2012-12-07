package org.microsauce.gravy.context

import java.util.regex.Pattern

class EnterpriseService extends Service {
	
	static String GET = 'get'
	static String POST = 'post'
	static String PUT = 'put'
	static String OPTIONS = 'options'
	static String DELETE = 'delete'
	static String DEFAULT = 'default'
	
	Pattern uriPattern
	String uriString
}
