
@echo off

IF "%GRAVY_HOME%"==""  (
	echo GRAVY_HOME is not defined
	goto:eof
)
IF "%JAVA_HOME%"==""  (
	echo JAVA_HOME is not defined
	goto:eof
)

setLocal EnableDelayedExpansion
set cp=%GRAVY_HOME%\lib\*;%GRAVY_HOME%\lib\groovy\*;%GRAVY_HOME%\lib\rhino\*;%GRAVY_HOME%\lib\ringojs\*;%GRAVY_HOME%\lib\jruby\*;%JAVA_HOME%\lib\*

%JAVA_HOME%\bin\java -cp %GRAVY_HOME%\lib\*;%GRAVY_HOME%\lib\groovy\*;%GRAVY_HOME%\lib\rhino\*;%GRAVY_HOME%\lib\ringojs\*;%GRAVY_HOME%\lib\jruby\* org.codehaus.groovy.tools.GroovyStarter --classpath %cp%;%GRAVY_HOME%\lib\gravy.jar --main groovy.ui.GroovyMain %GRAVY_HOME%\bin\scripts\commandLine.groovy %*
