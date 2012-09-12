package org.microsauce.gravy.server.runtime

import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.ServletResponse
import javax.servlet.ServletRequest
import javax.servlet.Filter
import javax.servlet.FilterChain
import org.microsauce.gravy.server.util.ServerUtils
import org.microsauce.gview.GView
import groovy.util.logging.Log4j
import groovy.transform.CompileStatic

@Log4j
class GViewServlet extends GravyTemplateServlet {

	private GView view

	GViewServlet() {}

	GViewServlet(documentRoot, runMode) {
		super(documentRoot, runMode)

		view = new GView()
		view.mode = runMode
		view.documentRoot = documentRoot
	}

	@CompileStatic
	void render(String viewUri, Map model, PrintWriter printWriter) {
		printWriter << view.render(viewUri, model)
	}
}
