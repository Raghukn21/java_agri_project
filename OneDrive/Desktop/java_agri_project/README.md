# AgriGuard AI – 100% Pure Java Full-Plant Disease Detection & Decision Support

> **An intelligent, multi-crop, whole-plant agricultural health diagnosis and agronomic decision-support platform built 100% in Java.**

---

## 1. Project Vision

**AgriGuard AI** addresses the real-world agricultural challenge of early, accurate, and multi-faceted plant disease detection. Built entirely in Java (Spring Boot 3.3.x) with native computer vision and colorimetric canopy decomposition:

- **Inputs**:
  - Full-plant images (capturing canopy, stems, lower foliage, and soil context).
  - Optional metadata: Crop name, growing environment (*Open Field, Greenhouse, Container/Pot, Hydroponic, Orchard*), growth stage (*Seedling, Vegetative, Flowering, Fruiting, Harvest*), and observable symptoms.
- **Outputs**:
  - Overall health status: **`HEALTHY`**, **`DISEASED`**, or **`STRESSED`**.
  - Pathogen & stress classification: Fungal, Bacterial, Viral, Sucking Pests, and Abiotic conditions (*Nitrogen deficiency, Potassium deficiency, Iron chlorosis, Drought stress, Waterlogging root hypoxia, Heat/Sunscald*).
  - Severity level: **`LOW`**, **`MEDIUM`**, **`HIGH`** with visual severity meter.
  - Confidence scoring with smart calibration (< 55% confidence triggers soft advisory and agronomist consultation referral).
  - 4-tier actionable advisory plan: Immediate recovery steps, Organic bio-control, Chemical active formulations, and Long-term cultural prevention.
  - Agronomic Spray Safety Window: Evaluates temperature, wind speed, humidity, and rain risk before spraying.

---

## 2. Architecture & Data Flow (100% Pure Java)

```
 ┌────────────────────────────────────────────────────────┐
 │           Frontend: Modern Glassmorphic SPA            │
 │   - Full-Plant Image Drag-and-Drop & Live Camera       │
 │   - Agronomic Spray Window & Weather Simulator         │
 │   - Instant Knowledge Base Search & Journal CSV Export │
 │   - Multi-tab diagnosis & Printable Agronomy Report    │
 └───────────────────────────┬────────────────────────────┘
                             │ HTTP/REST (Multipart/JSON)
                             ▼
 ┌────────────────────────────────────────────────────────┐
 │            Java Backend (Spring Boot 3.3.x)            │
 │  - Enterprise layered design: Controller-Service-Repo  │
 │  - JWT stateless authentication & user management      │
 │  - Native Java 2D Computer Vision & Canopy Analyzer    │
 │  - Excess Green Index (ExG), Chlorosis & Necrosis Core │
 │  - Agronomic Decision-Support & Checklist Engine       │
 └───────────────────────────┬────────────────────────────┘
                             │ JPA / Hibernate
                             ▼
               ┌────────────────────────┐
               │ Database (MySQL / H2)  │
               │ - users                │
               │ - crops (15+ profiles) │
               │ - diseases (35+ entries│
               │ - diagnoses (history)  │
               └────────────────────────┘
```

---

## 3. Technology Stack

- **Java Backend**: Spring Boot 3.3.x, Java 17 / 21 LTS, Spring Data JPA, Spring Security (BCrypt), JWT (jjwt), Jakarta Validation, Java 2D ImageIO Computer Vision.
- **Frontend**: HTML5, Modern Vanilla CSS (Glassmorphism design system), Vanilla JavaScript (ES6+), Google Fonts (*Outfit*, *Plus Jakarta Sans*).
- **Database**: MySQL 8.0 (default) with zero-config embedded H2 fallback profile.
- **Zero Python / Zero External API Dependencies**: 100% self-contained Java executable.

---

## 4. Multi-Crop Knowledge Base

The system comes pre-seeded with **15+ popular crops** and **35+ crop-specific pathogens and universal abiotic stresses**:

| Crop Family | Crops Supported | Common Pathogens & Conditions |
| :--- | :--- | :--- |
| **Solanaceae** | Tomato, Potato, Chili & Bell Pepper | Early Blight, Late Blight, Yellow Leaf Curl, Bacterial Spot, Anthracnose |
| **Poaceae** | Corn / Maize, Rice, Wheat | Northern Corn Leaf Blight, Common Rust, Rice Blast, Wheat Leaf Rust |
| **Rosaceae** | Apple, Rose, Strawberry | Apple Scab, Black Rot, Rose Powdery Mildew, Leaf Scorch |
| **Malvaceae** | Cotton | Bacterial Blight (Angular Leaf Spot), Bollworm Damage |
| **Vitaceae** | Grape | Black Rot, Powdery Mildew |
| **Universal (All Crops)** | Any crop / Uncataloged plant | Nitrogen Deficiency, Potassium Deficiency, Iron Chlorosis, Drought Stress, Waterlogging / Hypoxia, Heat Stress / Sunscald, Aphid/Mite Infestation, Unknown Stress |

---

## 5. REST API Endpoints

### Diagnosis & Detection
- `POST /api/diagnose`: Upload multipart plant photo + optional crop name, environment, growth stage, notes. Returns full diagnosis and advisory payload.
- `GET /api/diagnoses`: Fetch user or system scan history.
- `GET /api/diagnoses/{id}`: Fetch single diagnosis details by ID.
- `DELETE /api/diagnoses/{id}`: Remove past diagnosis entry.

### Crops & Knowledge Base
- `GET /api/crops`: List all supported crops.
- `GET /api/crops/{id}`: Get detailed crop profile.
- `GET /api/crops/{id}/diseases`: Get all diseases associated with a crop.
- `GET /api/diseases`: List all disease and abiotic stress entries.
- `GET /api/diseases/{key}`: Fetch detailed disease advice by key.
- `GET /api/diseases/category/{category}`: Filter diseases by category.

### Analytics & Authentication
- `GET /api/stats`: Dashboard summary statistics.
- `POST /api/auth/register`: Register user account.
- `POST /api/auth/login`: Login and receive JWT bearer token.
- `GET /api/auth/me`: Get current logged-in user profile.

---

## 6. How to Run Locally

### One-Click Launch
Double-click `start_all.bat` (or run `./start_all.ps1` in PowerShell). This will:
1. Start the Java Spring Boot application at `http://localhost:8088`.
2. Automatically launch your default browser to `http://localhost:8088`.

---

### Manual Launch
```bash
cd backend
# Run on MySQL (database auto-creates):
.\mvnw.cmd spring-boot:run

# Or run with embedded H2 database (zero setup):
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=h2
```

Navigate to **`http://localhost:8088`** in your browser.

- Demo User Account: `farmer_john` / `agri123`
- Demo Admin Account: `admin` / `admin123`
- Guest mode works without login!

---

## 7. Automated Test Suite (JUnit 5)

To run the complete automated test suite:
```bash
cd backend
.\mvnw.cmd test
```
*All 20+ unit and integration test cases validate native Java vision inference, controllers, security, and advisory generation.*
