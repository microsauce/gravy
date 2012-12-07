package org.microsauce.gravy.context

import groovy.transform.CompileStatic

import java.util.regex.Matcher
import java.util.regex.Pattern

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.microsauce.gravy.util.ServerUtils


class HandlerBinding {

	Pattern uriPattern
	String requestUri
	
	Map<String, Object> binding = [:]	// base binding: req object, res object, chain
	Map<String, String> paramMap = [:]	// uri named parameter map
	List<String> paramList = []			// complete uri parameter value list
	List<String> splat	= []			// wild card 'splat' list
	
	HandlerBinding(HttpServletRequest req, HttpServletResponse res, Pattern uriPattern, List<String> params) {
		init(req, res, uriPattern, params)
	}
	
	@CompileStatic
	void init(HttpServletRequest req, HttpServletResponse res, Pattern uriPattern, List<String> paramNames) {
		// TODO what todo with this ? auto-bind as 'form' ???
		//		if ( this. ) {
		//			def className = classBinding.name
		//			def contextName = className.substring(0,1).toLowerCase() + className.substring(1)
		//			def classBindingInstance = Mapper.getInstance().bindRequest( classBinding, req )
		//
		//			binding[contextName] = classBindingInstance
		//		}
		
		this.requestUri = req.requestURI
		this.uriPattern = uriPattern
		
		Matcher matches =  requestUri =~ uriPattern
		binding = [:]
		Integer ndx = 1
		splat = []
		if ( paramNames.size() > 0 ) {
			while (matches.find()) {
				Integer groupCount = matches.groupCount()
				for (;ndx<=groupCount;ndx++) {
					String thisParamName = paramNames[ndx-1]
					String paramValue = matches.group(ndx)
					paramList << paramValue
					if ( thisParamName == '*' )
						splat << paramValue
					else {
						paramMap[thisParamName] = paramValue
					}
				}
			}
		} else if (matches.groupCount() > 0) {
			Integer groupCount = matches.groupCount()
			while (matches.find()) {
				for (;ndx <= groupCount; ndx++ ) {
					splat << matches.group(ndx)
				}
			}
		}

		binding.route = uriPattern.toString()
		binding.chain = this
		binding << ServerUtils.buildContext(req, res, null)

	}
	
}
