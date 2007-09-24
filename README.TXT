SWEET HOME 3D v 1.1
---------------------

This archive contains source code of Sweet Home 3D, an interior design Java application.
Please read http://sweethome3d.sourceforge.net/history.html for more information
about new features available in this version.

You may also download this source code with the following CVS command :

cvs -z3 -d:pserver:anonymous@sweethome3d.cvs.sourceforge.net:/cvsroot/sweethome3d co -P -r V_1_1 SweetHome3D


HOW TO USE THIS SOURCE CODE
---------------------------

Source code classes are compatible with Java SE 5, and this directory
contains all the information required to compile source code
as an Eclipse project or with Ant.

If you want to compile Java files of this release with Eclipse, 
please follow the next steps :
1. install last JDK 5.0 available at http://java.sun.com
2. on systems different from Mac OS X, install Java 3D 1.5.1
   available at http://java3d.dev.java.net/ 
3. install Eclipse available at http://www.eclipse.org
4. in Eclipse, select File > Import... menu
5. In "Select an import source" list of Import dialog box, 
   choose "Existing project into Workspace" option
6. click on Next buton
7. in "Select root directory" text field, 
   type or select the path to the root of this archive
8. click on Finish 

You may execute aplications and tests of this archive, with the Eclipse Run menu.
The last release of Sweet Home 3D can be tested directly as a Java Web Start 
application at http://sweethome3d.sourceforge.net/


If you want to build the Java Web Start application of this release with Ant,
please install last JDK 5.0 and Ant, and run ant command at archive root.
Note that the last part of the deploy Ant task requires a passphrase for keystore 
to sign the deployed jars. For security reasons, the required file keys.keytool
isn't provided with this source code. You'll have first to generate this file 
and choose a passphrase, with the following command run at archive root :
  keytool -genkey -keystore keys.keytool -alias SweetHome3D 


(C) 2006-2007 Emmanuel Puybaret / eTeks. Distributed under GNU General Public License