@echo off
setlocal enabledelayedexpansion

for %%I in ("%~dp0.") do set "SCRIPT_DIR=%%~fI"
for %%I in ("%SCRIPT_DIR%\..") do set "REPO_ROOT=%%~fI"
set "DIST_DIR=%REPO_ROOT%\dist"
set "OUT_APK=%DIST_DIR%\GalaxyEnabler.apk"
set "RELEASE_APK=%SCRIPT_DIR%\app\build\outputs\apk\release\app-release.apk"

if not defined JAVA_HOME (
  if exist "%ProgramFiles%\Android\Android Studio\jbr\bin\java.exe" (
    set "JAVA_HOME=%ProgramFiles%\Android\Android Studio\jbr"
  )
)

if not defined ANDROID_HOME (
  if defined ANDROID_SDK_ROOT (
    set "ANDROID_HOME=%ANDROID_SDK_ROOT%"
  ) else if exist "%LOCALAPPDATA%\Android\Sdk" (
    set "ANDROID_HOME=%LOCALAPPDATA%\Android\Sdk"
  )
)

call "%SCRIPT_DIR%\gradlew.bat" --no-daemon -p "%SCRIPT_DIR%" :app:assembleRelease
if errorlevel 1 exit /b %errorlevel%

if not exist "%RELEASE_APK%" (
  echo Release APK not found at "%RELEASE_APK%" 1>&2
  exit /b 1
)

if not exist "%DIST_DIR%" mkdir "%DIST_DIR%"
copy /y "%RELEASE_APK%" "%OUT_APK%" >nul
if errorlevel 1 exit /b %errorlevel%

for %%A in ("%OUT_APK%") do set "SIZE_BYTES=%%~zA"
echo Built universal APK: %OUT_APK% (%SIZE_BYTES% bytes)
endlocal
