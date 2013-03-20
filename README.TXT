SWEET HOME 3D v 4.0
-------------------

This archive contains source code of Sweet Home 3D, an interior design Java application.
Please read http://www.sweethome3d.com/history.html for more information
about new features available in this version.

You may also download this source code with the following CVS command:

cvs -z3 -d:pserver:anonymous@sweethome3d.cvs.sourceforge.net:/cvsroot/sweethome3d co -P -r V_4_0 SweetHome3D


HOW TO USE THIS SOURCE CODE
---------------------------

Source code classes are compatible with Java SE 5, and the directory where
you find this file contains all the information required to compile source code
as an Eclipse project or with Ant.

If you want to compile and test the Java files of this release 
with Eclipse, please follow the next steps:
1. install last JDK 5 or 6 available at http://java.sun.com
2. install Eclipse available at http://www.eclipse.org
3. in Eclipse, select File > Import... menu
4. In "Select an import source" list of Import dialog box, 
   choose "Existing project into Workspace" option
5. click on Next button
6. in "Select root directory" text field, 
   type or select the path to the root of this archive
7. click on Finish 

You may execute applications and tests of this archive, with the Eclipse Run menu.
To execute applications and tests that require Java 3D, open Run Dialog in Eclipse, 
select the Arguments tab of the target application, and add to the VM arguments 
text field the option: 
-Djava.ext.dirs=lib/windows/i386;lib, 
-Djava.ext.dirs=lib/windows/x64;lib,
-Djava.ext.dirs=lib/macosx:lib, 
-Djava.ext.dirs=lib/linux/i386:lib 
or -Djava.ext.dirs=lib/linux/x64:lib
according to your system and its architecture, except under Java 7 and Mac OS X, 
where VM arguments should be:
-Djava.ext.dirs=lib/macosx/java3d-1.6:lib -Djava.library.path=lib/macosx/java3d-1.6
-Djogamp.gluegen.UseTempJarCache=false -Dcom.eteks.sweethome3d.j3d.useOffScreen3DView=true

The last release of Sweet Home 3D can be tested directly as a Java Web Start 
application at http://www.sweethome3d.com/ or as an applet at
http://www.sweethome3d.com/SweetHome3DOnLine.html

If you want to build Sweet Home 3D with Ant, please install last JDK 5 or 6 and 
Ant available at http://ant.apache.org/, and run ant command in the directory where
you uncompressed this archive.
The default target provided by build.xml Ant file builds the Jar executable version
in the install subdirectory. Read build.xml Ant file for more information about
other available targets. 
Note that the last part of the Ant targets javaWebStart, applet and viewer 
requires a passphrase for keystore to sign the deployed jars. For security reasons, 
the required file keys.keytool isn't provided with this source code. You'll have 
first to generate this file and choose a passphrase, with the following command run 
in the directory where you uncompressed this archive:
  keytool -genkey -keystore keys.keytool -alias SweetHome3D 



Read LICENSE.TXT file for more details about licenses applicable to this software
and included materials developed by third parties.
    
    
Sweet Home 3D, Copyright (c) 2005-2013 Emmanuel PUYBARET / eTeks. 
Distributed under GNU General Public License    