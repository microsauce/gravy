
import org.microsauce.gravy.template.gstring.GStringTemplateEngine


def appRoot = config.appRoot
def documentRoot =  config.gravy.documentRoot ?: appRoot+'/WEB-INF/view'
def serviceUri = config.serviceUri ?: '/view/gstring' 
def engines = [:]

//
// lazy load a template engine instance for each module
//
def templateEngine = { moduleName ->
	def engine = engines[moduleName]
	if ( !engine ) {
		engine = new GStringTemplateEngine()
		engine.mode = config.runMode ?: 'prod'
		engine.documentRoot = documentRoot+'/'+moduleName
		engines[moduleName] = engine
	}
	engine
}

route serviceUri, {
	def model = req.get '_model'
	def viewUri = req.getAttribute '_view' 	// not serialized
	def module = req.getAttribute '_module'	// not serialized
	def moduleName = module.name
	res.contentType = 'text/html'
	
	out << templateEngine(module.name).render(viewUri, model)
	out.flush()
}
