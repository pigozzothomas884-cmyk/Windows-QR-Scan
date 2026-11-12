# QR Scan

Lightweight open-source Java desktop utility for Windows to generate QR codes in real time and decode existing ones directly from a screen capture or by loading an image file.

## Features

- **Real-Time QR Code Generation:** Create custom QR codes on the fly.
- **Screen Capture Scanner:** Select and scan any QR code directly from your screen.
- **Image File Reader:** Load local PNG/image files to decode embedded QR codes.
- **System Tray Integration:** Quick access from the Windows notification area.
- **Self-Contained Runtime:** Standalone Windows installer bundling its own Java runtime without requiring administrator privileges.

## Requirements (Building from Source)

- **JDK 17** or later
- **Maven 3.6** or later
- **Inno Setup 6** (for building the Windows installer)
- Windows with system tray icon support

## Build & Run

From the root directory of the repository:

```powershell
mvn clean package
java -jar target\qr-scan-1.0.0.jar
```

The generated JAR includes ZXing and all required dependencies without needing an external classpath.

## Building the Windows Installer

The application uses `src\main\resources\QRScanLogo.png` as the executable icon and `src\main\resources\InstallerIcon.ico` for the setup wizard. To package the standalone installer (`dist\QRScan-Setup-1.0.0.exe`):

```powershell
mvn clean verify
.\packaging\build-installer.ps1 -JavaHome 'C:\path\to\jdk-17'
```

The installation runs in user scope (no UAC elevation required) and verifies `runtime\bin\jimage.dll` prior to packaging.

## Code Signing

Free code signing provided by [SignPath.io](https://signpath.io/), certificate by [SignPath Foundation](https://signpath.org/).
