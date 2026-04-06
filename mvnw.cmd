@ECHO OFF
SETLOCAL
set DIR=%~dp0
if "%DIR%"=="" set DIR=.
if "%DIR:~-1%"=="\" set DIR=%DIR:~0,-1%
set MAVEN_PROJECTBASEDIR=%DIR%
java -Dmaven.multiModuleProjectDirectory="%MAVEN_PROJECTBASEDIR%" -classpath "%DIR%\.mvn\wrapper\maven-wrapper.jar" org.apache.maven.wrapper.MavenWrapperMain %*
ENDLOCAL
