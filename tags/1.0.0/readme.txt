Window/Preferences/Java/"Build Path"/"Classpath Variables"
Add PND_LIB variable pointing to the directory containing the required jars.

Window/Preferences/"Run/Debug"/"String Substitution"
Add pnd_nat variable pointing to the directory containing the native libraries.

Add these VM arguments when running games:
"-Djava.library.path=${pnd_nat}\[os]"
-Dorg.lwjgl.opengl.Display.allowSoftwareOpenGL=true