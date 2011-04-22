SWEET HOME 3D v 3.2
-------------------

This archive contains source code of Sweet Home 3D, an interior design Java application.
Please read http://www.sweethome3d.com/history.html for more information
about new features available in this version.

You may also download this source code with the following CVS command:

cvs -z3 -d:pserver:anonymous@sweethome3d.cvs.sourceforge.net:/cvsroot/sweethome3d co -P -r V_3_2 SweetHome3D


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
text field the option 
-Djava.ext.dirs=lib;lib/windows/i386, 
-Djava.ext.dirs=lib;lib/windows/x64,
-Djava.ext.dirs=lib:lib/macosx, 
-Djava.ext.dirs=lib:lib/linux/i386 
or -Djava.ext.dirs=lib:lib/linux/x64 
according to your system and its architecture.

The last release of Sweet Home 3D can be tested directly as a Java Web Start 
application at http://www.sweethome3d.com/ or as an applet at
http://www.sweethome3d.com/SweetHome3DOnLine.html

If you want to build Sweet Home 3D with Ant, please install last JDK 5 or 6 and 
Ant available at http://ant.apache.org/, and run ant command in the directory where
you uncompressed this archive.
The default target provided by build.xml Ant file builds the Java Web Start version.
Read build.xml Ant file for more information about other available targets. 
Note that the last part of the Ant targets javaWebStart, applet and viewer 
requires a passphrase for keystore to sign the deployed jars. For security reasons, 
the required file keys.keytool isn't provided with this source code. You'll have 
first to generate this file and choose a passphrase, with the following command run 
in the directory where you uncompressed this archive:
  keytool -genkey -keystore keys.keytool -alias SweetHome3D 



Read LICENSE.TXT file for more details about licenses applicable to this software
and included materials developed by third parties.
    
    
Copyright (c) 2005-2011 Emmanuel PUYBARET / eTeks. Distributed under GNU General Public License    