Write-Host "===============================================================" -ForegroundColor Green
Write-Host "       AgriGuard AI - 100% Pure Java Plant Disease System" -ForegroundColor Green
Write-Host "===============================================================" -ForegroundColor Green

$rootDir = Split-Path -Parent $MyInvocation.MyCommand.Path

Write-Host "`nStarting Java Spring Boot Backend & Native Vision Engine (port 8088)..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$rootDir\backend'; .\mvnw.cmd spring-boot:run `"-Dspring-boot.run.profiles=h2`""

Write-Host "`nWaiting for server to initialize..." -ForegroundColor Yellow
Start-Sleep -Seconds 6

Write-Host "Launching AgriGuard AI in default browser..." -ForegroundColor Green
Start-Process "http://localhost:8088"

Write-Host "`nApplication running at: http://localhost:8088" -ForegroundColor White
