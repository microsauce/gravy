
@echo off
setLocal EnableDelayedExpansion
set cp="
for /R %GRAVY_HOME%/lib/groovy %%a in (*.jar) do (
	set cp=!cp!;%%a
)
set cp=!cp!"

%JAVA_HOME%\bin\java -cp %cp% org.codehaus.groovy.tools.GroovyStarter --classpath %cp% --main groovy.ui.GroovyMain %GRAVY_HOME%\bin\scripts\bootstrap.groovy "$@"