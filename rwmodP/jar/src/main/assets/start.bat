@echo off
setlocal
set JAVA_OPTS=-Dfile.encoding=utf-8 -Djava.awt.headless=false -Djava.library.path=%~dp0
java %JAVA_OPTS% -jar "%~dp0*.jar" awt.Main main
endlocal