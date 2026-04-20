@echo off
setlocal

set "PROJECT_DIR=%~dp0"
set "GRADLE_USER_HOME=%PROJECT_DIR%.gradle-user-home"

echo Using GRADLE_USER_HOME=%GRADLE_USER_HOME%
call "%PROJECT_DIR%gradlew.bat" app:installDebug
if errorlevel 1 exit /b %errorlevel%

adb shell monkey -p com.spacecolony -c android.intent.category.LAUNCHER 1
