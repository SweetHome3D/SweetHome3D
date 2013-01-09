; installerInnoSetup.iss
;
; Sweet Home 3D, Copyright (c) 2007-2013 Emmanuel PUYBARET / eTeks <info@eteks.com>
;
; SweetHome3D-4.0-windows.exe setup program creator
; This script requires Inno setup available at http://www.jrsoftware.org/isinfo.php
; and a build directory stored in current directory containing :
;   a SweetHome3D.exe file built with launch4j
; + a jre... subdirectory containing a dump of Windows JRE without the files mentioned 
;   in the JRE README.TXT file (JRE bin/javaw.exe command excepted)     
; + a lib subdirectory containing SweetHome3D.jar and Windows Java 3D DLLs and JARs for Java 3D
; + file COPYING.TXT

[Setup]
AppName=Sweet Home 3D
AppVerName=Sweet Home 3D version 4.0
AppPublisher=eTeks
AppPublisherURL=http://www.eteks.com
AppSupportURL=http://sweethome3d.sourceforge.net
AppUpdatesURL=http://sweethome3d.sourceforge.net
DefaultDirName={pf}\Sweet Home 3D
DefaultGroupName=eTeks Sweet Home 3D
LicenseFile=..\..\COPYING.TXT
OutputDir=.
OutputBaseFilename=..\SweetHome3D-4.0-windows
Compression=lzma2/ultra64
SolidCompression=yes
ChangesAssociations=yes
VersionInfoVersion=4.0.0.0
VersionInfoTextVersion=4.0
VersionInfoDescription=Sweet Home 3D Setup
VersionInfoCopyright=Copyright (c) 2007-2013 eTeks
VersionInfoCompany=eTeks
; Signing
;
; Requires keys.pfx in install\windows subdirectory
; Enter password and define SignToolPgm in Ant with following tasks:
; <input message="Enter signature password:" 
;        addproperty="password"/> 
; <exec executable="C:\Program Files\Inno Setup 5\ISCC.exe">
;    <arg value="/sSignToolPgm=$$qC:\Program Files\Microsoft SDKs\Windows\v7.1\Bin\signtool.exe$$q sign /f $$q${basedir}\install\windows\keys.pfx$$q /p ${password} $p"/>
;    <arg value="${basedir}\install\windows\signedInstallerInnoSetup.iss"/>
; </exec>  
SignTool=SignToolPgm /d $qSweet Home 3D Installer$q /du $qhttp://www.sweethome3d.com/$q $f
SignedUninstaller=yes

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"
Name: "french"; MessagesFile: "compiler:Languages\French.isl"
Name: "portuguese"; MessagesFile: "compiler:Languages\Portuguese.isl"
Name: "brazilianportuguese"; MessagesFile: "compiler:Languages\BrazilianPortuguese.isl"
Name: "italian"; MessagesFile: "compiler:Languages\Italian.isl"
Name: "german"; MessagesFile: "compiler:Languages\German.isl"
Name: "czech"; MessagesFile: "compiler:Languages\Czech.isl"
Name: "polish"; MessagesFile: "compiler:Languages\Polish.isl"
Name: "spanish"; MessagesFile: "compiler:Languages\Spanish.isl"
Name: "hungarian"; MessagesFile: "compiler:Languages\Hungarian.isl"
Name: "russian"; MessagesFile: "compiler:Languages\Russian.isl"
Name: "swedish"; MessagesFile: "Swedish.isl"
Name: "greek"; MessagesFile: "Greek.isl"
Name: "chinesesimp"; Messagesfile: "ChineseSimp.isl"
Name: "japanese"; Messagesfile: "Japanese.isl"
Name: "bulgarian"; Messagesfile: "Bulgarian.isl"

[Tasks]
Name: desktopicon; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"
;Name: associate; Description: "{cm:AssocFileExtension,Sweet Home 3D,sh3d}"; GroupDescription: "{cm:OtherTasks}"

[Files]
Source: "build\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{group}\Sweet Home 3D"; Filename: "{app}\SweetHome3D.exe"; Comment: "{cm:SweetHome3DComment}"
Name: "{group}\{cm:UninstallProgram,Sweet Home 3D}"; Filename: "{uninstallexe}"
Name: "{userdesktop}\Sweet Home 3D"; Filename: "{app}\SweetHome3D.exe"; Tasks: desktopicon; Comment: "{cm:SweetHome3DComment}"

[Run]
Filename: "{app}\SweetHome3D.exe"; Description: "{cm:LaunchProgram,Sweet Home 3D}"; Flags: nowait postinstall skipifsilent

[UninstallDelete]
Type: filesandordirs; Name: "{app}\jre6\launch4j-tmp"

