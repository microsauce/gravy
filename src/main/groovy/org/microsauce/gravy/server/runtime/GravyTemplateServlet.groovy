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
import org.microsauce.gravy.resovler.ResourceResolver

@Log4j
abstract class GravyTemplateServlet extends HttpServlet {

	public static List<String> roots = []

	protected String documentRoot
	protected String runMode
	private ResourceResolver resolver

	GravyTemplateServlet(documentRoot, runMode) {
		this.documentRoot = documentRoot
		this.runMode = runMode
		resolver = new ResourceResolver(documentRoot)
println "GravyTemplateServlet GravyTemplateServlet.roots = ${GravyTemplateServlet.roots}"		
		resolver.roots.addAll(GravyTemplateServlet.roots) 
	}

	protected abstract void render(String viewUri, Map model, PrintWriter printWriter)

	@CompileStatic
	public void service(HttpServletRequest req, HttpServletResponse res) {
		String viewUri = getViewUri(req)
		Map model = getModel(req)
		model << [req: req, res: res]
		try {
			render(viewUri, getModel(req), res.writer)
		}
		catch (Exception all) {
			res.status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR 
			res.contentType = 'text/html'
			res.writer << "<h1>500</h1><br/>Failed to render view $viewUri:<br/><pre>"
			all.printStackTrace(res.writer)
			res.writer << "</pre>"
		}
	}

	@CompileStatic
	protected Map getModel(HttpServletRequest req) {
		(Map)req.getAttribute('_model')
	}

	@CompileStatic
	protected String getViewUri(HttpServletRequest req) {
		String viewName = req.getAttribute('_view')
		String controller = (String)req.getAttribute('_controller')
		String viewUri = viewName.contains('/') ? viewName : '/'+controller+'/'+viewName
		resolver.realUri viewUri
	}

}
