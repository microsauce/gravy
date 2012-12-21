package org.microsauce.gravy.context

import groovy.transform.CompileStatic

import java.util.regex.Matcher
import java.util.regex.Pattern

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


class HandlerBinding {

	Pattern uriPattern
	String requestUri
	
	Map<String, String> paramMap = [:]	// uri named parameter map
	List<String> paramList = []			// complete uri parameter value list
	List<String> splat	= []			// wild card 'splat' list
	String json							// a json payload
	
	HandlerBinding(HttpServletRequest req, HttpServletResponse res, Pattern uriPattern, List<String> params) {
		init(req, res, uriPattern, params)
	}
	
	@CompileStatic
	void init(HttpServletRequest req, HttpServletResponse res, Pattern uriPattern, List<String> paramNames) {
		
		this.requestUri = req.requestURI
		this.uriPattern = uriPattern
		
		json = readJsonPayload(req)
		
		Matcher matches =  requestUri =~ uriPattern
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

	}
	
	@CompileStatic private String readJsonPayload(HttpServletRequest req) {
		String json = null
		if ( req.contentType && req.contentType.startsWith('application/json') ) {
			json = req.inputStream.getText('UTF-8')
		}
		json
	}
	
}
