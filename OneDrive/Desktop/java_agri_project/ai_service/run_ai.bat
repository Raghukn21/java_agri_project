@echo off
echo Starting AgriGuard AI Python Microservice on http://localhost:8000 ...
cd /d "%~dp0"
python -m uvicorn app:app --host 0.0.0.0 --port 8000 --reload
pause
