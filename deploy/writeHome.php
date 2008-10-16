<?php 
  /*
   * writeHome.php 13 Oct 2008
   *
   * Copyright (c) 2008 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
   *
   * This program is free software; you can redistribute it and/or modify
   * it under the terms of the GNU General Public License as published by
   * the Free Software Foundation; either version 2 of the License, or
   * (at your option) any later version.
   *
   * This program is distributed in the hope that it will be useful,
   * but WITHOUT ANY WARRANTY; without even the implied warranty of
   * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   * GNU General Public License for more details.
   *
   * You should have received a copy of the GNU General Public License
   * along with this program; if not, write to the Free Software
   * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
   */
   
  // Updloads the file available in multipart file "home", saves it
  // in homes directory and returns "1" if save was successful
  $homesDir = "../persistent/homes";
  $homeFile = $homesDir."/".$_FILES['home']['name'].".sh3d"; 
  
  // Security for Sweet Home 3D hosted on sourceforge.net
  if ($_FILES['home']['size'] > 200000) {
    // Refuse files bigger than 200000 Bytes
    echo "0";
    return;
  }
  
  if (!file_exists($homeFile)) {
    $homes = array();
    $handler = opendir($homesDir);
    while ($file = readdir($handler)) {
      if (!is_dir($file) && eregi('.sh3d', $file)) {
        $homes[] = $homesDir."/".$file;
      }  
    }
    closedir($handler);

    // If there are 5 recorded homes 
    if (sizeof($homes) >= 5) {
      // Remove the oldest file
      function compareFileDate($file1, $file2) {
        return filemtime($file1) - filemtime($file2);
      }
      usort($homes, "compareFileDate");
      unlink($homes [0]);
    }
  }
  // End of security
  
  if (move_uploaded_file($_FILES['home']['tmp_name'], $homeFile)) {
    echo "1";
  } else {
    echo "0";
  }
?>