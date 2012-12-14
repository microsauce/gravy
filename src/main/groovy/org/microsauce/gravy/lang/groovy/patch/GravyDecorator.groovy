package org.microsauce.gravy.lang.groovy.patch

import groovy.json.JsonBuilder
import javax.servlet.http.*
import javax.servlet.*

import org.microsauce.gravy.module.config.Config;


// TODO move this code to GroovyModule
class GravyDecorator {

	private static conf = Config.getInstance().get()

	def static decorateBinding() {
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
			req.setAttribute('_model', model)
			def rd = req.getRequestDispatcher(conf.gravy.viewUri)
			rd.forward(req, res)
		}
		Binding.metaClass.renderJSON = { def model ->
			res.contentType = 'application/json'
			res.writer << new JsonBuilder(model).toString()
		}

	}

	def static decorateHttpServletRequest() {
		HttpServletRequest.metaClass.toObject = { clazz ->
			Mapper.getInstance().bindRequest( clazz, delegate )
		}
		def attr = { key, value = null ->
			if ( value == null )
				return delegate.getAttribute(key)
			else
				delegate.setAttribute(key, value)

			value
		}
		HttpServletRequest.metaClass.attr = attr
		HttpSession.metaClass.attr = attr
		HttpServletRequest.metaClass.parm = { key ->
			delegate.getParameter(key)
		}

	}

}