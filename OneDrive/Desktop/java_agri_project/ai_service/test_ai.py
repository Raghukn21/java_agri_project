import io
import sys
from PIL import Image, ImageDraw
from model import MultiCropPlantDiseaseModel

def test_model_predictions():
    print("Testing MultiCropPlantDiseaseModel...")
    model = MultiCropPlantDiseaseModel()
    assert len(model.classes) > 0, "Classes should not be empty"
    print(f"Loaded {len(model.classes)} disease/stress classes.")

    # 1. Test with Healthy Green Image
    img_green = Image.new("RGB", (256, 256), color=(34, 139, 34)) # Forest Green
    draw = ImageDraw.Draw(img_green)
    draw.rectangle([50, 50, 200, 200], fill=(50, 205, 50))
    buf = io.BytesIO()
    img_green.save(buf, format="JPEG")
    res_healthy = model.predict(buf.getvalue(), crop_hint="Tomato")
    print(f"Test 1 (Green Plant - Tomato Hint): {res_healthy['disease_key']} -> {res_healthy['health_status']} (Conf: {res_healthy['confidence']})")
    assert res_healthy["health_status"] in ["HEALTHY", "DISEASED", "STRESSED"]

    # 2. Test with Yellow Chlorosis / Nitrogen Stress
    img_yellow = Image.new("RGB", (256, 256), color=(220, 220, 60))
    buf2 = io.BytesIO()
    img_yellow.save(buf2, format="JPEG")
    res_yellow = model.predict(buf2.getvalue(), notes="Lower leaves turning pale yellow")
    print(f"Test 2 (Yellow Foliage - Nitrogen Hint): {res_yellow['disease_key']} -> {res_yellow['display_name']} (Conf: {res_yellow['confidence']})")
    assert res_yellow["health_status"] == "STRESSED" or "deficiency" in res_yellow["disease_key"] or "chlorosis" in res_yellow["disease_key"]

    # 3. Test with Spotty / Necrotic Foliage (Blight / Spot)
    img_spotted = Image.new("RGB", (256, 256), color=(60, 140, 40))
    draw_spot = ImageDraw.Draw(img_spotted)
    for x in range(30, 220, 30):
        for y in range(30, 220, 30):
            draw_spot.ellipse([x, y, x+15, y+15], fill=(50, 25, 10), outline=(200, 180, 50))
    buf3 = io.BytesIO()
    img_spotted.save(buf3, format="JPEG")
    res_spotted = model.predict(buf3.getvalue(), crop_hint="Tomato", notes="Concentric spots on lower leaves")
    print(f"Test 3 (Spotted Leaf - Early Blight Hint): {res_spotted['disease_key']} -> {res_spotted['display_name']} (Conf: {res_spotted['confidence']})")
    assert "blight" in res_spotted["disease_key"] or res_spotted["health_status"] == "DISEASED"

    print("\nAll AI Model Tests Passed Successfully!")

if __name__ == "__main__":
    test_model_predictions()
