#!/bin/bash

# AgriGuard DevOps Setup Script

set -e

echo "🚀 AgriGuard DevOps Setup Script"
echo "================================"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check Docker installation
if ! command -v docker &> /dev/null; then
    echo -e "${RED}❌ Docker is not installed${NC}"
    echo "Download from: https://www.docker.com/products/docker-desktop"
    exit 1
fi

echo -e "${GREEN}✅ Docker is installed${NC}"
docker --version

# Check Docker daemon
if ! docker ps &> /dev/null; then
    echo -e "${RED}❌ Docker daemon is not running${NC}"
    echo "Please start Docker Desktop"
    exit 1
fi

echo -e "${GREEN}✅ Docker daemon is running${NC}"

# Menu
echo ""
echo "Select an option:"
echo "1. Build with Docker Compose (Recommended)"
echo "2. Build individual images"
echo "3. Push to Docker Hub"
echo "4. Run containers"
echo "5. Clean up (remove containers/images)"
echo "6. View logs"
echo "7. Stop all containers"
echo "8. Exit"

read -p "Enter your choice (1-8): " choice

case $choice in
    1)
        echo -e "${YELLOW}Building with Docker Compose...${NC}"
        docker-compose build
        echo -e "${GREEN}✅ Build completed${NC}"
        read -p "Start containers now? (y/n): " start
        if [ "$start" = "y" ]; then
            docker-compose up -d
            echo -e "${GREEN}✅ Containers started${NC}"
            echo ""
            echo "Frontend: http://localhost"
            echo "Backend: http://localhost:8080"
            echo "AI Service: http://localhost:5000"
            echo "Database: localhost:3306"
        fi
        ;;
    
    2)
        echo -e "${YELLOW}Building individual images...${NC}"
        
        read -p "Build backend? (y/n): " build_backend
        if [ "$build_backend" = "y" ]; then
            echo "Building backend..."
            docker build -f Dockerfile.backend -t agriguard-backend:latest .
            echo -e "${GREEN}✅ Backend image built${NC}"
        fi
        
        read -p "Build AI service? (y/n): " build_ai
        if [ "$build_ai" = "y" ]; then
            echo "Building AI service..."
            docker build -f ai_service/Dockerfile -t agriguard-ai:latest ./ai_service
            echo -e "${GREEN}✅ AI service image built${NC}"
        fi
        ;;
    
    3)
        echo -e "${YELLOW}Pushing to Docker Hub...${NC}"
        
        read -p "Docker Hub username: " username
        
        read -p "Push backend? (y/n): " push_backend
        if [ "$push_backend" = "y" ]; then
            docker tag agriguard-backend:latest $username/agriguard-backend:latest
            docker push $username/agriguard-backend:latest
            echo -e "${GREEN}✅ Backend image pushed${NC}"
        fi
        
        read -p "Push AI service? (y/n): " push_ai
        if [ "$push_ai" = "y" ]; then
            docker tag agriguard-ai:latest $username/agriguard-ai:latest
            docker push $username/agriguard-ai:latest
            echo -e "${GREEN}✅ AI service image pushed${NC}"
        fi
        
        echo -e "${GREEN}✅ Images pushed to Docker Hub${NC}"
        echo "View at: https://hub.docker.com/repositories"
        ;;
    
    4)
        echo -e "${YELLOW}Starting containers...${NC}"
        docker-compose up -d
        echo -e "${GREEN}✅ Containers started${NC}"
        docker-compose ps
        ;;
    
    5)
        echo -e "${YELLOW}Cleaning up...${NC}"
        docker-compose down -v
        echo -e "${GREEN}✅ Cleanup completed${NC}"
        ;;
    
    6)
        echo -e "${YELLOW}Showing logs...${NC}"
        docker-compose logs -f
        ;;
    
    7)
        echo -e "${YELLOW}Stopping containers...${NC}"
        docker-compose down
        echo -e "${GREEN}✅ Containers stopped${NC}"
        ;;
    
    8)
        echo "Goodbye! 👋"
        exit 0
        ;;
    
    *)
        echo -e "${RED}Invalid choice${NC}"
        exit 1
        ;;
esac

echo ""
echo -e "${GREEN}Done!${NC}"
