import os
from typing import Optional
from fastapi import FastAPI, File, Form, UploadFile, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from model import MultiCropPlantDiseaseModel

app = FastAPI(
    title="AgriGuard AI - Multi-Crop Plant Disease Vision Service",
    description="Deep learning and multi-feature vision API for full-plant disease, pest, and abiotic stress detection.",
    version="1.0.0"
)

# CORS middleware for local frontend and backend integration
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

model = MultiCropPlantDiseaseModel()

@app.get("/")
def root():
    return {
        "service": "AgriGuard AI Vision Microservice",
        "status": "ONLINE",
        "supported_crops_count": 15,
        "classes_count": len(model.classes),
        "docs_url": "/docs"
    }

@app.get("/health")
def health_check():
    return {
        "status": "UP",
        "classes_loaded": len(model.classes),
        "service": "AgriGuard AI"
    }

@app.get("/classes")
def get_classes():
    return {
        "count": len(model.classes),
        "classes": model.classes
    }

@app.get("/crops")
def get_crops():
    crops = sorted(list(set(info.get("crop", "General Plant") for info in model.classes.values())))
    return {
        "count": len(crops),
        "crops": crops
    }

@app.post("/predict")
async def predict_plant_disease(
    image: UploadFile = File(...),
    crop_name: Optional[str] = Form(None),
    location: Optional[str] = Form(None),
    environment: Optional[str] = Form(None),
    growth_stage: Optional[str] = Form(None),
    notes: Optional[str] = Form(None)
):
    try:
        image_bytes = await image.read()
        if not image_bytes:
            raise HTTPException(status_code=400, detail="Uploaded file is empty.")

        env = environment or location
        result = model.predict(
            image_bytes=image_bytes,
            crop_hint=crop_name,
            environment=env,
            growth_stage=growth_stage,
            notes=notes
        )
        return result
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Prediction error: {str(e)}")

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("app:app", host="0.0.0.0", port=8000, reload=True)
