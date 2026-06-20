param([string]$Target = "build")

$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.19.10-hotspot"
$env:PATH      = "$env:JAVA_HOME\bin;$env:PATH"

$ADB     = "C:\Android\Sdk\platform-tools\adb.exe"
$PACKAGE = "se.familjenhed.minroj"
$APK     = Get-ChildItem "app\build\outputs\apk\debug\*.apk" | Select-Object -First 1 -ExpandProperty FullName

switch ($Target) {
    "build"      { .\gradlew assembleDebug }
    "install"    { .\gradlew assembleDebug; & $ADB install -r $APK }
    "uninstall"  { & $ADB uninstall $PACKAGE }
    "release"    { .\gradlew assembleRelease }
    "clean"      { .\gradlew clean }
    "distribute" { .\gradlew assembleDebug appDistributionUploadDebug }
    "test"       { .\gradlew test }
    "devices"    { & $ADB devices }
    "logcat"     {
        $pid = & $ADB shell pidof -s $PACKAGE
        & $ADB logcat --pid=$pid
    }
    default      { Write-Host "Targets: build, install, uninstall, release, distribute, clean, test, devices, logcat" }
}
