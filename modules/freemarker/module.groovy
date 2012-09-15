
import org.microsauce.gravy.server.runtime.FreemarkerServlet

def runMode = config.freemarker.mode ?: 'prod'
def appRoot = config.appRoot
def documentRoot =  appRoot+(config.freemarker.documentRoot ?: '/view')
def viewUri = config.gravy.viewUri ?: '/view/renderer'

def viewServlet = new FreemarkerServlet(documentRoot, runMode)
servlet(viewUri, viewServlet)

