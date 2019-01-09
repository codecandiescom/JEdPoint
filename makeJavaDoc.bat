deltree /y Documentation\Developers\JavaDoc
mkdir "Documentation\Developers\JavaDoc"
"c:\Program Files\j2sdk1.4.0\bin\javadoc.exe" -classpath . -d "Documentation\Developers\JavaDoc" -verbose -public d:\Programming\Java\JEdPoint\src\JEdPoint\*.java
