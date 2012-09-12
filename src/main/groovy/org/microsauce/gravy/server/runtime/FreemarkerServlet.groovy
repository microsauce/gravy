package org.microsauce.gravy.server.runtime

import freemarker.template.*
import groovy.util.logging.Log4j
import groovy.transform.CompileStatic

@Log4j
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
