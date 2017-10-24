; signedInstallerInnoSetup.iss
;
; Sweet Home 3D, Copyright (c) 2007-2017 Emmanuel PUYBARET / eTeks <info@eteks.com>
;
; SweetHome3D-5.6-windows.exe setup program creator
; This script requires Inno setup available at http://www.jrsoftware.org/isinfo.php
; and a build directory stored in current directory containing :
;   a SweetHome3D.exe file built with launch4j
; + a jre... subdirectory containing a dump of Windows JRE without the files mentioned 
;   in the JRE README.TXT file (JRE bin/javaw.exe command excepted)     
; + a lib subdirectory containing SweetHome3D.jar and Windows Java 3D DLLs and JARs for Java 3D
; + file COPYING.TXT

[Setup]
AppName=Sweet Home 3D
AppVersion=5.6
AppCopyright=Copyright (c) 2007-2017 eTeks
AppVerName=Sweet Home 3D version 5.6
AppPublisher=eTeks
AppPublisherURL=http://www.eteks.com
AppSupportURL=http://sweethome3d.sourceforge.net
AppUpdatesURL=http://sweethome3d.sourceforge.net
DefaultDirName={pf}\Sweet Home 3D
DefaultGroupName=eTeks Sweet Home 3D
LicenseFile=..\..\COPYING.TXT
OutputDir=.
OutputBaseFilename=SweetHome3D-5.6-windows
Compression=lzma2/ultra64
SolidCompression=yes
ChangesAssociations=yes
VersionInfoVersion=5.6.0.0
VersionInfoTextVersion=5.6
VersionInfoDescription=Sweet Home 3D Setup
VersionInfoCopyright=Copyright (c) 2007-2017 eTeks
VersionInfoCompany=eTeks
; Install in 64 bit mode if possible
ArchitecturesInstallIn64BitMode=x64
; Signing
;
; Requires keys.p12 in current directory
; Enter password and define SignToolPgm in Ant with following tasks:
; <input message="Enter signature password:" 
;        addproperty="password"/> 
; <exec executable="C:\Program Files\Inno Setup 5\ISCC.exe">
;    <arg value="/sSignToolPgm=$$qC:\Program Files (x86)\Windows Kits\8.1\bin\x86\signtool.exe$$q sign /f $$q${basedir}\keys.p12$$q /p ${password} $p"/>
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
Name: "greek"; MessagesFile: "compiler:Languages\Greek.isl"
Name: "japanese"; Messagesfile: "compiler:Languages\Japanese.isl"
Name: "swedish"; MessagesFile: "Swedish.isl"
Name: "chinesesimp"; Messagesfile: "ChineseSimp.isl"
Name: "bulgarian"; Messagesfile: "Bulgarian.isl"

[Tasks]
Name: desktopicon; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"

[InstallDelete]
; Remove old jres
Type: filesandordirs; Name: "{app}\jre6"
Type: filesandordirs; Name: "{app}\jre1.8.0_51"
Type: filesandordirs; Name: "{app}\jre1.8.0_60"
Type: filesandordirs; Name: "{app}\jre1.8.0_66"
; Remove Java3D 1.5.2 if not used
Type: files; Name: "{app}\lib\vecmath.jar"; Check: not IsJava3D152Installed
Type: files; Name: "{app}\lib\j3d*.jar"; Check: not IsJava3D152Installed
Type: files; Name: "{app}\lib\j3d*.dll"; Check: not IsJava3D152Installed
; Remove other Java3D if Java3D 1.5.2 used
Type: filesandordirs; Name: "{app}\lib\java3d-1.6"; Check: IsJava3D152Installed

