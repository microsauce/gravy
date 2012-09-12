package org.microsauce.gview

import org.microsauce.gravy.template.GStringTemplateEngine
import javax.servlet.http.*
import groovy.json.JsonSlurper
import groovy.util.logging.*
import java.text.SimpleDateFormat
import java.text.DecimalFormat
import static org.codehaus.groovy.runtime.StackTraceUtils.*

/**
* TODO something about the i18n temp/layout is broken
*
*/

@Log4j
class GView {

	def static final Modes = [
		EAGER : 'eager',
		DEV : 'dev',
		LAZY : 'lazy'
	]

	def DEFAULTS = [
		'documentRoot' : '.',
		'mode' : Modes.LAZY,
		'layoutFolder' : 'layouts',
		'encoding' : 'utf-8',
		'defaultLocale' : 'en_US'
	]

	//
	// configurable properties
	//
	def documentRoot
	def mode
	def layoutFolder
	def encoding
	def defaultLocale

	//
	// infrastructure
	//
	def templateEngine
	def jsonSlurper

	//
	// template preprocessors
	//

	def layoutPreprocessor = { sourceUri, source -> // layout
		def layoutPattern = /.*<#(layout|LAYOUT)\s+(.+)>/
		def sectionPattern = /.*<#(section|SECTION)\s+(.+)>/
		def layoutUri
		def currentSection
		def sections = [:]
		def implicitBody = new StringBuilder()

		source.eachLine { line -> 
			if ( !layoutUri ) {
				def matches = line =~ layoutPattern
				if ( matches.size() > 0 ) layoutUri = matches[0][2]
				else implicitBody << line+'\n'
			}
			else {
				def matches = line =~ sectionPattern
				if ( matches.size() > 0 ) {
					sections[matches[0][2]] = new StringBuilder()
					currentSection = matches[0][2]
				}
				else {
					if ( !currentSection ) implicitBody << line+'\n'
					else sections[currentSection] << line+'\n'
				}
			}
		}

		if ( sections.size() == 0 ) sections['body'] = implicitBody

		def buffer = new StringBuilder()
		if ( layoutUri ) {
			new File(documentRoot+'/layout/'+layoutUri).eachLine { line -> // layout subfolder is implicit
				def matches = line =~ sectionPattern
				if ( matches.size() > 0 ) {
					def thisSection = sections[matches[0][2]] 
					if (!thisSection) { /* warning condition */
						log.warn("WARNING: '${matches[0][2]}' section of layout '${layoutUri}' undefined in source file '${sourceUri}'.")
						buffer << line+'\n'
					}
					else buffer << thisSection
				}
				else buffer << line+'\n'
			}
		}
		else buffer << sections.body
			
		[layoutUri, buffer.toString()]
	}
	//
	// TODO this is a dirty hack to work around GROOVY-5367 (still open as of Groovy 2.0.1 (scheduled for 3.0).
	// When resolved I will add the 'underscore' functions at the root level of the template binding
	//
	def addUnderscoreDotPreprocessor = { sourceUri, source ->
		def pattern = /(?<!\.|new )(?<![a-zA-Z0-9])([a-zA-Z0-9_]+\w*\()/ // /(?<!new)(?<!\.)\w*(?<![a-zA-Z0-9])([a-zA-Z0-9_]+\w*\()/
		def matcher = source =~ pattern
		def buffer = new StringBuffer()

		while (matcher.find()) {
			def count = matcher.groupCount()
			for (def i = 1; i <= count; i++) 
				matcher.appendReplacement(buffer, '_.'+matcher.group(i))
		}
		matcher.appendTail(buffer)
		buffer.toString()
	}

	def preprocessors = [addUnderscoreDotPreprocessor]

	//
	// underscore closures
	//
	def _render = { templateUri, model -> // templates (grails style)
		render(templateUri, model)
	}

	def _contextPath = { HttpServletRequest req ->
		req.contextPath == '/' ? '' : req.contextPath
	}

	def _format = { object, pattern, locale = defaultLocale -> // date, currency, number
		def result = object
		try {
			switch ( object.getClass() ) {
				case Date.class:
					def fmt = new SimpleDateFormat(pattern, new Locale(locale))
					result = fmt.format(object)
					break
				case Number.class:
					def fmt = new DecimalFormat(pattern)
					result = fmt.format(object)
					break
				default:
					log.warn("WARNING: format(): unsupported type ${object.getClass()}")
			}
		}
		catch ( all ) {
			log.error("ERROR: unable to format \"$object\" with pattern $pattern:\n\t${all.message}", all)
		}
		return result
	} 

	def _sub = { str, params ->
		def pattern = /(\{[0-9]+\})/
		def matcher = str =~ pattern
		def buffer = new StringBuffer()

		while (matcher.find()) {
			def count = matcher.groupCount()
			for (def i = 1; i <= count; i++) 
				matcher.appendReplacement(buffer, subReplacement(matcher.group(i), params))
		}
		matcher.appendTail(buffer)
		buffer.toString()
	}

	def subReplacement(group, params) {
		def replacement = group
		try {
			def ndx = group.replaceAll('\\{', '').replaceAll('\\}','').toInteger()
			replacement = params[ndx]
			if ( !replacement ) {
				replacement = group
				log.error("Error: substitution not found at index $ndx of parameter list. Group: '$group' params: $params")
			}
		}
		catch ( all ) {
			log.error("Error: unable to parse substitution group: '$group' params: $params\n${all.message}", all)
		}

		replacement
	}

	def _escape = { str ->
		str.replaceAll('<', '&lt;').replaceAll('>', '&gt;') //.replaceAll('\n', ' ').replaceAll('\r', ' ')
	}


	def underscore = [
		_ : [
			sub : _sub,
			render : _render,
			format : _format,
			esc : _escape,
			contextPath : _contextPath
		]
	]

	def templates = [:]

	public GView() { this([:]) }

	public GView(config) {
		configure(config)

		templateEngine = new GStringTemplateEngine()
		jsonSlurper = new JsonSlurper()
	}

	def init() {

		def layoutFilter = ~(documentRoot+'/layout/.*')
		def i18nFilter = /.*\.i18n\.json/

		if ( documentRoot.startsWith('.') )
			documentRoot = documentRoot.replace('.', System.getProperty("user.dir"))

		if ( Modes.EAGER == mode ) {
			new File(documentRoot).eachFileRecurse { file ->
				if ( file.isDirectory() ) return
				def absolutePath = file.getAbsolutePath()
				if ( 
					!( absolutePath ==~ layoutFilter ) && 
					!( absolutePath ==~ i18nFilter )
				) {
					def relativePath = absolutePath - documentRoot
					getTemplate(relativePath)
				}
			}
		} // end if ( Mode.EAGER == mode )

		this
	}

	def configure(config) {

		// for each configurable option
		DEFAULTS.each { key, value ->
			// first check config map
			if ( config[key] ) this[key] = config[key]
			// next check system properties
			else if ( System.properties['GView.'+key] ) 
				this[key] = System.properties['GView.'+key]
			// last use sensible default
			else this[key] = value
		}

	}

	def i18n(view, model) {
		def result = [:]

		if ( view.i18n ) {
			def locale = model.locale ?: defaultLocale
			if ( view.layoutUri && view.i18n[view.layoutUri] && view.i18n[view.layoutUri][locale] )
				result << view.i18n[view.layoutUri][locale] 
			if ( view.i18n[view.uri] && view.i18n[view.uri][locale] )
				result << view.i18n[view.uri][locale] 
		}
		result
	}

	/**
	* Render the view identified by uri bound to the given data model
	* 
	* @param uri - the uri of the view to render
	* @param model - the model
	*/
	def render(String uri, Map model) { 

		def result = null
		try {
			def view = getTemplate(uri)
			log.info("view URI $uri")
			if ( !view ) return null
			model << underscore // bind the underscore closures to the model
			model << [i18n: i18n(view, model)]//[i18n: view.i18n] 
			result = view?.compiledTemplate.make(model)
		}
		catch ( all ) {
			log.error("ERROR: failed to render view ${uri}:\n${all.message}", all)
//			all.printStackTrace()
			throw all
		}
		if ( !result )
			log.error("ERROR: Template $uri not found")
		result
	}

	def sourceModified(template) {
		template.timestamp < modifiedDate(template)
	}

	def modifiedDate(template) {
		def modDates = []
		modDates << tStamp(documentRoot + '/' + template.uri)
		modDates << tStamp(documentRoot + '/' + template.uri+'.i18n.json')
		modDates << tStamp(documentRoot + '/layout/' + template.layoutUri)
		modDates << tStamp(documentRoot + '/layout/' + template.layoutUri+'.i18n.json')
		modDates.max()
	}

	private class Template {
		def compiledTemplate
		def i18n 

		// metadata		
		def uri
		def layoutUri	
		def timestamp 
	}

	def getTemplate(uri) {
		def template = templates[uri]
		if ( !template || ( Modes.DEV == mode && sourceModified(template) ) ) {
			// read the template file
			def absolutePath = documentRoot+uri
			log.info("absolute path: $absolutePath")
			def file = new File(absolutePath)
			if ( file.exists() ) {
				def fileAsText = file.getText(encoding)

				// pre-process the template
				def (layoutUri, processedText) = layoutPreprocessor(absolutePath, fileAsText)
				def layoutAbsolutePath = layoutUri ? documentRoot+'/layout/'+layoutUri : null
				preprocessors.each { thisProcessor ->
					processedText = thisProcessor(absolutePath, processedText)
				}

				def compiledTemplate
				try {
					compiledTemplate = templateEngine.createTemplate(processedText)
				}
				catch (all) {
					log.error("Error: failed to compile template $absolutePath:\n${all.message}\n===\n${processedText}", all)
					all.printStackTrace()
					throw all
				}

				def i18n = [:]
				i18n[uri] = loadI18nResource(absolutePath+'.i18n.json')
				if ( layoutAbsolutePath ) i18n[layoutUri] = loadI18nResource(layoutAbsolutePath+'.i18n.json')

				def timestamp = [
					tStamp(absolutePath), 
					tStamp(absolutePath+'.i18n.json'),
					tStamp(layoutAbsolutePath),
					tStamp(layoutAbsolutePath+'.i18n.json')
				].max()

				template = new Template([
					uri: uri,
					i18n: i18n,
					layoutUri: layoutUri,
					timestamp: timestamp,
					compiledTemplate: compiledTemplate
				])
				templates[uri] = template
			} else log.error("Error: $uri source not found")
		}
		template
	}

	def tStamp(path) {
		if ( path == null ) return 0
		new File(path).lastModified()
	}

	def loadI18nResource(resourcePath) {
		def i18nFile = new File(resourcePath)

		def result = null
		if ( i18nFile.exists() ) {
			def fileAsText = i18nFile.getText(encoding)

			try {
				result = jsonSlurper.parseText( fileAsText )
			}
			catch (all) {
				log.error("Error: unable to parse i18n resource ${absolutePath}:\n\t${all.message}", all)
				return
			}
		}
		result
	}

}