[CustomMessages]
SweetHome3DComment=Arrange the furniture of your house
french.SweetHome3DComment=Am閚agez les meubles de votre logement
portuguese.SweetHome3DComment=Organiza as mobilias da sua casa
brazilianportuguese.SweetHome3DComment=Organiza as mobilias da sua casa
czech.SweetHome3DComment=Sestavte si design interieru vaseho domu
polish.SweetHome3DComment=Zaprojektuj wnetrze swojego domu
hungarian.SweetHome3DComment=Keszitse el lakasanak belso kialakitasat!
chinesesimp.SweetHome3DComment=布置您的温馨小家
OtherTasks=Other tasks:
french.OtherTasks=Autres t鈉hes :
chinesesimp.OtherTasks=其他任务:

[Registry]
Root: HKCR; Subkey: ".sh3d"; ValueType: string; ValueName: ""; ValueData: "eTeks Sweet Home 3D"; Flags: uninsdeletevalue
Root: HKCR; Subkey: "eTeks Sweet Home 3D"; ValueType: string; ValueName: ""; ValueData: "Sweet Home 3D"; Flags: uninsdeletekey
Root: HKCR; Subkey: "eTeks Sweet Home 3D\DefaultIcon"; ValueType: string; ValueName: ""; ValueData: "{app}\SweetHome3D.exe,0"
Root: HKCR; Subkey: "eTeks Sweet Home 3D\shell\open\command"; ValueType: string; ValueName: ""; ValueData: """{app}\SweetHome3D.exe"" -open ""%1"""

Root: HKCR; Subkey: ".sh3l"; ValueType: string; ValueName: ""; ValueData: "eTeks Sweet Home 3D Language Library"; Flags: uninsdeletevalue
Root: HKCR; Subkey: "eTeks Sweet Home 3D Language Library"; ValueType: string; ValueName: ""; ValueData: "Sweet Home 3D"; Flags: uninsdeletekey
Root: HKCR; Subkey: "eTeks Sweet Home 3D Language Library\DefaultIcon"; ValueType: string; ValueName: ""; ValueData: "{app}\SweetHome3D.exe,0"
Root: HKCR; Subkey: "eTeks Sweet Home 3D Language Library\shell\open\command"; ValueType: string; ValueName: ""; ValueData: """{app}\SweetHome3D.exe"" -open ""%1"""

Root: HKCR; Subkey: ".sh3f"; ValueType: string; ValueName: ""; ValueData: "eTeks Sweet Home 3D Furniture Library"; Flags: uninsdeletevalue
Root: HKCR; Subkey: "eTeks Sweet Home 3D Furniture Library"; ValueType: string; ValueName: ""; ValueData: "Sweet Home 3D"; Flags: uninsdeletekey
Root: HKCR; Subkey: "eTeks Sweet Home 3D Furniture Library\DefaultIcon"; ValueType: string; ValueName: ""; ValueData: "{app}\SweetHome3D.exe,0"
Root: HKCR; Subkey: "eTeks Sweet Home 3D Furniture Library\shell\open\command"; ValueType: string; ValueName: ""; ValueData: """{app}\SweetHome3D.exe"" -open ""%1"""

Root: HKCR; Subkey: ".sh3t"; ValueType: string; ValueName: ""; ValueData: "eTeks Sweet Home 3D Textures Library"; Flags: uninsdeletevalue
Root: HKCR; Subkey: "eTeks Sweet Home 3D Furniture Library"; ValueType: string; ValueName: ""; ValueData: "Sweet Home 3D"; Flags: uninsdeletekey
Root: HKCR; Subkey: "eTeks Sweet Home 3D Furniture Library\DefaultIcon"; ValueType: string; ValueName: ""; ValueData: "{app}\SweetHome3D.exe,0"
Root: HKCR; Subkey: "eTeks Sweet Home 3D Furniture Library\shell\open\command"; ValueType: string; ValueName: ""; ValueData: """{app}\SweetHome3D.exe"" -open ""%1"""

Root: HKCR; Subkey: ".sh3p"; ValueType: string; ValueName: ""; ValueData: "eTeks Sweet Home 3D Plugin"; Flags: uninsdeletevalue
Root: HKCR; Subkey: "eTeks Sweet Home 3D Plugin"; ValueType: string; ValueName: ""; ValueData: "Sweet Home 3D"; Flags: uninsdeletekey
Root: HKCR; Subkey: "eTeks Sweet Home 3D Plugin\DefaultIcon"; ValueType: string; ValueName: ""; ValueData: "{app}\SweetHome3D.exe,0"
Root: HKCR; Subkey: "eTeks Sweet Home 3D Plugin\shell\open\command"; ValueType: string; ValueName: ""; ValueData: """{app}\SweetHome3D.exe"" -open ""%1"""
