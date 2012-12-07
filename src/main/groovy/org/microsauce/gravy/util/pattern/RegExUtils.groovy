package org.microsauce.gravy.util.pattern

import java.util.regex.Pattern
import java.util.regex.Matcher
import groovy.transform.CompileStatic

class RegExUtils {

	static String[] escapeCharacters = ['\\.','\\+','\\?','\\[','\\]','\\^','\\$']

	@CompileStatic
	static List<String> parametersInOrder(String uriPattern) {
		LinkedHashMap<Integer,String> parameters = new LinkedHashMap<Integer,String>() 
		String namedParmPattern=':([a-zA-Z]+)'
		String wildCardPattern='(\\*)'
		Pattern[] patterns = [Pattern.compile(namedParmPattern), Pattern.compile(wildCardPattern)]

		for (int j = 0; j<patterns.length; j++) {
			Matcher matcher = patterns[j].matcher(uriPattern)

			while (matcher.find())
				parameters[matcher.start()] = matcher.group(1)
		}

		List<Integer> keyList = parameters.keySet() as List<Integer>
		keyList.sort()

		keyList.collect { Integer thisKey ->
	 		parameters[thisKey]
		} as List<String>
	}

	@CompileStatic
	private static String escapedUriPattern(String uriPattern) {
		escapeCharacters.each { String character ->
			uriPattern = uriPattern.replaceAll(character, '\\'+character)
		}
		uriPattern
	}

	@CompileStatic
	static Map<String,Object> parseRoute(String uriExpression) {
		uriExpression = escapedUriPattern uriExpression
		String optionalNamedParamPattern = '(.\\?:([a-zA-Z]+)\\?)'
		String optionalReplacementPattern = '.{0,1}(.*)'
		String namedParmPattern = ':([a-zA-Z]+)'
		String wildCardPattern = '(\\*)'
		String replacementPattern = '(.+)'

		List<String> params = parametersInOrder(uriExpression)
		String uriPattern = routePattern(uriExpression, wildCardPattern, replacementPattern)
		uriPattern = routePattern(uriPattern, optionalNamedParamPattern, optionalReplacementPattern)
		uriPattern = routePattern(uriPattern, namedParmPattern, replacementPattern)

		Pattern compiledPattern = ~ uriPattern

		[uriPattern: compiledPattern, params: params] as Map<String,Object>
	}

	@CompileStatic
	private static String routePattern(String uriExpression, String currentPattern, String replacementPattern) {
		StringBuffer buffer = new StringBuffer()
		Matcher matches = uriExpression =~ currentPattern
		while (matches.find()) {
			matches.appendReplacement(buffer, replacementPattern)
		}
		matches.appendTail(buffer)
		buffer.toString()
	}

}