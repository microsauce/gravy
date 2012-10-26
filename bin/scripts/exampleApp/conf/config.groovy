/*
	This is a general purpose configuration file for your gravy app.  

	This configuration is bound to all scripts in your application 
	as 'config'
*/

import org.apache.log4j.*

meta {
	version='1.0'
	author=''
}

dependencies {
	// maven (m2) repositories
	repositories=[]
	
	// maven coordinates 'group:artifactId:version'
	lib=[]
}

gravy {
	refresh = true
	env=System.getProperty('gravy.env')
	errorPage='/error' 
	viewUri='/view/renderer'
	modules=['gstring']
	//modules=['scalate']
}

//
// module configuration 
//
gstring {
	documentRoot='/view'
	uri=gravy.viewUri
}
/*
freemarker {
	documentRoot='/view'
	uri=gravy.viewUri
}
scalate {
	documentRoot='/view'
	uri=gravy.viewUri
}
*/

//
// embedded jetty configuration
//

jetty {
	webroot='/webroot'
	host='localhost'
	port=8080
	contextPath='/'
}

log4j {
	appender.appLog='org.apache.log4j.RollingFileAppender'
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

//
// envinroment specific tweeks
//

environments {
	dev {

		gravy {
			refresh=true
		}
		gstring {
			mode='dev'
		}
		freemarker {
			mode='dev'
		}
		log4j {
			appender.'appLog.File'="${System.getProperty('user.dir')}/log/application.log"
			rootCategory='DEBUG, appLog, console'
		}
	}

	prod {
		log4j {
			appender.'appLog.File'="${System.getProperty('java.io.tmpdir')}/log/application.log"
			rootCategory='ERROR, appLog'
		}
	}
}
