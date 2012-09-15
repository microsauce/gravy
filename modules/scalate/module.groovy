
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.ServletResponse
import javax.servlet.ServletRequest
import javax.servlet.Filter
import javax.servlet.FilterChain
import org.microsauce.gravy.server.util.ServerUtils
import org.microsauce.gravy.server.runtime.GravyTemplateServlet
import org.fusesource.scalate.japi.* 
import groovy.transform.CompileStatic

class ScalateServlet extends GravyTemplateServlet {

	private TemplateEngineFacade templateEngine

	ScalateServlet(documentRoot, runMode) {
		super(documentRoot, runMode)

		templateEngine = getTemplateEngine()
	}

	@CompileStatic
	void render(String viewUri, Map model, PrintWriter printWriter) {
		log.debug "scalate: render view $viewUri"
		templateEngine.layout(viewUri, printWriter, model)
	}

	def getTemplateEngine() {
		System.setProperty('scalate.mode', runMode) // TODO verify
		TemplateEngineFacade engine = new TemplateEngineFacade()
		engine.sourceDirectories = [new File(documentRoot)]
		engine
	}
}

def runMode = config.scalate.mode ?: 'production'
def appRoot = config.appRoot
def documentRoot =  appRoot+(config.scalate.documentRoot ?: '/view')
def viewUri = config.gravy.viewUri ?: '/view/renderer'

def viewServlet = new ScalateServlet(documentRoot, runMode)
servlet(viewUri, viewServlet)
