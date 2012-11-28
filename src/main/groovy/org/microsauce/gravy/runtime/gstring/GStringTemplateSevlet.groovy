package org.microsauce.gravy.runtime.gstring

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j

import org.microsauce.gravy.runtime.GravyTemplateServlet
import org.microsauce.gravy.runtime.resolver.ResourceResolver
import org.microsauce.gravy.template.gstring.GStringTemplateEngine

@Log4j
class GStringTemplateServlet extends GravyTemplateServlet {

	private GStringTemplateEngine view
	private ResourceResolver resolver

	GStringTemplateServlet() {}

	GStringTemplateServlet(documentRoot, runMode) {
		super(documentRoot, runMode)

		view = new GStringTemplateEngine()
		view.mode = runMode
		view.documentRoot = documentRoot
	}

	@CompileStatic
	void render(String viewUri, Map model, PrintWriter printWriter) {
		printWriter << view.render(viewUri, model)
	}
}
