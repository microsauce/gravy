package org.microsauce.gravy.server.util

import java.text.SimpleDateFormat
import groovy.util.logging.Log4j

/**
* Mapper is a utility for binding http request parameters to a Groovy/Java
* data bean.  
*/
@Log4j
public class Mapper {
	private static Mapper instance

	def collectionPattern = /([0-9a-zA-Z_]+)\[([0-9]+)\]/
	def nestedObjectPattern = /([0-9a-zA-Z_]+)\.([0-9a-zA-Z_\[\]]+)/

	def dateFormat = 'MM/dd/yyyy' 
	def dateParser = new Date()

	def cache = [:]

	private Mapper() {}

	static Mapper getInstance() {
		if ( !instance ) instance = new Mapper()
		instance
	}

	/**
	* 
	*/
	def bindRequest(clazz, req) {
		def typeMapping = buildParameterTypeMapping(clazz)

		def (parameterMap, errors) = buildParameterMap(req, typeMapping)
		def binding = clazz.newInstance(parameterMap)
		binding.metaClass.errors = errors ?: []
		binding
	}

	def buildParameterTypeMapping(Class clazz) {
		def classMapping = cache[clazz]
		if ( !classMapping ) classMapping = doBuildParameterTypeMapping(clazz, true, null)
		classMapping
	}

	def doBuildParameterTypeMapping(Class clazz, recurse, typeStack) {

		// cyclical reference check
		if ( typeStack ) {
			if ( refCount( clazz, typeStack ) > 0 ) return
			else typeStack << clazz
		}
		else {
			typeStack = []
			typeStack << clazz
		}

		def paramTypes = [:]
		clazz.getDeclaredFields().each { field ->
			def name = field.name

			if ( !field.synthetic ) {
				def type = field.type
		
				if ( Number.isAssignableFrom( type ) || 
					type in [ String, Date, int, long, float, double, short ]) 
					paramTypes[name] = type
				else if ( Collection.isAssignableFrom( type ) ) {
					def prop = clazz.getDeclaredField(name)
					def propListType =  prop.getGenericType()
					def propListClass = propListType.actualTypeArguments[0]

					def collectionName = name+'[]'
					if ( Number.isAssignableFrom( propListClass ) || propListClass == String || propListClass == Date )
						paramTypes[collectionName] = propListClass
					else { // user defined type
						doBuildParameterTypeMapping(propListClass, true, typeStack).each { f1Name, f1Value ->
							paramTypes[collectionName+'.'+f1Name] = f1Value
						}
					}
				} else if (recurse) { // user defined type
					doBuildParameterTypeMapping(type, true, typeStack).each { f1Name, f1Value ->
						paramTypes[name+'.'+f1Name] = f1Value
					}
				}

			}
		}
		paramTypes
	}

	def refCount(clazz, stack) {
		def counter = 0
		for ( thisClazz in stack ) {
			if ( thisClazz == clazz) counter++
		}
		counter
	}

	def buildParameterMap(req, parameterTypes) {
		def parameterMap = [:]  // root
		def errors = []

		def fullNameValue = [:] as LinkedHashMap
		for ( fullName in req.getParameterNames() ) {
			try {
				def value = convertValue(fullName, parameterTypes, req)
				if ( value ) {
					fullNameValue[fullName] = value
				}

			} catch ( all ) {
				errors << [parameter: fullName, value: req.getParameter(fullName), exception: all]
				all.printStackTrace()
			}
		}
		fullNameValue.sort() // TODO verify

		// normalize
		def list = []
		fullNameValue.each { key, value ->
			merge(parameterMap, buildSliceForParm( key, value ) )
		}

		[parameterMap, errors]
	}

	def private merge(target, source) {
		def thisKey = source.keySet().iterator().next()

		if ( thisKey ==~ collectionPattern ) {  
			def matches = thisKey =~ collectionPattern
			def collectionName = matches[0][1]
			def ndx = matches[0][2] as int

			if ( target[collectionName] == null ) { 
				target[collectionName] = []
			}
			if ( target[collectionName][ndx] ) { 
				target[collectionName][ndx] << source[thisKey]
			}
			else { 
				target[collectionName][ndx] = source[thisKey] 
			}

			if ( source[thisKey] instanceof Map ) { 
				merge( target[collectionName][ndx], source[thisKey] )
			}
		} else {
			if ( target[thisKey] == null ) {
				target[thisKey] = source[thisKey]
			}
			else {
				def isList = false
				def sourceElement = source[thisKey]
				if ( sourceElement instanceof Map ) {
					// filter out [] elements they are handled above
					def mapKey = sourceElement.keySet().iterator().next()
					if ( mapKey ==~ collectionPattern ) {
						target[thisKey] << [:]
						isList = true
					}
				}
				if (!isList && source[thisKey] instanceof Map) {
					target[thisKey] << source[thisKey]  // this is the one
				}
			}

			if ( source[thisKey] instanceof Map ) {
				merge(target[thisKey], source[thisKey])
			}
		}
	}
	def private buildSliceForParm(parameter, value) {
		def split = parameter.split('\\.')
		def slice = [:]
		def ret = slice
		for ( def i = 0; i < split.size()-1; i++ ) {
			slice[split[i]] = [:]
			slice = slice[split[i]]
		}
		slice[split[split.size()-1]] = value
		ret
	}
	def private convertValue(name, parameterTypes, req) {
		def value = null
		def propertyType = parameterTypes[lookupKey( name )] 
		if ( !propertyType ) { log.debug("cannot bind parameter $name to class"); return value }
		if ( propertyType == Date ) {
			def dateParser = new SimpleDateFormat(getDateFormat(name, req))
			value = dateParser.parse(req.getParameter(name)) 
		}
		else value = req.getParameter(name).asType( propertyType )

		value
	}

	def lookupKey(String parameterName) {
		parameterName.replaceAll('\\[[0-9]+\\]', '[]')
	}

	def getDateFormat(name, req) {
		def df = req.getParameter("${name}_dateFormat")
		if ( !df )
			df = req.getParameter('dateFormat') ?: dateFormat
		
		df
	}

}