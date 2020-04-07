; signedInstallerInnoSetup.iss
;
; Sweet Home 3D, Copyright (c) 2007-2020 Emmanuel PUYBARET / eTeks <info@eteks.com>
;
; SweetHome3D-6.3-windows.exe setup program creator
; This UTF-8 BOM encoded script requires Inno Setup Unicode available at http://www.jrsoftware.org/isinfo.php
; and a build directory stored in current directory containing :
;   a SweetHome3D.exe file built with launch4j
; + a jre... subdirectory containing a dump of Windows JRE without the files mentioned 
;   in the JRE README.TXT file (JRE bin/javaw.exe command excepted)     
; + a lib subdirectory containing SweetHome3D.jar and Windows Java 3D DLLs and JARs for Java 3D
; + file COPYING.TXT

[Setup]
DisableWelcomePage=no
AppName=Sweet Home 3D
AppVersion=6.3
AppCopyright=Copyright (c) 2007-2020 eTeks
AppVerName=Sweet Home 3D version 6.3
AppPublisher=eTeks
AppPublisherURL=http://www.eteks.com
AppSupportURL=http://sweethome3d.sourceforge.net
AppUpdatesURL=http://sweethome3d.sourceforge.net
DisableDirPage=no
DefaultDirName={pf}\Sweet Home 3D
DefaultGroupName=eTeks Sweet Home 3D
LicenseFile=..\..\COPYING.TXT
OutputDir=.
OutputBaseFilename=SweetHome3D-6.3-windows
Compression=lzma2/ultra64
SolidCompression=yes
ChangesAssociations=yes
ExtraDiskSpaceRequired=107000000
VersionInfoVersion=6.3.0.0
VersionInfoTextVersion=6.3
VersionInfoDescription=Sweet Home 3D Setup
VersionInfoCopyright=Copyright (c) 2007-2020 eTeks
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
Name: "dutch"; MessagesFile: "compiler:Languages\Dutch.isl"
Name: "german"; MessagesFile: "compiler:Languages\German.isl"
Name: "czech"; MessagesFile: "compiler:Languages\Czech.isl"
Name: "polish"; MessagesFile: "compiler:Languages\Polish.isl"
Name: "spanish"; MessagesFile: "compiler:Languages\Spanish.isl"
Name: "hungarian"; MessagesFile: "compiler:Languages\Hungarian.isl"
Name: "russian"; MessagesFile: "compiler:Languages\Russian.isl"
Name: "greek"; MessagesFile: "compiler:Languages\Greek.isl"
Name: "japanese"; Messagesfile: "compiler:Languages\Japanese.isl"
Name: "swedish"; MessagesFile: "Swedish.isl"
Name: "chinesesimp"; Messagesfile: "ChineseSimplified.isl"
Name: "chinesetrad"; Messagesfile: "ChineseTraditional.isl"
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
Source: "build\lib\jeksparser-calculator*.jar"; DestDir: "{app}\lib"; Flags: ignoreversion
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
Source: "build\SweetHome3D-java3d-1.5.2-x86.exe"; DestDir: "{app}"; DestName: "SweetHome3D.exe"; Flags: ignoreversion; Check: not Is64BitInstalled and IsJava3D152Installed and not IsARM64
; Install program for not 64 bit and not Java 3D 1.5.2
Source: "build\SweetHome3D-x86.exe"; DestDir: "{app}"; DestName: "SweetHome3D.exe"; Flags: ignoreversion; Check: not Is64BitInstalled and not IsJava3D152Installed
; Install program for 64 bit and Java 3D 1.5.2
Source: "build\SweetHome3D-java3d-1.5.2-x64.exe"; DestDir: "{app}"; DestName: "SweetHome3D.exe"; Flags: ignoreversion; Check: Is64BitInstalled and IsJava3D152Installed
; Install program for 64 bit and not Java 3D 1.5.2
Source: "build\SweetHome3D-x64.exe"; DestDir: "{app}"; DestName: "SweetHome3D.exe"; Flags: ignoreversion; Check: Is64BitInstalled and not IsJava3D152Installed
; Install program for ARM 64 bit 
Source: "build\SweetHome3D-java3d-1.5.2-x86-d3d.exe"; DestDir: "{app}"; DestName: "SweetHome3D.exe"; Flags: ignoreversion; Check: not Is64BitInstalled and IsJava3D152Installed and IsARM64

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
; Delete files created by Launch4j
Type: filesandordirs; Name: "{app}\jre8\launch4j-tmp"
; Delete unpacked jars
Type: files; Name: "{app}\jre8\lib\rt.jar"
Type: dirifempty; Name: "{app}\jre8\lib" 
Type: dirifempty; Name: "{app}\jre8" 
Type: files; Name: "{app}\lib\SweetHome3D.jar"
Type: dirifempty; Name: "{app}\lib" 
Type: dirifempty; Name: "{app}" 

