
import org.microsauce.gravy.server.runtime.GViewServlet

def runMode = config.gstring.mode ?: 'prod'
def appRoot = config.appRoot
def documentRoot =  appRoot+'/WEB-INF/view'
def viewUri = config.gravy.viewUri ?: '/view/renderer'

def viewServlet = new GViewServlet(documentRoot, runMode)
servlet(viewUri, viewServlet)

