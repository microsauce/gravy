
import org.microsauce.gravy.server.runtime.GViewServlet

def runMode = config.gstring.mode ?: 'prod'
println "config: $config"
println "gstring mode: $runMode"
def appRoot = config.appRoot
def documentRoot =  appRoot+(config.gstring.documentRoot ?: '/view')
def viewUri = config.gravy.viewUri ?: '/view/renderer'

def viewServlet = new GViewServlet(documentRoot, runMode)
servlet(viewUri, viewServlet)

