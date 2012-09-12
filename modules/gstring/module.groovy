
import org.microsauce.gravy.server.runtime.GViewServlet

def runMode = config.gstring.mode ?: 'prod'
def appRoot = config.appRoot
def documentRoot =  appRoot+(config.gstring.documentRoot ?: '/view')
def viewUri = config.gravy.viewUri ?: '/view/renderer'

def viewServlet = new GViewServlet(documentRoot, runMode)
app.servlet(viewUri, viewServlet)

