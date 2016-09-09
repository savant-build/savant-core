@ECHO OFF

IF "%OS%"=="Windows_NT" @SETLOCAL
IF "%OS%"=="WINNT" @SETLOCAL
IF "%JAVA_HOME%"=="" GOTO javaHomeMissing

SETLOCAL ENABLEDELAYEDEXPANSION

SET SAVANT_HOME=%~dp0..
dir %SAVANT_HOME%\lib > NUL
IF NOT ERRORLEVEL 1 GOTO savantHomeSet
ECHO Unable to locate SAVANT_HOME. Please set the environmental variable.
EXIT /B 1

:javaHomeMissing
ECHO The JAVA_HOME environment variable is not set. Please set this to point to a JDK 8 installation of Java such that $JAVA_HOME/bin/java points to the Java executable
EXIT /B 1

:savantHomeSet
SET CLASSPATH=
FOR %%f IN ("%SAVANT_HOME%\lib\*.jar") DO SET CLASSPATH=%%f;!CLASSPATH!
%JAVA_HOME%\bin\java %SAVANT_OPTS% -cp "%CLASSPATH%" org.savantbuild.run.Main %*

ENDLOCAL
