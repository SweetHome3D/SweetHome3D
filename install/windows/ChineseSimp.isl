; *** Inno Setup version 5.1.11+ Simplified Chinese messages ***
;
; Based on previous version by Peng Bai and Mack Zhang
;
; To download user-contributed translations of this file, go to:
;   http://www.jrsoftware.org/files/istrans/
;
; Note: When translating this text, do not add periods (.) to the end of
; messages that didn't have them already, because on those messages Inno
; Setup adds the periods automatically (appending a period would result in
; two periods being displayed).

[LangOptions]
LanguageName=<7B80><4F53><4E2D><6587>
LanguageID=$0804
LanguageCodePage=936
DialogFontName=宋体
DialogFontSize=9
WelcomeFontName=宋体
WelcomeFontSize=12
TitleFontName=宋体
TitleFontSize=29
CopyrightFontName=宋体
CopyrightFontSize=9

[Messages]

; *** 应用程序标题
SetupAppTitle=安装
SetupWindowTitle=安装 - %1
UninstallAppTitle=卸载
UninstallAppFullTitle=%1 卸载

; *** 其他公用信息
InformationTitle=信息
ConfirmTitle=确认
ErrorTitle=错误

; *** SetupLdr 信息
SetupLdrStartupMessage=现在将开始安装 %1。您想要继续吗？
LdrCannotCreateTemp=无法创建临时文件。安装中断
LdrCannotExecTemp=无法执行临时目录中的文件。安装中断

; *** 启动错误信息
LastErrorMessage=%1.%n%n错误 %2: %3
SetupFileMissing=安装目录中缺少文件 %1。请纠正该问题，或者获取一个新的程序副本。
SetupFileCorrupt=安装文件已损坏。请获取一个新的程序副本。
SetupFileCorruptOrWrongVer=安装文件已损坏，或者与此安装程序的版本不兼容。请纠正该问题，或者获取一个新的程序副本。
NotOnThisPlatform=此程序无法在 %1 上运行。
OnlyOnThisPlatform=此程序必须在 %1 上运行。
OnlyOnTheseArchitectures=此程序只能安装在下列处理器结构设计的 Windows 版本上:%n%n%1
MissingWOW64APIs=当前 Windows 版本不包含执行 64 位安装程序所需的功能。要纠正该问题，请安装 Service Pack %1。
WinVersionTooLowError=此程序需要 %1 v%2 或更高版本。
WinVersionTooHighError=此程序不能在 %1 v%2 或更高版本上安装。
AdminPrivilegesRequired=安装此程序时，您必须以管理员身份登录。
PowerUserPrivilegesRequired=安装此程序时，您必须以管理员或 Power Users 成员的身份登录！
SetupAppRunningError=安装程序检测到 %1 正在运行。%n%n请立即关闭其所有实例，然后单击“确定”继续，或者单击“取消”退出。
UninstallAppRunningError=卸载程序检测到 %1 正在运行。%n%n请立即关闭其所有实例，然后单击“确定”继续，或者单击“取消”退出。

; *** 其他错误信息
ErrorCreatingDir=安装程序无法创建目录“%1”
ErrorTooManyFilesInDir=无法在目录“%1”中创建文件，因为目录中的文件过多

; *** 安装公用信息
ExitSetupTitle=退出安装
ExitSetupMessage=安装尚未完成。如果您现在退出安装程序，程序将不会被安装。%n%n您可以以后再运行安装程序来完成安装。%n%n要退出安装吗?
AboutSetupMenuItem=关于安装程序(&A)...
AboutSetupTitle=关于安装程序
AboutSetupMessage=%1 版本 %2%n%3%n%n%1 主页:%n%4
AboutSetupNote=
TranslatorNote=

; *** 按钮
ButtonBack=< 上一步(&B)
ButtonNext=下一步(&N) >
ButtonInstall=安装(&I)
ButtonOK=确定
ButtonCancel=取消
ButtonYes=是(&Y)
ButtonYesToAll=全部是(&A)
ButtonNo=否(&N)
ButtonNoToAll=全部否(&O)
ButtonFinish=完成(&F)
ButtonBrowse=浏览(&B)...
ButtonWizardBrowse=浏览(&R)...
ButtonNewFolder=新建文件夹(&M)

