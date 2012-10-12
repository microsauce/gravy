
import freemarker.template.*
import groovy.util.logging.Log4j
import groovy.transform.CompileStatic

class FreemarkerServlet extends GravyTemplateServlet {

	private Configuration templateEngine

	FreemarkerServlet(documentRoot, runMode) {
		super(documentRoot, runMode)

        templateEngine = new Configuration()
        templateEngine.setDirectoryForTemplateLoading(
            new File(documentRoot))
        templateEngine.setObjectWrapper(
        	new DefaultObjectWrapper())
	}

	@CompileStatic
	void render(String viewUri, Map model, PrintWriter printWriter) {
		Template temp = templateEngine.getTemplate(viewUri)

        temp.process(model, printWriter)
        printWriter.flush()
	}

}

def runMode = config.freemarker.mode ?: 'prod'
def appRoot = config.appRoot
def documentRoot =  appRoot+'/WEB-INF/view'
def viewUri = config.gravy.viewUri ?: '/view/renderer'

def viewServlet = new FreemarkerServlet(documentRoot, runMode)
servlet(viewUri, viewServlet)

