@echo off
setlocal
set GRADLE_VERSION=8.11
set DIST=%USERPROFILE%\.gradle\wrapper\dists\gradle-%GRADLE_VERSION%\gradle-%GRADLE_VERSION%\bin\gradle.bat
if not exist "%DIST%" (
  echo Please install Gradle 8.11 or run this project in GitHub Actions.
  exit /b 1
)
call "%DIST%" %*
