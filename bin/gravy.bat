
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
set cp=
for /R %GRAVY_HOME%/lib/groovy %%a in (*.jar) do (
	set cp=!cp!;%%a
)

%JAVA_HOME%\bin\java -cp %cp%;%GRAVY_HOME%\lib\gravy.jar org.codehaus.groovy.tools.GroovyStarter --classpath %cp%;%GRAVY_HOME%\lib\gravy.jar --main groovy.ui.GroovyMain %GRAVY_HOME%\bin\scripts\bootstrap.groovy %*
