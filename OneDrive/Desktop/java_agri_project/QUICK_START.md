# Quick Start - AgriGuard DevOps

## ✅ What's Been Done

1. **Git Repository**: Project pushed to GitHub
   - URL: https://github.com/Raghukn21/java_agri_project
   - Branch: main

2. **Docker Configuration**:
   - ✅ Dockerfile (single image for all services)
   - ✅ Dockerfile.backend (Java Spring Boot)
   - ✅ ai_service/Dockerfile (Python Flask)
   - ✅ docker-compose.yml (complete orchestration)
   - ✅ nginx.conf (reverse proxy/frontend)

3. **DevOps Files**:
   - ✅ .gitignore (excludes unnecessary files)
   - ✅ .dockerignore (excludes build artifacts)
   - ✅ DEVOPS.md (detailed deployment guide)
   - ✅ docker-setup.sh (Linux/Mac setup script)
   - ✅ docker-setup.bat (Windows setup script)

## 🚀 Quick Start Commands

### Using Docker Compose (Easiest)

```bash
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

### Build Individual Images

```bash
# Backend
docker build -f Dockerfile.backend -t agriguard-backend:latest .

# AI Service
docker build -f ai_service/Dockerfile -t agriguard-ai:latest ./ai_service
```

### Push to Docker Hub

```bash
# Login to Docker Hub
docker login

# Tag images
docker tag agriguard-backend:latest USERNAME/agriguard-backend:latest
docker tag agriguard-ai:latest USERNAME/agriguard-ai:latest

# Push images
docker push USERNAME/agriguard-backend:latest
docker push USERNAME/agriguard-ai:latest
```

## 🔗 Access Points

| Service | URL | Port |
|---------|-----|------|
| Frontend | http://localhost | 80 |
| Backend API | http://localhost:8080 | 8080 |
| AI Service | http://localhost:5000 | 5000 |
| Database | localhost | 3306 |

## 📋 Services in Docker Compose

| Service | Image | Status |
|---------|-------|--------|
| db | mysql:8.0 | Database |
| backend | agriguard-backend:latest | Spring Boot |
| ai | agriguard-ai:latest | Flask AI |
| frontend | nginx:alpine | Reverse Proxy |

## 🔧 Useful Commands

```bash
# View images
docker images

# View running containers
docker ps

# View all containers
docker ps -a

# Container logs
docker logs container_name

# Execute command in container
docker exec -it container_name bash

# Remove stopped containers
docker container prune

# Remove unused images
docker image prune

# Full system cleanup
docker system prune -a
```

## ⚙️ Environment Variables

### Database (MySQL)
- Root Password: `root123`
- User: `root`
- Database: `agriguard`

### Backend Connection
- Spring Datasource: `jdbc:mysql://db:3306/agriguard`
- AI Service URL: `http://ai:5000`

## 📝 Dockerfile Locations

```
java_agri_project/
├── Dockerfile                  # All-in-one (not recommended)
├── Dockerfile.backend          # Java backend only
├── docker-compose.yml          # Full stack orchestration
├── ai_service/
│   └── Dockerfile              # Python AI service
├── nginx.conf                  # Reverse proxy config
├── DEVOPS.md                   # Full documentation
├── docker-setup.sh             # Linux/Mac setup
└── docker-setup.bat            # Windows setup
```

## 🐛 Troubleshooting

### Port Already in Use
```bash
# Find process on port 8080
lsof -i :8080

# Kill it
kill -9 PID
```

### Container Won't Start
```bash
# Check logs
docker logs container_name

# Detailed inspection
docker inspect container_name
```

### Docker Daemon Issues
```bash
# Restart Docker
sudo systemctl restart docker

# Or restart Docker Desktop (Windows/Mac)
```

## 📚 Next Steps

1. **Local Testing**
   - Build images locally
   - Test all services with Docker Compose
   - Verify API endpoints work

2. **Docker Hub Push**
   - Create Docker Hub account
   - Push images with your username
   - Tag images with versions

3. **CI/CD Pipeline**
   - Create GitHub Actions workflow
   - Auto-build on commits
   - Auto-push to Docker Hub

4. **Cloud Deployment**
   - Docker Swarm
   - Kubernetes
   - Azure Container Instances
   - AWS ECS
   - Google Cloud Run

## 🔑 Key Files to Review

1. **docker-compose.yml** - Service configuration
2. **Dockerfile.backend** - Backend build
3. **ai_service/Dockerfile** - AI service build
4. **nginx.conf** - Frontend routing
5. **DEVOPS.md** - Complete guide

## 📞 Support

For detailed information, see [DEVOPS.md](DEVOPS.md)

- Docker Docs: https://docs.docker.com
- Docker Hub: https://hub.docker.com
- GitHub: https://github.com/Raghukn21/java_agri_project

---

**Last Updated**: 2024
**Status**: Ready for deployment ✅
