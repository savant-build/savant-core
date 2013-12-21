@ECHO OFF

IF "%OS%"=="Windows_NT" @SETLOCAL
IF "%OS%"=="WINNT" @SETLOCAL

SETLOCAL ENABLEDELAYEDEXPANSION

SET SAVANT_HOME=%~dp0..
dir %SAVANT_HOME%\lib > NUL
IF NOT ERRORLEVEL 1 GOTO savantHomeSet
ECHO Unable to locate SAVANT_HOME. Please set the environmental variable.
EXIT /B 1

:savantHomeSet
SET CLASSPATH=
FOR %%f IN ("%SAVANT_HOME%\lib\*.jar") DO SET CLASSPATH=%%f;!CLASSPATH!
java "SAVANT_OPTS" -cp "%CLASSPATH%" org.savantbuild.run.Main %*

ENDLOCAL
