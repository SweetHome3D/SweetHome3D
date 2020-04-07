SWEET HOME 3D VIEWER v 6.3
--------------------------

This archive contains files required to install Sweet Home 3D Viewer, an 
applet able to display the 3D view of a Sweet Home 3D file from its URL.
This applet can run under Windows, Mac OS X and Linux.
The size of the library files downloaded by this applet varies from 2.2 MB 
to 4 MB depending on the system and the Java version of the user.

Applet users may press the space bar to switch between aerial view 
and virtual visitor view, once they clicked in the applet, unless the
the value of the parameter "activateCameraSwitchKey" isn't equal to "true".


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
     var parameters = {java_arguments:"-Xmx640m",
                       navigationPanel:"true",
                       homeURL:"default.sh3d",
                       jnlp_embedded:"PD94bWwgdm..."};
   for example, if the URL of your SH3D file is:
     http://www.mydomain.com/myhome.sh3d
   you should obtain the following line:
     var parameters = {java_arguments:"-Xmx512m", 
                       navigationPanel:"true",
                       homeURL:"http://www.mydomain.com/myhome.sh3d",
                       jnlp_embedded:"PD94bWwgdm..."};
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
  var parameters = {java_arguments:"-Xmx640m", 
                    navigationPanel:"true",
                    homeURL:"http://www.mydomain.com/userHome.php",
                    jnlp_embedded:"PD94bWwgdm...",
                    ignoreCache:"true"};

If you don't want to display navigation arrows, simply remove the following line
from parameters list:
  navigationPanel:"true",

If you don't want the user to be able to switch between aerial view and virtual 
visitor view with the space bar, add the following line to parameters list:
  activateCameraSwitchKey:"false",

If you want to select a given point of view stored in the SH3D file, 
add a "camera" parameter with a value equal to the name of that point of view.
You may also propose to the user to select the point of view of his choice 
in the contextual menu of the 3D view, by adding a "selectableCameras" parameter
with a value equal to a comma separated list of viewpoint names, as in the 
following example:
  var parameters = {java_arguments:"-Xmx640m", 
                    navigationPanel:"true",
                    camera:"Kitchen",
                    selectableCameras:"Living room, Kitchen, Bedroom",
                    activateCameraSwitchKey:"false",                    
                    homeURL:"http://www.mydomain.com/userHome.php",
                    jnlp_embedded:"PD94bWwgdm..."};

Similarly, you may select a given level of a multilevel home, with the "level" 
parameter equal to the name of that level.
You may also propose to the user to select the level of his choice in the
contextual menu of the 3D view, by adding a "selectableLevels" parameter with
a value equal to a comma separated list of existing level names, as in the 
following example:
  var parameters = {java_arguments:"-Xmx640m", 
                    navigationPanel:"true",
                    level:"1st floor",
                    selectableLevels:"Basement, 1st floor",                    
                    homeURL:"http://www.mydomain.com/userHome.php",
                    jnlp_embedded:"PD94bWwgdm..."};

"camera", "selectableCameras", "level" and "selectableLevels" parameters are 
optional and can be cited or not. If a requested viewpoint of level name doesn't 
exist, it will be silently ignored. If "selectableCameras" and "selectableLevels" 
are cited a separator will be added in the contextual menu to separate both lists.
Keep in mind that selecting a level may have a different effect according 
to the "Display all levels / Selected level" options in the displayed home 
and whether visitor eyes should be adjusted to the selected level or not. 
Using the default settings ("Display Selected level" and "Adjust eyes 
elevation according to selected level" options selected) is probably the choice 
that will be the easiest to understand for the end user.

                    
Caution: From version 4.2, a new "jnlp_embedded" parameter followed with a very 
long text was added to the parameters list. Don't forget to add it to your 
existing viewer if you want it to be able to run under Java 7u45 and superior.    
               
                     
Mentioning the following copyright on the web page where you use 
Sweet Home 3D Viewer will be greatly appreciated:

Sweet Home 3D Viewer, Copyright (c) 2005-2020 Emmanuel PUYBARET / eTeks. 
Distributed under GNU General Public License    
