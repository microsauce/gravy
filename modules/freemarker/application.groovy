
import freemarker.template.*
import freemarker.cache.*
import groovy.util.logging.Log4j
import groovy.transform.CompileStatic

def appRoot = config.appRoot
def documentRoot =  config.gravy.documentRoot ?: appRoot+'/WEB-INF/view'
def serviceUri = config.serviceUri ?: '/view/freemarker'
def engines = [:]

//
// lazy load a template engine instance for each module
//
def templateEngine = { moduleName ->
	def engine = engines[moduleName]
	if ( !engine ) {
		// the second argument tells the loader to follow symlinks (necessary in dev mode)
		def templateLoader = new FileTemplateLoader(new File(documentRoot+'/'+moduleName), true)
		engine = new Configuration()
//		engine.setDirectoryForTemplateLoading(
//			new File(documentRoot))
		engine.setObjectWrapper(
			new DefaultObjectWrapper())
		engine.setTemplateLoader(templateLoader)
	}
	engine
}

route serviceUri, {
	def model = req.get '_model'
	def viewUri = req.getAttribute '_view' 	// not serialized
	def module = req.getAttribute '_module'	// not serialized
	def moduleName = module.name

	Template temp = templateEngine(module.name).getTemplate(viewUri)
	temp.process(model, out)
	out.flush()
}

