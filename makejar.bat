@echo off
mkdir classes

deltree /y "classes\jedpoint"

del Jedpoint.jar
deltree /y "jar\jedpoint"

cd src
javac -g:none -O JEdPoint\JEdPointMicrokernel.java -d ..\classes

cd ..

xcopy classes\* Jar\ /s

deltree /y "jar\dependency cache"
deltree /y "jar\package cache"

cd jar
pkzip -ex -r -P JEdPoint *
ren JEdPoint.zip JEdPoint.jar

move JEdPoint.jar ..\

cd..

deltree /y "jar\jedpoint"

:exit