[Files]
Source: "build\*.TXT"; DestDir: "{app}"; Flags: ignoreversion 
Source: "build\lib\SweetHome3D.pack.gz"; DestDir: "{app}\lib"; Flags: ignoreversion
Source: "build\lib\Furniture.jar"; DestDir: "{app}\lib"; Flags: ignoreversion
Source: "build\lib\Textures.jar"; DestDir: "{app}\lib"; Flags: ignoreversion
Source: "build\lib\Examples.jar"; DestDir: "{app}\lib"; Flags: ignoreversion
Source: "build\lib\Help.jar"; DestDir: "{app}\lib"; Flags: ignoreversion
Source: "build\lib\batik-svgpathparser-*.jar"; DestDir: "{app}\lib"; Flags: ignoreversion
Source: "build\lib\sunflow-*.jar"; DestDir: "{app}\lib"; Flags: ignoreversion
Source: "build\lib\freehep-vectorgraphics-svg-*.jar"; DestDir: "{app}\lib"; Flags: ignoreversion
Source: "build\lib\iText-*.jar"; DestDir: "{app}\lib"; Flags: ignoreversion
Source: "build\lib\jmf.jar"; DestDir: "{app}\lib"; Flags: ignoreversion
; Install Java 3D 1.5.2 Jars
Source: "build\lib\j3d*.jar"; DestDir: "{app}\lib"; Flags: ignoreversion; Check: IsJava3D152Installed
Source: "build\lib\vecmath.jar"; DestDir: "{app}\lib"; Flags: ignoreversion; Check: IsJava3D152Installed
; Install Java 3D not 1.5.2 Jars
Source: "build\lib\java3d-1.6\*.jar"; DestDir: "{app}\lib\java3d-1.6"; Flags: ignoreversion; Check: not IsJava3D152Installed
; Install JRE and Java 3D for not 64 bit
Source: "build\jre8\x86\*"; DestDir: "{app}\jre8"; Flags: ignoreversion recursesubdirs createallsubdirs; Check: not Is64BitInstalled
Source: "build\lib\x86\*.dll"; DestDir: "{app}\lib"; Flags: ignoreversion; Check: not Is64BitInstalled and IsJava3D152Installed
Source: "build\lib\java3d-1.6\x86\*.dll"; DestDir: "{app}\lib\java3d-1.6"; Flags: ignoreversion; Check: not Is64BitInstalled and not IsJava3D152Installed
; Install JRE and Java 3D for 64 bit
Source: "build\jre8\x64\*"; DestDir: "{app}\jre8"; Flags: ignoreversion recursesubdirs createallsubdirs; Check: Is64BitInstalled
Source: "build\lib\x64\*.dll"; DestDir: "{app}\lib"; Flags: ignoreversion; Check: Is64BitInstalled and IsJava3D152Installed
Source: "build\lib\java3d-1.6\x64\*.dll"; DestDir: "{app}\lib\java3d-1.6"; Flags: ignoreversion; Check: Is64BitInstalled and not IsJava3D152Installed
; Install program for not 64 bit and Java 3D 1.5.2
Source: "build\SweetHome3D-java3d-1.5.2-x86.exe"; DestDir: "{app}"; DestName: "SweetHome3D.exe"; Flags: ignoreversion; Check: not Is64BitInstalled and IsJava3D152Installed
; Install program for not 64 bit and not Java 3D 1.5.2
Source: "build\SweetHome3D-x86.exe"; DestDir: "{app}"; DestName: "SweetHome3D.exe"; Flags: ignoreversion; Check: not Is64BitInstalled and not IsJava3D152Installed
; Install program for 64 bit and Java 3D 1.5.2
Source: "build\SweetHome3D-java3d-1.5.2-x64.exe"; DestDir: "{app}"; DestName: "SweetHome3D.exe"; Flags: ignoreversion; Check: Is64BitInstalled and IsJava3D152Installed
; Install program for 64 bit and not Java 3D 1.5.2
Source: "build\SweetHome3D-x64.exe"; DestDir: "{app}"; DestName: "SweetHome3D.exe"; Flags: ignoreversion; Check: Is64BitInstalled and not IsJava3D152Installed

