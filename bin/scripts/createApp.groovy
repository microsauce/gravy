// TODO add web.xml to this script

if (args.length < 1) {
	println 'wrong number of arguments: you must provide project name'
	return
}
def appName = args[0]

def config = '''
import org.apache.log4j.*


gravy {
	errorPage='/error' 
	viewUri='/view/renderer'
	modules=['gstring']
}

//
// module configuration 
//

freemarker {
	documentRoot='/view'
	uri=gravy.viewUri
	runMode='development'
}
scalate {
	documentRoot='/view'
	uri=gravy.viewUri
	runMode='development'
}

gstring {
	documentRoot='/view'
	uri=gravy.viewUri
}

jetty {
	webroot='/webroot'
	host='localhost'
	port=8080
	contextPath='/foo'

	ssl {
		port=8443
	}
}

log4j {
	appender.appLog='org.apache.log4j.RollingFileAppender'
	appender.'appLog.File'="${System.getProperty('user.dir')}/log/application.log"
	appender.'appLog.MaxFileSize'='10MB'
	appender.'appLog.MaxBackupIndex'='5'
	appender.'appLog.append'='true'
	appender.'appLog.layout'='org.apache.log4j.PatternLayout'
	appender.'appLog.layout.ConversionPattern'='%d{ABSOLUTE} %5p %c{1}:%L - %m%n'

	appender.console='org.apache.log4j.ConsoleAppender'
	appender.'console.Target'='System.out'
	appender.'console.layout'='org.apache.log4j.PatternLayout'
	appender.'console.layout.ConversionPattern'='%d{ABSOLUTE} %5p %c{1}:%L - %m%n'
}

environments {
	dev {
		jetty {
			ssl {
				keyStorePassword='password'
				keyManagerPassword='Curly4pres'
			}
		}

		gstring {
			runMode='dev'
		}
		
		log4j {
			rootCategory='DEBUG, appLog, stdout'
		}
	}

	prod {
		log4j {
			rootCategory='ERROR, appLog, stdout'
		}
	}
}
'''

File.metaClass.mkParentDirs = { 
	def parentFolder = delegate.parent ? new File(delegate.parent) : null
	if ( !parentFolder ) return null
	else if ( parentFolder.exists() ) return delegate

	parentFolder.mkParentDirs()
	parentFolder.mkdir()
	
	delegate
}

def workingFolder = System.getProperty('user.dir')
def appRoot = workingFolder+'/'+appName

def appFolders = [		
	// appRoot
	appRoot+'/scripts', 
	appRoot+'/modules',  // tie into CP
	appRoot+'/conf',
	appRoot+'/view',
	appRoot+'/webroot/img',
	appRoot+'/webroot/css',
	appRoot+'/webroot/js',
	appRoot+'/lib',
	appRoot+'/log',
	appRoot+'/src/main/java',
	appRoot+'/src/main/groovy',
	appRoot+'/src/main/resources',
	appRoot+'/src/test/java',
	appRoot+'/src/test/groovy',
	appRoot+'/src/test/resources'
]

def appFiles = [
	(appRoot+'/application.goovy'):helloWorld,
	(appRoot+'/conf/config.groovy'):config
]

appFolders.each { thisFolder ->
	println "making folder ${thisFolder}"
	new File(thisFolder).mkParentDirs().mkdir()
}

appFiles.keySet().each { thisFile ->
	println "creating file ${thisFile}"
	def file = new File(thisFile)
	file.createNewFile()
	file << appFiles[thisFile]
}
