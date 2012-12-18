/*
 * Launcher.c  17 dec 2012
 *
 * Sweet Home 3D, Copyright (c) 2012 Emmanuel PUYBARET / eTeks <info@eteks.com>
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
 
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <mach-o/dyld.h>
#include <CoreServices/CoreServices.h>

#define ORACLE_JAVA_LAUNCHER  "AppBundler"
#define APPLE_JAVA_LAUNCHER   "JavaApplicationStub"

/**
 * Returns the path of the given executable found in the same directory as the executable that launched this program.
 */ 
char * getJavaLauncherExecutablePath(char *javaLauncherExecutable) {
  // Retrieve executable path
  uint32_t size = sizeof(char) * 1024;  
  char *executablePath = malloc(size);
  if (_NSGetExecutablePath(executablePath, &size) != 0) {
    free(executablePath);
    executablePath = malloc(size);
    _NSGetExecutablePath(executablePath, &size);
  } 
  
  // Replace last path element by javaExecutable
  char *javaLauncherExecutablePath = malloc(sizeof(char) * (strlen(executablePath) + strlen(javaLauncherExecutable)));
  strcpy(javaLauncherExecutablePath, executablePath);
  strcpy(strrchr(javaLauncherExecutablePath, '/') + 1, javaLauncherExecutable);
  
  free(executablePath);
  return javaLauncherExecutablePath;
}

/**
 * Launches AppBundler under Mac OS X 10.7 and more recent versions, or JavaApplicationStub for older versions.
 * Compile with:
 * gcc -o "Sweet Home 3D/Contents/MacOS/SweetHome3D" -framework CoreServices -arch i386 -arch x86_64 -arch ppc -mmacosx-version-min=10.4 Launcher.c 
 */
int main(int argc, char *argv[])
{
  // Retrieve Mac OS X version
  SInt32 majorVersion,minorVersion;
  Gestalt(gestaltSystemVersionMajor, &majorVersion);
  Gestalt(gestaltSystemVersionMinor, &minorVersion);

  char *javaLauncherExecutablePath;
  if (majorVersion >= 10 && minorVersion >= 7) {
    javaLauncherExecutablePath = getJavaLauncherExecutablePath(ORACLE_JAVA_LAUNCHER);
  } else {
    javaLauncherExecutablePath = getJavaLauncherExecutablePath(APPLE_JAVA_LAUNCHER);
  } 
  int returnedValue = execv(javaLauncherExecutablePath, argv);
  free(javaLauncherExecutablePath);
  
  return returnedValue;
}

