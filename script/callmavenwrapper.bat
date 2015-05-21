@REM author: Peter Carlo Colapietro
@REM #see: http://wynnnetherland.com/linked/2013012801/bootstrapping-consistency/
@REM #see: https://github.com/bdemers/maven-wrapper
@echo off
pushd ..\java
call mvnw.bat %1
popd