[CustomMessages]
ArchitectureLabel=Architecture:
french.ArchitectureLabel=Architecture :
spanish.ArchitectureLabel=Arquitectura:
italian.ArchitectureLabel=Architettura:
dutch.ArchitectureLabel=Programmatuur opbouw:
german.ArchitectureLabel=Architektur:
portuguese.ArchitectureLabel=Arquitectura:
brazilianportuguese.ArchitectureLabel=Arquitectura:
swedish.ArchitectureLabel=Arkitektur:
czech.ArchitectureLabel=Architektura:
polish.ArchitectureLabel=Architektura:
greek.ArchitectureLabel=Αρχιτεκτονική:
bulgarian.ArchitectureLabel=Архитектура:
russian.ArchitectureLabel=Архитектура:
chinesesimp.ArchitectureLabel=安装于:
chinesetrad.ArchitectureLabel=安裝於:
japanese.ArchitectureLabel=建築様式:

UninstallExistingVersionCheckBox=Uninstall previously installed version
french.UninstallExistingVersionCheckBox=Désinstaller la version installée précédemment
spanish.UninstallExistingVersionCheckBox=Desinstalar la anterior versión instalada
italian.UninstallExistingVersionCheckBox=Disinstalla versioni precedentemente installate
dutch.UninstallExistingVersionCheckBox=Verwijder eerder geïnstalleerde versie
german.UninstallExistingVersionCheckBox=Vorherige Version deinstallieren
portuguese.UninstallExistingVersionCheckBox=Desinstalar a versão anterior
brazilianportuguese.UninstallExistingVersionCheckBox=Desinstalar a versão anterior
swedish.UninstallExistingVersionCheckBox=Avinstallera föregående version
czech.UninstallExistingVersionCheckBox=Odinstalovat předchozí nainstalovanou verzi
polish.UninstallExistingVersionCheckBox=Odinstaluj poprzednią zainstalowaną wersję
greek.UninstallExistingVersionCheckBox=Απεγκατάσταση προηγούμενης έκδοσης
bulgarian.UninstallExistingVersionCheckBox=Деинсталирай предишната версия
russian.UninstallExistingVersionCheckBox=Деинсталлировать предыдущую версию
chinesesimp.UninstallExistingVersionCheckBox=卸除以前安装的版本
chinesetrad.UninstallExistingVersionCheckBox=卸載以前安裝的版本
japanese.UninstallExistingVersionCheckBox=前インストールバージョン削除

SweetHome3DComment=Arrange the furniture of your house
french.SweetHome3DComment=Aménagez les meubles de votre logement
portuguese.SweetHome3DComment=Organiza as mobilias da sua casa
brazilianportuguese.SweetHome3DComment=Organiza as mobilias da sua casa
czech.SweetHome3DComment=Sestavte si design interieru vaseho domu
polish.SweetHome3DComment=Zaprojektuj wnetrze swojego domu
hungarian.SweetHome3DComment=Keszitse el lakasanak belso kialakitasat!

UnpackingMessage=Unpacking %1...
french.UnpackingMessage=Décompression du fichier %1...

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
var architecture64Bit : boolean;
var uninstallExistingVersionCheckBox : TCheckBox;
  
function IsJava3D152Installed: Boolean;
var
  windowsVersion : TWindowsVersion;
  requiredJava3DVersion : String;
  i : Integer;
begin
  (* Uses by default Java 3D 1.5.2 under Windows 7 included or under 64 bit systems when 32 bit is selected *)
  GetWindowsVersionEx(windowsVersion);
  Result := (windowsVersion.major < 6) 
        or ((windowsVersion.major = 6) and (windowsVersion.major < 2))
        or (Is64BitInstallMode and (not architecture64Bit))
        or IsARM64;
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
begin
  Result := architecture64Bit;
end; 

(* Updates installation dir according to selected architecture *)
procedure UpdateInstallationDir();
var 
  subDir : String;