; *** "选择语言" 对话框信息
SelectLanguageTitle=选择安装语言
SelectLanguageLabel=请选择安装过程中使用的语言:

; *** 公用向导文本
ClickNext=单击“下一步”继续，或者单击“取消”退出安装。
BeveledLabel=
BrowseDialogTitle=浏览文件夹
BrowseDialogLabel=在下面的列表中选择一个文件夹，然后单击“确定”。
NewFolderName=新建文件夹

; *** "欢迎" 向导页
WelcomeLabel1=欢迎使用 [name] 安装向导
WelcomeLabel2=现在将开始安装 [name/ver]。%n%n建议您在继续操作之前关闭其他应用程序。

; *** "密码" 向导页
WizardPassword=密码
PasswordLabel1=此安装程序受密码保护。
PasswordLabel3=请输入密码，然后单击“下一步”继续。密码区分大小写。
PasswordEditLabel=密码(&P):
IncorrectPassword=您输入的密码不正确。请重新输入。

; *** "许可协议" 向导页
WizardLicense=许可协议
LicenseLabel=请在继续安装之前阅读下面的重要信息。
LicenseLabel3=请阅读下面的许可协议。您必须接受此协议中的条款，才能继续安装。
LicenseAccepted=我接受(&A)
LicenseNotAccepted=我不接受(&D)

; *** "信息" 向导页
WizardInfoBefore=信息
InfoBeforeLabel=请在继续安装之前阅读下面的重要信息。
InfoBeforeClickLabel=如要继续安装，请单击“下一步”。
WizardInfoAfter=信息
InfoAfterLabel=请在继续安装之前阅读下面的重要信息。
InfoAfterClickLabel=当你准备继续安装时，请单击“下一步”。

; *** "用户信息" 向导页
WizardUserInfo=用户信息
UserInfoDesc=请输入你的信息。
UserInfoName=用户名(&U):
UserInfoOrg=组织(&O):
UserInfoSerial=序列号(&S):
UserInfoNameRequired=你必须输入用户名。

; *** "选择目标位置" 向导页
WizardSelectDir=选择目标位置
SelectDirDesc=您要将 [name] 安装到何处?
SelectDirLabel3=安装程序将把 [name] 安装到下面的文件夹中。
SelectDirBrowseLabel=若要继续，请单击“下一步”。如果您想把程序安装到其他文件夹，请单击“浏览”。
DiskSpaceMBLabel=至少需要 [mb] MB 可用磁盘空间。
ToUNCPathname=安装程序无法安装到 UNC 路径。如果您试图安装到网络中，请映射一个网络驱动器。
InvalidPath=您必须输入一个带盘符的完整路径，例如:%n%nC:\APP%n%n或者以下格式的 UNC 路径:%n%n\\server\share
InvalidDrive=你选择的驱动器或 UNC 路径不存在或无法访问。请重新选择。
DiskSpaceWarningTitle=磁盘空间不足
DiskSpaceWarning=安装程序至少需要 %1 KB 的可用空间，但您选择的驱动器仅有 %2 KB 可用。%n%n无论如何也要继续吗?
DirNameTooLong=文件夹名称或路径过长。
InvalidDirName=文件夹名称无效。
BadDirName32=文件夹名称不能包含以下字符:%n%n%1
DirExistsTitle=文件夹已存在
DirExists=文件夹:%n%n%1%n%n已存在。无论如何也要安装到此文件夹中吗?
DirDoesntExistTitle=文件夹不存在
DirDoesntExist=文件夹:%n%n%1%n%n不存在。您要创建此文件夹吗?

; *** "选择组件" 向导页
WizardSelectComponents=选择组件
SelectComponentsDesc=您要安装哪些组件?
SelectComponentsLabel2=请选择您要安装的组件，清除不想安装的组件。单击“下一步”继续。
FullInstallation=完全安装
CompactInstallation=精简安装
CustomInstallation=自定义安装
NoUninstallWarningTitle=组件已存在
NoUninstallWarning=安装程序检测到您的计算机中已经安装了下列组件:%n%n%1%n%n取消选择这些组件并不会将它们卸载。%n%n无论如何也要继续吗？
ComponentSize1=%1 KB
ComponentSize2=%1 MB
ComponentsDiskSpaceMBLabel=当前所选组件至少需要 [mb] MB 可用磁盘空间。

