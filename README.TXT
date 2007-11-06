SWEET HOME 3D v 1.2
---------------------

This archive contains source code of Sweet Home 3D, an interior design Java application.
Please read http://sweethome3d.sourceforge.net/history.html for more information
about new features available in this version.

You may also download this source code with the following CVS command :

cvs -z3 -d:pserver:anonymous@sweethome3d.cvs.sourceforge.net:/cvsroot/sweethome3d co -P -r V_1_2 SweetHome3D


HOW TO USE THIS SOURCE CODE
---------------------------

Source code classes are compatible with Java SE 5, and this directory
contains all the information required to compile source code
as an Eclipse project or with Ant.

If you want to compile ant test the Java files of this release 
with Eclipse, please follow the next steps :
1. install last JDK 5.0 available at http://java.sun.com
2. install Eclipse available at http://www.eclipse.org
3. in Eclipse, select File > Import... menu
4. In "Select an import source" list of Import dialog box, 
   choose "Existing project into Workspace" option
5. click on Next buton
6. in "Select root directory" text field, 
   type or select the path to the root of this archive
7. click on Finish 

You may execute applications and tests of this archive, with the Eclipse Run menu.
To execute applications and tests that require Java 3D, open Run Dialog in Eclipse, 
select the Arguments tab of the target application, and add to the VM arguments 
text field the option -Djava.ext.dirs=lib;lib/windows, -Djava.ext.dirs=lib:lib/macosx 
or -Djava.ext.dirs=lib:lib/linux/i386 depending on your system.

The last release of Sweet Home 3D can be tested directly as a Java Web Start 
application at http://sweethome3d.sourceforge.net/


If you want to build the Java Web Start application of this release with Ant,
please install last JDK 5.0 and Ant, and run ant command at archive root.
Note that the last part of the deploy Ant task requires a passphrase for keystore 
to sign the deployed jars. For security reasons, the required file keys.keytool
isn't provided with this source code. You'll have first to generate this file 
and choose a passphrase, with the following command run at archive root :
  keytool -genkey -keystore keys.keytool -alias SweetHome3D 


Read LICENSE.TXT file for more details about licenses applicable to this software
and included materials developed by third parties.
    
    
(C) 2006-2007 Emmanuel Puybaret / eTeks. Distributed under GNU General Public License    