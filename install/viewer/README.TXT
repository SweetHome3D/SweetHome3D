SWEET HOME 3D VIEWER v 4.1
--------------------------

This archive contains files required to install Sweet Home 3D Viewer, an 
applet able to display the 3D view of a Sweet Home 3D file from its URL.
This applet can run under Windows, Mac OS X 10.4 to 10.8 and Linux.
The size of the library files downloaded by this applet varies from 2.2 MB 
to 4 MB depending on the system and the Java version of the user.

Applet users may press the space bar to switch between aerial view 
and virtual visitor view, once they clicked in the applet.


HOW TO INSTALL SWEET HOME 3D VIEWER
-----------------------------------

1. If you want to use Sweet Home 3D Viewer on a web server, save and compress 
   the SH3D file you want to display using the "File > Save and compress" 
   menu item to limit its size
2. Copy or send by FTP the SH3D file to its final directory 
3. Copy the URL of your SH3D file to clipboard, ensuring it doesn't contain 
   spaces (or replace them by "%20" symbols)
4. Edit the "SweetHome3DViewer.html" file and paste it as the value of the 
   parameter named "homeURL" instead of "default.sh3d" default file :
     var parameters = {java_arguments:"-Xmx512m",
                       navigationPanel:"true",
                       homeURL:"default.sh3d"};
   for example, if the URL of your SH3D file is:
     http://www.mydomain.com/myhome.sh3d
   you should obtain the following line:
     var parameters = {java_arguments:"-Xmx512m", 
                       navigationPanel:"true",
                       homeURL:"http://www.mydomain.com/myhome.sh3d"};
5. Open the file "SweetHome3DViewer.html" in your web browser to check if
   it works correctly
6. Copy the content of the applet scripts of "SweetHome3DViewer.html" in
   the web page of your choice ; you can change the width and the height of
   the applet by modifying the values of its "width" and "height" attributes
7. Install this web page at the location of your choice, copying it with 
   the files in the directory named "lib" 

Note that by default, "homeURL" value is equal to "default.sh3d" and a non 
absolute URL will be relative to the web page containing the applet scripts.

If the cache of the SH3D file returned by the "homeURL" value should never 
be used, add the parameter "ignoreCache" with a value equal to "true".
For example, if your URL is the PHP request "http://www.mydomain.com/userHome.php"
the parameters list should become:
  var parameters = {java_arguments:"-Xmx512m", 
                    navigationPanel:"true",
                    homeURL:"http://www.mydomain.com/userHome.php",
                    ignoreCache:"true"};

If you don't want to display navigation arrows, simply remove the line
  navigationPanel:"true",
from parameters list.
                     
                     
Mentioning the following copyrights on the web page where you use 
Sweet Home 3D Viewer will be greatly appreciated:

Sweet Home 3D Viewer, Copyright (c) 2005-2013 Emmanuel PUYBARET / eTeks. 
Distributed under GNU General Public License    