; *** "选择附加任务" 向导页
WizardSelectTasks=选择附加任务
SelectTasksDesc=您要执行哪些附加任务?
SelectTasksLabel2=请选择要在 [name] 的安装过程中执行的附加任务，然后单击“下一步”。

; *** "选择开始菜单文件夹" 向导页
WizardSelectProgramGroup=选择开始菜单文件夹
SelectStartMenuFolderDesc=您要在何处创建程序的快捷方式?
SelectStartMenuFolderLabel3=安装程序将在下面的开始菜单文件夹中创建程序的快捷方式。
SelectStartMenuFolderBrowseLabel=若要继续，请单击“下一步”。如果您要在其他文件夹中创建快捷方式，请单击“浏览”。
MustEnterGroupName=您必须输入一个文件夹名称。
GroupNameTooLong=文件夹名称或路径过长。
InvalidGroupName=文件夹名称无效。
BadGroupName=文件夹名称不能包含以下字符:%n%n%1
NoProgramGroupCheck2=不要创建开始菜单文件夹(&D)

; *** "准备安装" 向导页
WizardReady=准备安装
ReadyLabel1=安装程序已经准备好在您的计算机上安装 [name]。
ReadyLabel2a=单击“安装”继续。如果您想修改前面的设置，请单击“上一步”。
ReadyLabel2b=单击“安装”继续。
ReadyMemoUserInfo=用户信息:
ReadyMemoDir=目标位置:
ReadyMemoType=安装类型:
ReadyMemoComponents=选择的组件:
ReadyMemoGroup=开始菜单文件夹:
ReadyMemoTasks=附加任务:

; *** "正在准备安装" 向导页
WizardPreparing=正在准备安装
PreparingDesc=安装程序正准备在您的计算机上安装 [name]。
PreviousInstallNotCompleted=之前安装的程序尚未完成安装或卸载的过程。您需要重新启动计算机来完成这些操作。%n%n请在重新启动后再次运行安装程序完成 [name] 的安装。
CannotContinue=安装程序无法继续。请单击“取消”退出。

; *** "正在安装" 向导页
WizardInstalling=正在安装
InstallingLabel=正在安装 [name]，请稍候...

; *** "安装完成" 向导页
FinishedHeadingLabel=[name] 安装完成
FinishedLabelNoIcons=安装程序已将 [name] 安装到了您的计算机中。
FinishedLabel=安装程序已将 [name] 安装到了您的计算机中。现在您可以通过程序图标来运行应用程序。
ClickFinish=请单击“完成”退出安装。
FinishedRestartLabel=若要完成 [name] 的安装，必须重新启动计算机。您现在要重新启动吗?
FinishedRestartMessage=若要完成 [name] 的安装，必须重新启动计算机。%n%n您现在要重新启动吗?
ShowReadmeCheck=是，我要查看自述文件
YesRadio=是(&Y)，立即重新启动计算机
NoRadio=否(&N)，我将在稍后自行重启计算机
RunEntryExec=运行 %1
RunEntryShellExec=查看 %1

; *** "安装程序需要下一张磁盘" 信息
ChangeDiskTitle=安装程序需要下一张磁盘
SelectDiskLabel2=请插入磁盘 %1 并单击“确定”。%n%n如果磁盘上所需的文件不在下面所显示的文件夹中，请输入正确的路径或单击“浏览”。
PathLabel=路径(&P):
FileNotInDir2=文件“%1”不在“%2”中。请插入正确的磁盘，或者选择其他文件夹。
SelectDirectoryLabel=请指定下一张磁盘的位置。

; *** 安装阶段信息
SetupAborted=安装程序未能完成运行。%n%n请纠正问题后重新运行安装程序。
EntryAbortRetryIgnore=单击“重试”再次尝试，单击“忽略”继续，或单击“中止”取消安装。

; *** 安装状态信息
StatusCreateDirs=正在创建目录...
StatusExtractFiles=正在提取文件...
StatusCreateIcons=正在创建快捷方式...
StatusCreateIniEntries=正在创建 INI 条目...
StatusCreateRegistryEntries=正在创建注册表条目...
StatusRegisterFiles=正在注册文件...
StatusSavingUninstall=正在保存卸载信息...
StatusRunProgram=正在完成安装...
StatusRollback=正在撤销更改...

