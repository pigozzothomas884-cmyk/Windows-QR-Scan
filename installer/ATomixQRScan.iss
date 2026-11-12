#define MyAppName "QR Scan"
#define MyAppVersion "1.0.0"
#define MyAppPublisher "ATomix Corp"
#define AppImageDir "..\target\installer-app\QR Scan"

[Setup]
AppId={{44E3A657-A72F-4F0C-92CE-E1AA40D51D97}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppPublisher={#MyAppPublisher}
DefaultDirName={localappdata}\Programs\QR Scan
DefaultGroupName={#MyAppName}
PrivilegesRequired=lowest
ArchitecturesAllowed=x64
ArchitecturesInstallIn64BitMode=x64
MinVersion=10.0
SetupIconFile=..\src\main\resources\InstallerIcon.ico
UninstallDisplayIcon={app}\QR Scan.exe
OutputDir=..\dist
OutputBaseFilename=QRScan-Setup-{#MyAppVersion}
Compression=lzma2/ultra64
SolidCompression=yes
WizardStyle=modern
CloseApplications=yes
RestartApplications=no
VersionInfoCompany={#MyAppPublisher}
VersionInfoDescription=Installer for {#MyAppName}
VersionInfoProductName={#MyAppName}
VersionInfoProductVersion={#MyAppVersion}

[Languages]
Name: "italian"; MessagesFile: "compiler:Languages\Italian.isl"

[Tasks]
Name: "desktopicon"; Description: "Crea un collegamento sul desktop"; GroupDescription: "Collegamenti aggiuntivi:"

[Files]
Source: "{#AppImageDir}\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{autoprograms}\{#MyAppName}"; Filename: "{app}\QR Scan.exe"; WorkingDir: "{app}"
Name: "{autodesktop}\{#MyAppName}"; Filename: "{app}\QR Scan.exe"; WorkingDir: "{app}"; Tasks: desktopicon

[Run]
Filename: "{app}\QR Scan.exe"; Description: "Avvia {#MyAppName}"; Flags: nowait postinstall skipifsilent
