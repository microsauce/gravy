
import org.microsauce.gravy.runtime.gstring.GStringTemplateServlet

def runMode = config.gstring.mode ?: 'prod'
def appRoot = config.appRoot
def documentRoot =  appRoot+'/WEB-INF/view'
def viewUri = config.gravy.viewUri ?: '/view/gstring'

def viewServlet = new GStringTemplateServlet(documentRoot, runMode)
servlet(viewUri, viewServlet)

