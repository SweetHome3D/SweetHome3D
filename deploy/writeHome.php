<?php 
  /*
   * writeHome.php 13 Oct 2008
   *
   * Sweet Home 3D, Copyright (c) 2008 Emmanuel PUYBARET / eTeks <info@eteks.com>
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
  $homesDir = ".";
  $homeFile = $homesDir."/".$_FILES['home']['name'].".sh3d"; 
  
  if (move_uploaded_file($_FILES['home']['tmp_name'], $homeFile)) {
    echo "1";
  } else {
    echo "0";
  }
?>