begin
  subDir := '';
  if (Pos(ExpandConstant('{pf32}'), WizardForm.DirEdit.Text + '\') = 1) then
    begin 
      subDir := Copy(WizardForm.DirEdit.Text, Length(ExpandConstant('{pf32}')) + 1, 
          Length(WizardForm.DirEdit.Text) - Length(ExpandConstant('{pf32}')));
    end
  else if (Pos(ExpandConstant('{pf64}'), WizardForm.DirEdit.Text + '\') = 1) then
    subDir := Copy(WizardForm.DirEdit.Text, Length(ExpandConstant('{pf64}')) + 1, 
        Length(WizardForm.DirEdit.Text) - Length(ExpandConstant('{pf64}')));
  
  if (Length(subDir) <> 0) then
    begin
      if architecture64Bit then
        begin
          WizardForm.DirEdit.Text := ExpandConstant('{pf64}') + subDir;
        end
      else 
        begin
          WizardForm.DirEdit.Text := ExpandConstant('{pf32}') + subDir;
        end
    end;
end;

procedure UpdateArchitecture32Bit(sender: TObject);
begin
  architecture64Bit := not TRadioButton(sender).Checked;
  UpdateInstallationDir();
end;

procedure UpdateArchitecture64Bit(sender: TObject);
begin
  architecture64Bit := TRadioButton(sender).Checked;
  UpdateInstallationDir();
end;

function GetExistingVersionUninstallPath : String;
var
  uninstallKeyName : String;
  uninstallPath : String;
begin
  uninstallKeyName := 'Software\Microsoft\Windows\CurrentVersion\Uninstall\Sweet Home 3D_is1';
  uninstallPath := '';
  if not RegQueryStringValue(HKLM, uninstallKeyName, 'UninstallString', uninstallPath) then
    RegQueryStringValue(HKCU, uninstallKeyName, 'UninstallString', uninstallPath);
  Result := uninstallPath;
end;

(* Run at wizard launch *)
procedure InitializeWizard;
var
  windowsVersion : TWindowsVersion;
  requiredArchitecture : String;
  i : Integer;
  page: TNewNotebookPage;
  architecturePanel : TPanel;
  architectureLabel : TLabel;
  x86RadioButton : TRadioButton;
  x64RadioButton : TRadioButton;
begin
  architecture64Bit := Is64BitInstallMode;
  if architecture64Bit then 
    begin 
      (* Install in 32 bit under Windows 10 by default *)
      GetWindowsVersionEx(windowsVersion);
      architecture64Bit := (windowsVersion.major < 10) and not IsARM64;
      (* Search if required architecture in /os.arch custom param isn't 64 *)
      for i := 1 to ParamCount do
        if Pos('/os.arch=', ParamStr(i)) = 1 then
          begin
            requiredArchitecture := Copy(ParamStr(i), Length('/os.arch=') + 1, Length(ParamStr(i)));
            architecture64Bit := Pos('64', requiredArchitecture) > 0;      
            break;
          end; 
      UpdateInstallationDir();
    end;
  
  (* Update installation dir selection page with architecture 32/64 bit radio buttons *)
  page := WizardForm.SelectDirPage;

  architecturePanel := TPanel.create(page);  
  architecturePanel.Top := WizardForm.DirEdit.Top + WizardForm.DirEdit.Height + 30;
  architecturePanel.BevelOuter := bvNone;
  architecturePanel.Width := 400;
  architecturePanel.Height := WizardForm.YesRadio.Height;
  architecturePanel.Visible := Is64BitInstallMode and not IsARM64;
  architecturePanel.Parent := page;

  architectureLabel := TLabel.Create(page);
  architectureLabel.Caption := CustomMessage('ArchitectureLabel');
  architectureLabel.AutoSize := True;
  architectureLabel.Parent := architecturePanel;

  x86RadioButton := TRadioButton.Create(page);
  x86RadioButton.Caption := '32 bit';
  x86RadioButton.Left := architectureLabel.Width + 10;
  x86RadioButton.Height := architecturePanel.Height;
  x86RadioButton.Checked := not architecture64Bit;
  x86RadioButton.OnClick := @UpdateArchitecture32Bit; 
  x86RadioButton.Parent := architecturePanel;

  x64RadioButton := TRadioButton.Create(page);
  x64RadioButton.Caption := '64 bit';
  x64RadioButton.Left := x86RadioButton.Left + 100;
  x64RadioButton.Height := architecturePanel.Height;
  x64RadioButton.Checked := architecture64Bit;
  x64RadioButton.OnClick := @UpdateArchitecture64Bit; 
  x64RadioButton.Parent := architecturePanel;

  uninstallExistingVersionCheckBox := TCheckBox.Create(page);
  uninstallExistingVersionCheckBox.Top := architecturePanel.Top + architecturePanel.Height + 10;
  uninstallExistingVersionCheckBox.Caption := CustomMessage('UninstallExistingVersionCheckBox');
  uninstallExistingVersionCheckBox.Height := architecturePanel.Height;
  uninstallExistingVersionCheckBox.Width := 600;
  uninstallExistingVersionCheckBox.Checked := Is64BitInstallMode;
  uninstallExistingVersionCheckBox.Visible := GetExistingVersionUninstallPath <> '';
  uninstallExistingVersionCheckBox.Parent := page;
end;

procedure CurStepChanged(currentStep: TSetupStep);
var 
  previousVersionUninstallPath : String;
  resultCode : Integer;
begin
  if ((currentStep = ssInstall)
	  and uninstallExistingVersionCheckBox.Checked) then
	begin
	  (* Uninstall previous version if requested *) 
      previousVersionUninstallPath := GetExistingVersionUninstallPath;
      if previousVersionUninstallPath <> '' then 
        Exec(RemoveQuotes(previousVersionUninstallPath), '/VERYSILENT /NORESTART /SUPPRESSMSGBOXES', 
             '', SW_HIDE, ewWaitUntilTerminated, resultCode);
  end;
end;
