@echo off
title AgriGuard AI Launcher (100%% Pure Java Backend)
color 0A
echo ===============================================================
echo        AgriGuard AI - 100%% Pure Java Plant Disease System
echo ===============================================================
echo.
echo Starting Spring Boot Java Backend with Native Vision Engine (Port 8088)...
echo.
start "AgriGuard Pure Java Backend" cmd /k "cd /d %~dp0backend && mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=h2"

echo.
echo ===============================================================
echo  Application is initializing:
echo    - Web Application:   http://localhost:8088
echo    - REST Endpoints:    http://localhost:8088/api/diagnose
echo    - Knowledge Base:    http://localhost:8088/api/crops
echo ===============================================================
echo.
echo Waiting 6 seconds for Spring Boot server to start...
timeout /t 6 /nobreak >nul
start http://localhost:8088
echo.
echo Press any key to exit this launcher window (backend keeps running in its window).
pause >nul
