@echo off

if /I "%1"=="0" (
  taskkill /FI "WINDOWTITLE eq Backend-myblog - mvn  clean spring-boot:run" /T /F
  exit /b
)

if /I "%1"=="1" (
  start "Backend-myblog" /D "%~dp0backend" cmd /k "mvn clean spring-boot:run"
  exit /b 
)

if /I "%1"=="2" (
  start "Backend-myblog" /D "%~dp0backend" cmd /k "mvn clean spring-boot:run"
  start "Frontend-myblog" /D "%~dp0frontend" cmd /k "npm run dev"
  exit /b
)

