package org.microsauce.gravy.server.runtime

import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.ServletResponse
import javax.servlet.ServletRequest
import javax.servlet.Filter
import javax.servlet.FilterChain
import org.microsauce.gravy.app.*

import org.microsauce.gravy.app.ApplicationContext
import org.microsauce.gravy.server.util.ServerUtils
import org.microsauce.gravy.ErrorHandler
import groovy.util.logging.Log4j
import groovy.transform.CompileStatic


@Log4j
class ControllerFilter implements Filter {

	def static final CONTROLLER_NAME = 1
	def static final ACTION_NAME = 2
	def static final ENTITY_ID = 3

	def controllerUriPattern 
	ErrorHandler errorHandler

	public ControllerFilter() {
		errorHandler = ErrorHandler.getInstance()
		controllerUriPattern = ~ '\\/([^\\/]*)\\/([^\\/]*)(?:\\/([0-9]*))?'
	}

	@CompileStatic
	void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)  {
	
		HttpServletRequest req = (HttpServletRequest) request
		HttpServletResponse res = (HttpServletResponse) response
		log.debug "handling request uri ${req.requestURI}"

		// identify controller action
		Closure action = parseAndBind req, res
		try {
			if ( !action ) {
				res.status = HttpServletResponse.SC_NOT_FOUND 
			} else {
				action.call()
				res.writer.flush() 
			}
		}
		catch ( Exception all ) {
			all.printStackTrace()
			errorHandler.handleError HttpServletResponse.SC_INTERNAL_SERVER_ERROR, all.message, req, res, all 
		}
		finally {
			if ( !res.isCommitted() )
				chain.doFilter(req, res)
		}
	}

	def private handleError(req, res, message, status) { 
		log.error "an error occurred handling request for uri ${req.requestURI}: $message"
		res.status = status
		def rd = req.getRequestDispatcher("/error$status")
		rd.forward(req, res)
	}

	@CompileStatic
	private Closure parseAndBind(HttpServletRequest req, HttpServletResponse res) {
		String uri = ServerUtils.getUri(req)

		String controllerName = null
		String actionName = null
		if (uri == '/') {
			controllerName = ''
			actionName = '/'
		} else {

			List<String> parts = []
			for (String it in uri.split('/')) {
				if (it != '')
					parts.add(it)
			}

			if (parts.size() == 0) return null
		
			if (parts.size() == 1) {
				controllerName = ''
				actionName = parts[0]
			}
			else {

				StringBuilder buffer = new StringBuilder()

				parts[0..<(parts.size()-1)].each {
					buffer << '/'+it
				}
				controllerName = buffer.toString()
				actionName = parts[parts.size()-1]
			}
		}

		Controller controller = ApplicationContext.getInstance().findController(controllerName)
		if ( !controller ) return null
		Closure action = controller.actions[actionName]
		if ( !action ) return null
		def binding = [:]
		binding.controller = controllerName
		binding.action = actionName
		binding << ServerUtils.buildContext(req, res, null)

		ActionUtils.cloneAndBind(action, binding)
	}

	def private routeAndBind(HttpServletRequest req, HttpServletResponse res) {
		def action = null
		def matches = req.requestURI =~ controllerUriPattern
		def size = matches.size() > 0 ? matches[0].size() : 0
		def binding = [:] 
		def controllerName
		def actionName

		switch ( size ) {
			case 3:
				controllerName = matches[0][CONTROLLER_NAME]
				actionName = matches[0][ACTION_NAME]
				break
			default:
				log.warn "not found: ${req.requestURI}"
				return null
		}
		def controller = ApplicationContext.getInstance().findController(controllerName)
		if ( !controller ) return null
		action = controller.actions[actionName]
		if ( !action ) return null

		binding.controller = controllerName
		binding.action = actionName
		binding << ServerUtils.buildContext(req, res, controller.binding)

		action.delegate = binding as Binding
		action
	}

	void destroy() {}

	void init(javax.servlet.FilterConfig config) {}

}
