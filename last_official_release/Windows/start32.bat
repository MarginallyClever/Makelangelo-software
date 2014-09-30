:: special thanks to http://rgagnon.com/javadetails/java-0642.html
:: http://stackoverflow.com/questions/638301/discover-from-a-batch-file-where-is-java-installed
@echo off
cls
setlocal ENABLEEXTENSIONS
::
:: get the current java version
::

:CheckOS

IF EXIST "%PROGRAMFILES(X86)%" (GOTO 64bit) ELSE (GOTO 32bit)

:64bit

FOR /F "skip=2 tokens=2*" %%A IN ('REG QUERY "HKLM\Software\Wow6432Node\JavaSoft\Java Runtime Environment" /v CurrentVersion') DO set CurVer=%%B

FOR /F "skip=2 tokens=2*" %%A IN ('REG QUERY "HKLM\Software\Wow6432Node\JavaSoft\Java Runtime Environment\%CurVer%" /v JavaHome') DO set JAVA_HOME=%%B

GOTO endos

:32bit

FOR /F "skip=2 tokens=2*" %%A IN ('REG QUERY "HKLM\Software\JavaSoft\Java Runtime Environment" /v CurrentVersion') DO set CurVer=%%B

FOR /F "skip=2 tokens=2*" %%A IN ('REG QUERY "HKLM\Software\JavaSoft\Java Runtime Environment\%CurVer%" /v JavaHome') DO set JAVA_HOME=%%B

:endos

if defined JAVA_HOME (
  @echo The current Java runtime is  %CurVer%
) else (
  @echo Java not found.
  @pause
  goto end
)

"%JAVA_HOME%\bin\java.exe" -jar Makelangelo.jar

:end