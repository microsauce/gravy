
import freemarker.cache.*
import freemarker.template.*
import java.util.concurrent.ConcurrentHashMap

def documentRoot =  config.gravy.documentRoot ?: System.getProperty('gravy.viewRoot')
def serviceUri = config.serviceUri ?: '/view/freemarker'
def engines = new ConcurrentHashMap()
// TODO test caching behavior in WAR mode
def templates = new ConcurrentHashMap()

//
// lazy load a template engine instance for each module
//
def templateEngine = { moduleName ->
	def engine = engines[moduleName]
	if ( !engine ) {
		// the second argument tells the loader to follow symlinks (necessary in dev mode)
		def templateLoader = new FileTemplateLoader(new File(documentRoot+'/'+moduleName), true)
		engine = new Configuration()
		engine.setObjectWrapper(new DefaultObjectWrapper())
		engine.setTemplateLoader(templateLoader)
	}
	engine
}

route serviceUri, {

	def model = req.get '_model'
    def viewUri = req.getAttribute '_view' 	// not serialized
    def module = req.getAttribute '_module'	// not serialized
    res.contentType = 'text/html'

    def template = templates[viewUri]
    if ( !template ) {
        template = templateEngine(module.name).getTemplate(viewUri)
        templates[viewUri] = template
    }
    if ( template )	{
        template.process(model, res.printer)
        res.printer.flush()
    }
    else {
        // TODO
    }
}

