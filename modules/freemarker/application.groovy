import freemarker.cache.*
import freemarker.template.*
import java.util.concurrent.ConcurrentHashMap

def documentRoot = conf.gravy.documentRoot ?: System.getProperty('gravy.viewRoot')
def serviceUri = conf.serviceUri ?: '/view/freemarker'
def engines = new ConcurrentHashMap()

//
// lazy load a template engine instance for each module
//
def templateEngine = { moduleName ->
    def engine = engines[moduleName]
    if (!engine) {
        // the second argument tells the loader to follow symlinks (necessary in dev mode)
        def templateLoader = new FileTemplateLoader(new File(documentRoot + '/' + moduleName), true)
        engine = new Configuration()
        engine.setObjectWrapper(new DefaultObjectWrapper())
        engine.setTemplateLoader(templateLoader)
        engines[moduleName] = engine
    }
    engine
}

route serviceUri, {

    def model = req._model
    def viewUri = req.getAttribute '_view'                // not serialized
    def docRoot = req.getAttribute '_document_root'    // not serialized
    res.contentType = 'text/html'

    def template = templateEngine(docRoot).getTemplate(viewUri)
    if (template) {
        template.process(model, res.printer)
        res.printer.flush()
    } else throw new RuntimeException("Template not found for uri $viewUri")
}

