package org.microsauce.gravy.module.groovy

import groovy.json.JsonBuilder
import groovy.transform.CompileStatic

import java.util.regex.Pattern

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession

import org.microsauce.gravy.context.Context
import org.microsauce.gravy.json.GravyJsonSlurper
import org.microsauce.gravy.lang.groovy.api.GroovyAPI
import org.microsauce.gravy.lang.groovy.script.Script
import org.microsauce.gravy.lang.groovy.script.ScriptDecorator
import org.microsauce.gravy.lang.groovy.script.ScriptUtils
import org.microsauce.gravy.module.Module

class GroovyModule extends Module { // TODO pull code in from ScriptUtils,  

	Pattern datePattern = ~/[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}/
	
	@CompileStatic protected Object doLoad(Map binding) {
		patchEnterpriseRuntime()
		// TODO for now use the 'old' groovy script utilities
		ConfigObject root = new ConfigObject()
		binding.root = root
		binding.config = config
		
		// create, initialize, and execute the script
		Script script = new Script()
		script.binding.putAll(binding) 
//		script.config = config 			// TODO why is this necessary ??? 
		script.classLoader = classLoader
		script.name = name
		script.sourceUri = scriptFile.absolutePath
		if ( folder.exists() ) {
			script.roots << folder.absolutePath
			File scriptsFolder = new File(folder, '/scripts')
			if ( scriptsFolder.exists() )
				script.roots << folder.absolutePath+"/scripts"
		}
		addClosure script.binding
		GroovyAPI.module = this

		Object _return = ScriptUtils.run script
		
		_return 
	}

	void addClosure(Map binding) {
		binding.run = { name, scriptBinding = null ->
			def subScript = new Script(
				[sourceUri: name+'.groovy', binding: [config:config, app:app], roots: script.roots, classLoader: script.classLoader])
			new ScriptDecorator(config, context).decorate(subScript)
			ScriptUtils.run(subScript)
		}
	}
	
	private void patchEnterpriseRuntime() {
		//
		// TODO the following Binding.metaClass assignments are a workaround
		// for GROOVY-5367 (still open as of Groovy 2.0.4/scheduled for 3.0).
		// When resolved I will add render and forward to the closure delagate
		// binding rather than the Binding metaClass.
		//
		Binding.metaClass.forward = { String uri ->
			def rd = req.getRequestDispatcher(uri)
			rd.forward(req, res)
		}
		Binding.metaClass.redirect = { String uri ->
			res.sendRedirect(uri)
		}
		Binding.metaClass.include = { String uri ->
			def rd = req.getRequestDispatcher(uri)
			rd.include(req, res)
		}
		Binding.metaClass.render = { String viewName, Map model ->
			res.contentType = 'text/html'
			req.setAttribute('_view', viewName)
			def attrModel = model
			if ( serializeAttributes ) {
				attrModel = new JsonBuilder(model).toString()
			} 
			req.setAttribute('_model', attrModel)
			req.setAttribute('_module', this)
			def rd = req.getRequestDispatcher(renderUri)
			rd.forward(req, res)
		}
		Binding.metaClass.renderJSON = { def model ->
			res.contentType = 'application/json'
			res.writer << new JsonBuilder(model).toString()
		}

		def get = { key ->
			def value = delegate.getAttribute(key)
			if ( serializeAttributes ) {
				value = new GravyJsonSlurper().parseText(value) { k, val ->
					def retValue = val
					if ( val instanceof String && val.length() >= 19) {
						def substr = val.substring(0,19)
					
						if ( substr ==~ datePattern ) {
							retValue = Date.parse("yyyy-MM-dd'T'HH:mm:ss", substr)
						}
					}
					retValue
				}
			}
			value
		}
		def put = { key, value ->
			def attrValue = value
			if ( serializeAttributes ) {
				attrValue = new JsonBuilder(value).toString()
			}
			
			delegate.setAttribute(key, attrValue)			
		}
		HttpServletRequest.metaClass.get = get
		HttpSession.metaClass.get = get
		HttpServletRequest.metaClass.put = put
		HttpSession.metaClass.put = put
		HttpServletRequest.metaClass.parm = { key ->
			delegate.getParameter(key)
		}

	}

}
