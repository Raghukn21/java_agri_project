-- AgriGuard AI Database Schema
-- Multi-Crop, Full-Plant Disease Detection & Advisory System

CREATE DATABASE IF NOT EXISTS agriguard_db;
USE agriguard_db;

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    role VARCHAR(20) DEFAULT 'ROLE_USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Crops Reference Table (Multi-Crop Knowledge Base)
CREATE TABLE IF NOT EXISTS crops (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,          -- e.g., "Tomato", "Cotton", "Corn"
    scientific_name VARCHAR(150),               -- e.g., "Solanum lycopersicum"
    family_name VARCHAR(100),                   -- e.g., "Solanaceae"
    common_diseases TEXT,                       -- JSON or comma-separated keys
    general_care TEXT,                          -- General watering, sunlight, soil advice
    optimal_temp VARCHAR(50),                   -- e.g., "20-28°C"
    optimal_ph VARCHAR(30),                     -- e.g., "6.0-6.8"
    watering_tips TEXT,
    icon_url VARCHAR(255)
);

-- 3. Diseases & Stresses Table (General + Crop-Specific)
CREATE TABLE IF NOT EXISTS diseases (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    disease_key VARCHAR(100) NOT NULL UNIQUE,   -- e.g., "tomato_early_blight", "nitrogen_deficiency_general"
    display_name VARCHAR(150) NOT NULL,         -- e.g., "Early Blight (Tomato)"
    crop_id BIGINT NULL,                        -- NULL if condition is general across multiple crops
    category ENUM('FUNGAL', 'BACTERIAL', 'VIRAL', 'PEST', 'ABIOTIC', 'UNKNOWN') NOT NULL,
    description TEXT,                           -- Overview of pathogen / root cause
    symptoms TEXT,                              -- Observable whole-plant & leaf symptoms
    treatment TEXT,                             -- Immediate corrective action & recovery steps
    prevention TEXT,                            -- Preventive agricultural practices
    severity_guidance TEXT,                     -- Criteria for Low, Medium, High severity
    organic_control TEXT,                       -- Bio-fungicides, neem oil, organic remedies
    chemical_control TEXT,                      -- Recommended fungicide / pesticide active ingredients
    is_general BOOLEAN DEFAULT FALSE,           -- TRUE if applies to all/many crops
    FOREIGN KEY (crop_id) REFERENCES crops(id) ON DELETE SET NULL
);

-- 4. Diagnoses History Table (Enhanced Full-Plant)
CREATE TABLE IF NOT EXISTS diagnoses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL,                        -- Optional (supports guest or authenticated user)
    image_path VARCHAR(255) NOT NULL,           -- Stored photo path
    original_file_name VARCHAR(255),
    predicted_crop VARCHAR(100),                -- Crop identified by model or supplied by user
    disease_id BIGINT NULL,                     -- Foreign key to diseases table
    disease_key VARCHAR(100) NOT NULL,
    confidence DECIMAL(5, 4) NOT NULL,          -- e.g., 0.9250
    severity ENUM('LOW', 'MEDIUM', 'HIGH') NOT NULL,
    health_status ENUM('HEALTHY', 'DISEASED', 'STRESSED') NOT NULL,
    growth_stage VARCHAR(50),                   -- e.g., "Vegetative", "Flowering", "Fruiting"
    environment VARCHAR(50),                    -- e.g., "Field", "Greenhouse", "Pot"
    user_notes TEXT,                            -- Symptoms observed by user
    model_notes TEXT,                           -- Diagnostic caveats, low-confidence warnings
    diagnostic_checklist TEXT,                  -- JSON or bulleted checklist generated for grower
    diagnosed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (disease_id) REFERENCES diseases(id) ON DELETE SET NULL
);

-- Indexing for performance
CREATE INDEX IF NOT EXISTS idx_diagnoses_user ON diagnoses(user_id);
CREATE INDEX IF NOT EXISTS idx_diagnoses_disease ON diagnoses(disease_key);
CREATE INDEX IF NOT EXISTS idx_diseases_category ON diseases(category);
CREATE INDEX IF NOT EXISTS idx_crops_name ON crops(name);
