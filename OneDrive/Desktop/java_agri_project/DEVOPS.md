# DevOps Setup Guide

This guide explains how to build Docker images and push them to Docker Hub.

## Prerequisites

- Docker installed and running
- Docker Hub account (free: https://hub.docker.com)
- Git configured with GitHub credentials
- Java 17+ (for local builds without Docker)
- Maven 3.6+ (for local builds without Docker)
- Python 3.8+ (for local AI service)

## GitHub Repository

Your project is now hosted on GitHub:
- **Repository**: https://github.com/Raghukn21/java_agri_project.git
- **Branch**: main

## Docker Images

### Option 1: Build with Docker Compose (Recommended)

This will build all services with proper networking and database.

```bash
# Navigate to project directory
cd java_agri_project

# Build all services
docker-compose build

# Run all services
docker-compose up -d

# Check status
docker-compose ps

# View logs
docker-compose logs -f

# Stop all services
docker-compose down
```

### Option 2: Build Individual Docker Images

#### 1. Build Backend Image

```bash
cd java_agri_project

docker build -f Dockerfile.backend -t agriguard-backend:latest .

# Tag for Docker Hub
docker tag agriguard-backend:latest Raghukn21/agriguard-backend:latest
docker tag agriguard-backend:latest Raghukn21/agriguard-backend:1.0
```

#### 2. Build AI Service Image

```bash
cd java_agri_project/ai_service

docker build -t agriguard-ai:latest .

# Tag for Docker Hub
docker tag agriguard-ai:latest Raghukn21/agriguard-ai:latest
docker tag agriguard-ai:latest Raghukn21/agriguard-ai:1.0
```

#### 3. Run Individual Containers

```bash
# Run MySQL Database
docker run -d \
  --name agri_db \
  -e MYSQL_ROOT_PASSWORD=root123 \
  -e MYSQL_DATABASE=agriguard \
  -p 3306:3306 \
  -v mysql_data:/var/lib/mysql \
  mysql:8.0

# Wait for DB to start, then run backend
docker run -d \
  --name agriguard_backend \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://agri_db:3306/agriguard \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=root123 \
  -e AI_SERVICE_URL=http://agriguard_ai:5000 \
  -p 8080:8080 \
  --link agri_db:db \
  agriguard-backend:latest

# Run AI Service
docker run -d \
  --name agriguard_ai \
  -p 5000:5000 \
  agriguard-ai:latest
```

## Push to Docker Hub

### 1. Login to Docker Hub

```bash
docker login

# Enter your Docker Hub username and password
# (or use --username and --password-stdin for CI/CD)
```

### 2. Push Backend Image

```bash
docker push Raghukn21/agriguard-backend:latest
docker push Raghukn21/agriguard-backend:1.0
```

### 3. Push AI Service Image

```bash
docker push Raghukn21/agriguard-ai:latest
docker push Raghukn21/agriguard-ai:1.0
```

### 4. Verify on Docker Hub

Visit: https://hub.docker.com/repositories (replace `Raghukn21` with your username)

## Access Your Application

- **Frontend**: http://localhost (via nginx)
- **Backend API**: http://localhost:8080
- **AI Service**: http://localhost:5000
- **Database**: localhost:3306

### Test API Endpoints

```bash
# Health check
curl http://localhost:8080/health

# Get crops
curl http://localhost:8080/api/crops

# Predict disease (requires image)
curl -X POST -F "file=@image.jpg" http://localhost:5000/predict
```

## Environment Variables

Create a `.env` file for local development:

```env
# Database
MYSQL_ROOT_PASSWORD=root123
MYSQL_DATABASE=agriguard
MYSQL_USER=agri
MYSQL_PASSWORD=agri123

# Backend
SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/agriguard
AI_SERVICE_URL=http://ai:5000

# AI Service
FLASK_ENV=production
```

## CI/CD Integration

### GitHub Actions Example

Create `.github/workflows/docker-build.yml`:

```yaml
name: Build and Push Docker Images

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up Docker Buildx
      uses: docker/setup-buildx-action@v2
    
    - name: Login to Docker Hub
      uses: docker/login-action@v2
      with:
        username: ${{ secrets.DOCKER_USERNAME }}
        password: ${{ secrets.DOCKER_PASSWORD }}
    
    - name: Build and push backend
      uses: docker/build-push-action@v4
      with:
        context: .
        file: ./Dockerfile.backend
        push: true
        tags: |
          ${{ secrets.DOCKER_USERNAME }}/agriguard-backend:latest
          ${{ secrets.DOCKER_USERNAME }}/agriguard-backend:${{ github.sha }}
    
    - name: Build and push AI service
      uses: docker/build-push-action@v4
      with:
        context: ./ai_service
        file: ./ai_service/Dockerfile
        push: true
        tags: |
          ${{ secrets.DOCKER_USERNAME }}/agriguard-ai:latest
          ${{ secrets.DOCKER_USERNAME }}/agriguard-ai:${{ github.sha }}
```

**Setup GitHub Secrets**:
1. Go to Settings → Secrets and variables → Actions
2. Add `DOCKER_USERNAME`
3. Add `DOCKER_PASSWORD`

## Troubleshooting

### Docker daemon not running
```bash
# Windows: Start Docker Desktop
# Or use WSL2 backend
```

### Permission denied while building
```bash
# Restart Docker
docker restart

# Or check Docker daemon
sudo systemctl restart docker
```

### Port already in use
```bash
# Find process using port
lsof -i :8080
lsof -i :5000
lsof -i :3306

# Kill process (Windows)
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

### Container exited
```bash
# Check logs
docker logs agriguard_backend
docker logs agriguard_ai
docker logs agri_db

# Inspect container
docker inspect agriguard_backend
```

## Useful Commands

```bash
# List all images
docker images

# List all containers
docker ps -a

# Remove image
docker rmi image_name

# Remove container
docker rm container_name

# View image layers
docker history image_name

# Build with no cache
docker build --no-cache -t image_name .

# Docker stats
docker stats

# Prune unused resources
docker system prune -a

# Build and push in one command
docker buildx build --push -t user/image:tag .
```

## Next Steps

1. ✅ Push project to GitHub
2. ✅ Create Dockerfiles
3. ✅ Test Docker locally
4. ⬜ Set up GitHub Actions for CI/CD
5. ⬜ Deploy to Kubernetes/Docker Swarm
6. ⬜ Set up monitoring and logging

## Deployment Options

- **Docker Hub**: https://hub.docker.com
- **Azure Container Registry (ACR)**: Better for Azure deployments
- **AWS ECR**: For AWS deployments
- **Google Cloud Run**: Serverless container deployment
- **Kubernetes**: For orchestration
- **Docker Swarm**: Simpler orchestration

## Resources

- Docker Docs: https://docs.docker.com
- Docker Hub: https://hub.docker.com
- Docker Compose: https://docs.docker.com/compose
- GitHub Actions: https://docs.github.com/en/actions
