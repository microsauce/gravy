
@echo off

IF "%GRAVY_HOME%"==""  (
	echo Start up failed: GRAVY_HOME is not defined
	goto:eof
)
IF "%JAVA_HOME%"==""  (
	echo Startup failed: JAVA_HOME is not defined
	goto:eof
)

setLocal EnableDelayedExpansion
set cp=%GRAVY_HOME%\lib\*;%GRAVY_HOME%\lib\groovy\*;%GRAVY_HOME%\lib\rhino\*;%GRAVY_HOME%\lib\ringojs\*;%GRAVY_HOME%\lib\jruby\*;%GRAVY_HOME%\lib\jetty8\*;%GRAVY_HOME%\lib\jnotify\*;%JAVA_HOME%\lib\*

%JAVA_HOME%\bin\java -cp %cp% org.codehaus.groovy.tools.GroovyStarter --classpath %cp% --main groovy.ui.GroovyMain %GRAVY_HOME%\bin\scripts\commandLine.groovy %*