; *** 其他信息
ErrorInternal2=内部错误: %1
ErrorFunctionFailedNoCode=%1 失败
ErrorFunctionFailed=%1 失败。代码 %2
ErrorFunctionFailedWithMessage=%1 失败。代码 %2.%n%3
ErrorExecutingProgram=无法执行文件:%n%1

; *** 注册表错误
ErrorRegOpenKey=打开注册表键出错:%n%1\%2
ErrorRegCreateKey=创建注册表键出错:%n%1\%2
ErrorRegWriteKey=写入注册表键出错:%n%1\%2

; *** INI 错误
ErrorIniEntry=在文件“%1”中创建 INI 条目时出错。

; *** 文件复制错误
FileAbortRetryIgnore=单击“重试”再次尝试，单击“忽略”跳过此文件 (不推荐)，或单击“中止”取消安装。
FileAbortRetryIgnore2=单击“重试”再次尝试，单击“忽略”继续 (不推荐)，或单击“中止”取消安装。
SourceIsCorrupted=源文件已损坏
SourceDoesntExist=源文件“%1”不存在
ExistingFileReadOnly=现有文件被标记为只读。%n%n单击“重试”去除只读属性并再次尝试，单击“忽略”跳过此文件，或单击“中止”取消安装。
ErrorReadingExistingDest=尝试读取现有文件时出错:
FileExists=文件已存在。%n%n您要将其覆盖吗？
ExistingFileNewer=现有文件比要安装的更新。建议保留现有文件。%n%n要保留现有文件吗?
ErrorChangingAttr=试图更改现有文件的属性时出错:
ErrorCreatingTemp=试图在目标目录中创建文件时出错:
ErrorReadingSource=试图读取源文件时出错:
ErrorCopying=试图复制文件时出错:
ErrorReplacingExistingFile=试图替换现有文件时出错:
ErrorRestartReplace=重启后替换失败:
ErrorRenamingTemp=试图重命名目标目录中的文件时出错:
ErrorRegisterServer=无法注册 DLL/OCX: %1
ErrorRegSvr32Failed=RegSvr32 失败。退出代码 %1
ErrorRegisterTypeLib=无法注册类型库: %1

; *** 递交安装错误
ErrorOpeningReadme=试图打开自述文件时出错。
ErrorRestartingComputer=安装程序无法重新启动计算机。请手动操作。

; *** 卸载程序信息
UninstallNotFound=文件“%1”不存在。无法卸载
UninstallOpenError=无法打开“%1”文件。无法卸载
UninstallUnsupportedVer=卸载日志“%1”的格式无法被此版本的卸载程序识别！无法卸载
UninstallUnknownEntry=卸载日志中遇到未知条目 (%1)
ConfirmUninstall=您是否确定要完全卸载 %1 及其全部组件?
UninstallOnlyOnWin64=此安装只能在 64 位 Windows 上卸载。
OnlyAdminCanUninstall=此安装只能由管理员卸载。
UninstallStatusLabel=正在卸载 %1，请稍候...
UninstalledAll=%1 已成功卸载。
UninstalledMost=%1 卸载完成。%n%n某些内容无法清自动清除，请手动操作。
UninstalledAndNeedsRestart=若要完成 %1 的卸载，您必须重新启动计算机。%n%n要立即重新启动吗？
UninstallDataCorrupted=文件“%1”已损坏。无法卸载

; *** 卸载阶段信息
ConfirmDeleteSharedFileTitle=是否删除共享文件?
ConfirmDeleteSharedFile2=下面的共享文件被系统标记为不再被其他程序使用。是否删除?%n%n如果其他程序仍然使用此文件，那些程序可能无法正常运行。如果您无法确定，请选择“否”。保留此文件对您的系统不会造成任何损害。
SharedFileNameLabel=文件名:
SharedFileLocationLabel=位置:
WizardUninstalling=卸载状态
StatusUninstalling=正在卸载 %1...

[CustomMessages]

NameAndVersion=%1 v%2
AdditionalIcons=附加图标:
CreateDesktopIcon=创建桌面图标(&D)
CreateQuickLaunchIcon=创建快速启动栏图标(&Q)
ProgramOnTheWeb=%1 主页
UninstallProgram=卸载 %1
LaunchProgram=运行 %1
AssocFileExtension=将 %2 文件类型与 %1 关联(&A)
AssocingFileExtension=正在将 %2 文件类型与 %1 关联...