[Icons]
Name: "{group}\Sweet Home 3D"; Filename: "{app}\SweetHome3D.exe"; Comment: "{cm:SweetHome3DComment}"
Name: "{group}\{cm:UninstallProgram,Sweet Home 3D}"; Filename: "{uninstallexe}"
Name: "{userdesktop}\Sweet Home 3D"; Filename: "{app}\SweetHome3D.exe"; Tasks: desktopicon; Comment: "{cm:SweetHome3DComment}"

[Run]
; Unpack largest jars
Filename: "{app}\jre8\bin\unpack200.exe"; Parameters:"-r -q ""{app}\jre8\lib\rt.pack.gz"" ""{app}\jre8\lib\rt.jar"""; Flags: runhidden; StatusMsg: "{cm:UnpackingMessage,rt.jar}";
Filename: "{app}\jre8\bin\unpack200.exe"; Parameters:"-r -q ""{app}\lib\SweetHome3D.pack.gz"" ""{app}\lib\SweetHome3D.jar"""; StatusMsg: "{cm:UnpackingMessage,SweetHome3D.jar}"; Flags: runhidden
; Propose user to launch Sweet Home 3D at installation end
Filename: "{app}\SweetHome3D.exe"; Description: "{cm:LaunchProgram,Sweet Home 3D}"; Flags: nowait postinstall skipifsilent

[UninstallDelete]
; Delete unpacked jars
Type: files; Name: "{app}\jre8\lib\rt.jar"
Type: files; Name: "{app}\lib\SweetHome3D.jar"
; Delete files created by Launch4j
Type: filesandordirs; Name: "{app}\jre8\launch4j-tmp"

[CustomMessages]
SweetHome3DComment=Arrange the furniture of your house
french.SweetHome3DComment=Am閚agez les meubles de votre logement
portuguese.SweetHome3DComment=Organiza as mobilias da sua casa
brazilianportuguese.SweetHome3DComment=Organiza as mobilias da sua casa
czech.SweetHome3DComment=Sestavte si design interieru vaseho domu
polish.SweetHome3DComment=Zaprojektuj wnetrze swojego domu
hungarian.SweetHome3DComment=Keszitse el lakasanak belso kialakitasat!
chinesesimp.SweetHome3DComment=布置您的温馨小家
UnpackingMessage=Unpacking %1...
french.UnpackingMessage=D閏ompression du fichier %1...

[Registry]
Root: HKCR; Subkey: ".sh3d"; ValueType: string; ValueName: ""; ValueData: "eTeks Sweet Home 3D"; Flags: uninsdeletevalue
Root: HKCR; Subkey: ".sh3x"; ValueType: string; ValueName: ""; ValueData: "eTeks Sweet Home 3D"; Flags: uninsdeletevalue
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

[Code]
function IsJava3D152Installed: Boolean;
var
  windowsVersion : TWindowsVersion;
  requiredJava3DVersion : String;
  i : Integer;
begin
  (* Uses by default Java 3D 1.5.2 under Windows 7 included *)
  GetWindowsVersionEx(windowsVersion);
  Result := (windowsVersion.major < 6) or 
      (windowsVersion.major = 6) and (windowsVersion.major < 2);
  (* Search required Java 3D version in j3d.version custom param *)
  for i := 1 to ParamCount do
    if Pos('/j3d.version=', ParamStr(i)) = 1 then
      begin
        requiredJava3DVersion := Copy(ParamStr(i), Length('/j3d.version=') + 1, Length(ParamStr(i)));        
        Result := requiredJava3DVersion = '1.5.2';
        break;
      end;
end; 

function Is64BitInstalled: Boolean;
var
  windowsVersion : TWindowsVersion;
  requiredArchitecture : String;
  i : Integer;
begin
  Result := Is64BitInstallMode;
  if Result then 
    (* Search if required architecture in os.arch custom param isn't 64 *)
    for i := 1 to ParamCount do
      if Pos('/os.arch=', ParamStr(i)) = 1 then
        begin
          requiredArchitecture := Copy(ParamStr(i), Length('/os.arch=') + 1, Length(ParamStr(i)));
          Result := Pos('64', requiredArchitecture) > 0;      
          break;
        end;
end; 