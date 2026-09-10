@echo off
REM AgriGuard DevOps Setup Script for Windows

setlocal enabledelayedexpansion

echo.
echo 🚀 AgriGuard DevOps Setup Script
echo ================================
echo.

REM Check Docker installation
docker --version >nul 2>&1
if errorlevel 1 (
    echo ❌ Docker is not installed
    echo Download from: https://www.docker.com/products/docker-desktop
    pause
    exit /b 1
)

echo ✅ Docker is installed
docker --version

REM Check Docker daemon
docker ps >nul 2>&1
if errorlevel 1 (
    echo ❌ Docker daemon is not running
    echo Please start Docker Desktop
    pause
    exit /b 1
)

echo ✅ Docker daemon is running
echo.

REM Menu
echo Select an option:
echo 1. Build with Docker Compose (Recommended)
echo 2. Build individual images
echo 3. Push to Docker Hub
echo 4. Run containers
echo 5. Clean up (remove containers/images)
echo 6. View logs
echo 7. Stop all containers
echo 8. Exit
echo.

set /p choice="Enter your choice (1-8): "

if "%choice%"=="1" (
    echo Building with Docker Compose...
    docker-compose build
    echo ✅ Build completed
    set /p start="Start containers now? (y/n): "
    if "!start!"=="y" (
        docker-compose up -d
        echo ✅ Containers started
        echo.
        echo Frontend: http://localhost
        echo Backend: http://localhost:8080
        echo AI Service: http://localhost:5000
        echo Database: localhost:3306
    )
) else if "%choice%"=="2" (
    echo Building individual images...
    
    set /p build_backend="Build backend? (y/n): "
    if "!build_backend!"=="y" (
        echo Building backend...
        docker build -f Dockerfile.backend -t agriguard-backend:latest .
        echo ✅ Backend image built
    )
    
    set /p build_ai="Build AI service? (y/n): "
    if "!build_ai!"=="y" (
        echo Building AI service...
        docker build -f ai_service\Dockerfile -t agriguard-ai:latest .\ai_service
        echo ✅ AI service image built
    )
) else if "%choice%"=="3" (
    echo Pushing to Docker Hub...
    
    set /p username="Docker Hub username: "
    
    set /p push_backend="Push backend? (y/n): "
    if "!push_backend!"=="y" (
        docker tag agriguard-backend:latest !username!/agriguard-backend:latest
        docker push !username!/agriguard-backend:latest
        echo ✅ Backend image pushed
    )
    
    set /p push_ai="Push AI service? (y/n): "
    if "!push_ai!"=="y" (
        docker tag agriguard-ai:latest !username!/agriguard-ai:latest
        docker push !username!/agriguard-ai:latest
        echo ✅ AI service image pushed
    )
    
    echo ✅ Images pushed to Docker Hub
    echo View at: https://hub.docker.com/repositories
) else if "%choice%"=="4" (
    echo Starting containers...
    docker-compose up -d
    echo ✅ Containers started
    docker-compose ps
) else if "%choice%"=="5" (
    echo Cleaning up...
    docker-compose down -v
    echo ✅ Cleanup completed
) else if "%choice%"=="6" (
    echo Showing logs...
    docker-compose logs -f
) else if "%choice%"=="7" (
    echo Stopping containers...
    docker-compose down
    echo ✅ Containers stopped
) else if "%choice%"=="8" (
    echo Goodbye! 👋
    exit /b 0
) else (
    echo ❌ Invalid choice
    exit /b 1
)

echo.
echo ✅ Done!
pause
