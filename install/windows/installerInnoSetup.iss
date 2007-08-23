; installerInnoSetup.iss
;
; Copyright (c) 2007 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
;
; SweetHome3DSetup.exe creator
; This script requires Inno setup available at http://www.jrsoftware.org/isinfo.php
; and a tmp directory stored in current directory containing :
;   a SweetHome3D.exe file built with launch4j
; + a jre... subdirectory containing a dump of Windows JRE without the files mentioned 
;   in the JRE README.TXT file (JRE bin/javaw.exe command excepted)     
; + a lib subdirectory containing SweetHome3D.jar and Windows Java 3D DLLs and JARs for Java 3D
; + file COPYING.TXT

[Setup]
AppName=Sweet Home 3D
AppVerName=Sweet Home 3D version 1.0
AppPublisher=eTeks
AppPublisherURL=http://www.eteks.com
AppSupportURL=http://sweethome3d.sourceforge.net
AppUpdatesURL=http://sweethome3d.sourceforge.net
DefaultDirName={pf}\Sweet Home 3D
DefaultGroupName=eTeks Sweet Home 3D
LicenseFile=COPYING.TXT
OutputDir=.
OutputBaseFilename=SweetHome3DSetup
Compression=lzma
SolidCompression=yes
ChangesAssociations=yes
VersionInfoVersion=1.0.0.0
VersionInfoTextVersion=1.0
VersionInfoDescription=Sweet Home 3D Setup
VersionInfoCopyright=Copyright (c) 2007 eTeks
VersionInfoCompany=eTeks

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"
Name: "french"; MessagesFile: "compiler:Languages\French.isl"

[Tasks]
Name: desktopicon; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"
;Name: associate; Description: "{cm:AssocFileExtension,Sweet Home 3D,sh3d}"; GroupDescription: "{cm:OtherTasks}"


[Files]
Source: "tmp\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs
; NOTE: Don't use "Flags: ignoreversion" on any shared system files

[Icons]
Name: "{group}\Sweet Home 3D"; Filename: "{app}\SweetHome3D.exe"; Comment: "{cm:SweetHome3DComment}"
Name: "{group}\{cm:UninstallProgram,Sweet Home 3D}"; Filename: "{uninstallexe}"
Name: "{userdesktop}\Sweet Home 3D"; Filename: "{app}\SweetHome3D.exe"; Tasks: desktopicon; Comment: "{cm:SweetHome3DComment}"

[Run]
Filename: "{app}\SweetHome3D.exe"; Description: "{cm:LaunchProgram,Sweet Home 3D}"; Flags: nowait postinstall skipifsilent

[CustomMessages]
SweetHome3DComment=Arrange the furniture of your house
french.SweetHome3DComment=Aménagez les meubles de votre logement
OtherTasks=Other tasks:
french.OtherTasks=Autres tâches :

[Registry]
Root: HKCR; Subkey: ".sh3d"; ValueType: string; ValueName: ""; ValueData: "eTeks Sweet Home 3D"; Flags: uninsdeletevalue
Root: HKCR; Subkey: "eTeks Sweet Home 3D"; ValueType: string; ValueName: ""; ValueData: "Sweet Home 3D"; Flags: uninsdeletekey
Root: HKCR; Subkey: "eTeks Sweet Home 3D\DefaultIcon"; ValueType: string; ValueName: ""; ValueData: "{app}\SweetHome3D.exe,0"
Root: HKCR; Subkey: "eTeks Sweet Home 3D\shell\open\command"; ValueType: string; ValueName: ""; ValueData: """{app}\SweetHome3D.exe"" -open ""%1"""

