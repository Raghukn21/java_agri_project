// ==========================================================================
// AgriGuard AI - Multimodal Agricultural Copilot Application Logic
// ==========================================================================

const API_BASE = "";

// State
let attachedFile = null;
let currentDiagnosis = null;
let token = localStorage.getItem("agriguard_token") || null;
let currentUser = null;
let webcamStream = null;
let allCrops = [];
let allDiseases = [];
let chatHistory = [];
let threeScene, threeCamera, threeRenderer, threePlantMesh, threeParticles;

// DOM Ready
document.addEventListener("DOMContentLoaded", () => {
    initApp();
});

function initApp() {
    initThreeJsHologram();
    setupAuth();
    loadKnowledgeBase();
    loadDashboardStats();
    loadHistory();
    updateStandaloneSpray();
    setupPromptAutoResize();
}

// ==========================================================================
// 3D Holographic Plant Canvas (Three.js)
// ==========================================================================
function initThreeJsHologram() {
    const container = document.getElementById("threejsCanvasContainer");
    if (!container || typeof THREE === "undefined") return;

    try {
        const width = container.clientWidth || 220;
        const height = container.clientHeight || 220;

        threeScene = new THREE.Scene();
        threeCamera = new THREE.PerspectiveCamera(45, width / height, 0.1, 1000);
        threeCamera.position.z = 5;

        threeRenderer = new THREE.WebGLRenderer({ alpha: true, antialias: true });
        threeRenderer.setSize(width, height);
        threeRenderer.setPixelRatio(window.devicePixelRatio || 1);
        container.innerHTML = "";
        container.appendChild(threeRenderer.domElement);

        // 3D Wireframe Icosahedron (Representing Bio-Cell / Plant Node)
        const geometry = new THREE.IcosahedronGeometry(1.6, 2);
        const material = new THREE.MeshBasicMaterial({
            color: 0x10B981,
            wireframe: true,
            transparent: true,
            opacity: 0.75
        });
        threePlantMesh = new THREE.Mesh(geometry, material);
        threeScene.add(threePlantMesh);

        // Inner glowing core
        const coreGeo = new THREE.IcosahedronGeometry(0.9, 1);
        const coreMat = new THREE.MeshBasicMaterial({
            color: 0x3B82F6,
            wireframe: true,
            transparent: true,
            opacity: 0.5
        });
        const coreMesh = new THREE.Mesh(coreGeo, coreMat);
        threePlantMesh.add(coreMesh);

        // Surrounding Particle Field
        const particleCount = 80;
        const particleGeo = new THREE.BufferGeometry();
        const positions = new Float32Array(particleCount * 3);

        for (let i = 0; i < particleCount * 3; i += 3) {
            positions[i] = (Math.random() - 0.5) * 5;
            positions[i + 1] = (Math.random() - 0.5) * 5;
            positions[i + 2] = (Math.random() - 0.5) * 5;
        }

        particleGeo.setAttribute('position', new THREE.BufferAttribute(positions, 3));
        const particleMat = new THREE.PointsMaterial({
            color: 0x6EE7B7,
            size: 0.05,
            transparent: true,
            opacity: 0.8
        });
        threeParticles = new THREE.Points(particleGeo, particleMat);
        threeScene.add(threeParticles);

        // Animation Loop
        function animate() {
            requestAnimationFrame(animate);
            if (threePlantMesh) {
                threePlantMesh.rotation.x += 0.005;
                threePlantMesh.rotation.y += 0.008;
            }
            if (threeParticles) {
                threeParticles.rotation.y -= 0.002;
            }
            threeRenderer.render(threeScene, threeCamera);
        }
        animate();

        // Mouse Parallax Interaction
        container.addEventListener("mousemove", (e) => {
            const rect = container.getBoundingClientRect();
            const x = (e.clientX - rect.left) / width - 0.5;
            const y = (e.clientY - rect.top) / height - 0.5;
            if (threePlantMesh) {
                threePlantMesh.rotation.y = x * 2;
                threePlantMesh.rotation.x = y * 2;
            }
        });
    } catch (e) {
        console.warn("Three.js canvas notice:", e);
    }
}

// ==========================================================================
// View Switcher & Sidebar Navigation
// ==========================================================================
function switchView(viewName) {
    document.querySelectorAll(".view-panel").forEach(p => p.classList.remove("active"));
    document.querySelectorAll(".nav-item").forEach(b => b.classList.remove("active"));

    if (viewName === 'chat') {
        document.getElementById("viewChat").classList.add("active");
        document.getElementById("navChatBtn")?.classList.add("active");
    } else if (viewName === 'knowledge') {
        document.getElementById("viewKnowledge").classList.add("active");
        document.getElementById("navKnowledgeBtn")?.classList.add("active");
    } else if (viewName === 'journal') {
        document.getElementById("viewJournal").classList.add("active");
        document.getElementById("navJournalBtn")?.classList.add("active");
        loadHistory();
    } else if (viewName === 'spray') {
        document.getElementById("viewSpray").classList.add("active");
        document.getElementById("navSprayBtn")?.classList.add("active");
    } else if (viewName === 'stats') {
        document.getElementById("viewStats").classList.add("active");
        document.getElementById("navStatsBtn")?.classList.add("active");
        loadDashboardStats();
    }

    // Close sidebar on mobile upon navigation
    if (window.innerWidth < 768) {
        document.getElementById("sidebar").classList.remove("open");
    }
}

function toggleSidebar() {
    const sidebar = document.getElementById("sidebar");
    if (window.innerWidth < 768) {
        sidebar.classList.toggle("open");
    } else {
        sidebar.classList.toggle("collapsed");
    }
}

function toggleTheme() {
    const body = document.body;
    const btn = document.getElementById("themeToggleBtn");
    if (body.classList.contains("theme-dark")) {
        body.classList.remove("theme-dark");
        body.classList.add("theme-light");
        btn.textContent = "☀️";
    } else {
        body.classList.remove("theme-light");
        body.classList.add("theme-dark");
        btn.textContent = "🌙";
    }
}

function startNewChat() {
    chatHistory = [];
    document.getElementById("messageFeed").innerHTML = "";
    document.getElementById("welcomeHero").style.display = "block";
    removeAttachedImage();
    switchView("chat");
}

// ==========================================================================
// Multimodal Prompt Bar & File Handling
// ==========================================================================
function setupPromptAutoResize() {
    const textarea = document.getElementById("promptInput");
    if (!textarea) return;

    textarea.addEventListener("input", function() {
        this.style.height = "auto";
        this.style.height = Math.min(this.scrollHeight, 120) + "px";
    });
}

function handlePromptKeydown(e) {
    if (e.key === "Enter" && !e.shiftKey) {
        e.preventDefault();
        handleUserSubmit(e);
    }
}

function handleFileChange(e) {
    if (e.target.files && e.target.files.length > 0) {
        attachImageFile(e.target.files[0]);
    }
}

function attachImageFile(file) {
    if (!file.type.startsWith("image/")) {
        alert("Please select a valid image (JPEG, PNG, WEBP).");
        return;
    }

    attachedFile = file;
    const reader = new FileReader();
    reader.onload = (e) => {
        const ribbon = document.getElementById("attachedFilePill");
        const thumb = document.getElementById("attachedThumb");
        const nameLabel = document.getElementById("attachedFileName");

        thumb.src = e.target.result;
        nameLabel.textContent = file.name || "plant_photo.jpg";
        ribbon.style.display = "flex";
    };
    reader.readAsDataURL(file);
}

function removeAttachedImage() {
    attachedFile = null;
    document.getElementById("fileInput").value = "";
    document.getElementById("attachedFilePill").style.display = "none";
}

// Sample Image Generator / Quick Prompts
function loadSampleImage(type) {
    const canvas = document.createElement("canvas");
    canvas.width = 300;
    canvas.height = 300;
    const ctx = canvas.getContext("2d");

    let promptText = "";
    let cropVal = "";

    if (type === 'tomato_blight') {
        ctx.fillStyle = "#2D6A4F";
        ctx.fillRect(0, 0, 300, 300);
        ctx.fillStyle = "#74C69D";
        ctx.beginPath(); ctx.ellipse(150, 150, 120, 130, 0, 0, Math.PI * 2); ctx.fill();

        // Concentric spots
        const spots = [[90, 100], [180, 120], [130, 200], [210, 190]];
        spots.forEach(([x, y]) => {
            ctx.fillStyle = "#D4A373"; ctx.beginPath(); ctx.arc(x, y, 22, 0, Math.PI * 2); ctx.fill();
            ctx.fillStyle = "#6F1D1B"; ctx.beginPath(); ctx.arc(x, y, 14, 0, Math.PI * 2); ctx.fill();
            ctx.fillStyle = "#FFE6A7"; ctx.beginPath(); ctx.arc(x, y, 7, 0, Math.PI * 2); ctx.fill();
        });

        promptText = "Please diagnose these dark concentric ring lesions on my tomato leaves.";
        cropVal = "Tomato";
    } else if (type === 'nitrogen_yellow') {
        ctx.fillStyle = "#E9D8A6";
        ctx.fillRect(0, 0, 300, 300);
        ctx.fillStyle = "#EE9B00";
        ctx.beginPath(); ctx.ellipse(150, 150, 110, 130, 0, 0, Math.PI * 2); ctx.fill();
        ctx.fillStyle = "#94D2BD";
        ctx.beginPath(); ctx.ellipse(150, 100, 50, 70, 0, 0, Math.PI * 2); ctx.fill();

        promptText = "Lower plant canopy is turning pale yellow with stunted growth.";
        cropVal = "";
    } else if (type === 'corn_blight') {
        ctx.fillStyle = "#52B788";
        ctx.fillRect(0, 0, 300, 300);
        ctx.fillStyle = "#DDA15E";
        ctx.fillRect(80, 40, 35, 180);
        ctx.fillRect(160, 90, 30, 150);

        promptText = "Long cigar-shaped tan lesions along corn leaf veins.";
        cropVal = "Corn / Maize";
    }

    canvas.toBlob((blob) => {
        const file = new File([blob], `sample_${type}.jpg`, { type: "image/jpeg" });
        attachImageFile(file);
        document.getElementById("promptInput").value = promptText;
        if (cropVal) document.getElementById("cropSelector").value = cropVal;
    }, "image/jpeg", 0.95);
}

function askPresetQuestion(question) {
    document.getElementById("promptInput").value = question;
    handleUserSubmit(new Event("submit"));
}

// ==========================================================================
// Multimodal Diagnosis & Chat Stream Handler
// ==========================================================================
async function handleUserSubmit(e) {
    if (e) e.preventDefault();

    const input = document.getElementById("promptInput");
    const messageText = (input.value || "").trim();
    const currentFile = attachedFile;

    if (!messageText && !currentFile) {
        alert("Please enter a question or attach a plant photo.");
        return;
    }

    // Hide welcome hero on first message
    document.getElementById("welcomeHero").style.display = "none";

    // 1. Append User Message Bubble
    appendUserMessage(messageText, currentFile);
    input.value = "";
    input.style.height = "auto";

    const cropName = document.getElementById("cropSelector").value;
    const environment = document.getElementById("envSelector").value;
    const growthStage = document.getElementById("stageSelector").value;

    // Reset attachment
    removeAttachedImage();

    // 2. Process Request
    if (currentFile) {
        // Run Full Multimodal Vision Diagnosis via Spring Boot
        await executeVisionDiagnosis(currentFile, cropName, environment, growthStage, messageText);
    } else {
        // Process Follow-up Conversational Agronomy Query
        await executeConversationalQuery(messageText);
    }
}

function appendUserMessage(text, file) {
    const feed = document.getElementById("messageFeed");
    const msgDiv = document.createElement("div");
    msgDiv.className = "chat-msg-user";

    let imgHtml = "";
    if (file) {
        const url = URL.createObjectURL(file);
        imgHtml = `<img src="${url}" class="user-img-preview" alt="Uploaded plant">`;
    }

    msgDiv.innerHTML = `
        ${imgHtml}
        <div class="user-bubble">${escapeHtml(text || "Diagnose this plant image")}</div>
    `;

    feed.appendChild(msgDiv);
    scrollToBottom();
}

async function executeVisionDiagnosis(file, cropName, environment, growthStage, notes) {
    const radar = document.getElementById("scanningRadar");
    radar.style.display = "block";
    scrollToBottom();

    const formData = new FormData();
    formData.append("image", file);
    if (cropName) formData.append("cropName", cropName);
    if (environment) formData.append("environment", environment);
    if (growthStage) formData.append("growthStage", growthStage);
    if (notes) formData.append("notes", notes);

    try {
        const headers = {};
        if (token) headers["Authorization"] = `Bearer ${token}`;

        const res = await fetch(`${API_BASE}/api/diagnose`, {
            method: "POST",
            headers: headers,
            body: formData
        });

        if (!res.ok) {
            let errData = {};
            try {
                errData = await res.json();
            } catch (e) {
                errData = { error: "Diagnosis request failed (" + res.status + ")" };
            }

            if (res.status === 422 || errData.error === "INVALID_PLANT_IMAGE" || errData.isPlant === false) {
                renderNonPlantRejectionMessage(errData);
                return;
            }

            throw new Error(errData.message || errData.error || "Diagnosis failed on server.");
        }

        const data = await res.json();
        if (data.isPlant === false || data.healthStatus === "INVALID_IMAGE") {
            renderNonPlantRejectionMessage(data);
            return;
        }

        currentDiagnosis = data;
        renderAiDiagnosisMessage(data);
        loadDashboardStats();
        loadHistory();
    } catch (err) {
        appendAiTextMessage(`⚠️ **Diagnosis Notice:** ${err.message}`);
    } finally {
        radar.style.display = "none";
        scrollToBottom();
    }
}

function renderNonPlantRejectionMessage(errData) {
    const feed = document.getElementById("messageFeed");
    const msgDiv = document.createElement("div");
    msgDiv.className = "chat-msg-ai";

    const reason = errData.rejectionReason || "NON_PLANT_DETECTED";
    let icon = "🛑";
    let badgeText = "NON-PLANT DETECTED";
    let title = "Image Rejected: No Plant Foliage Detected";

    if (reason === "HUMAN_DETECTED") {
        icon = "👤";
        badgeText = "HUMAN / PORTRAIT DETECTED";
        title = "Image Rejected: Human Photo / Face Detected";
    }

    const message = errData.message || "AgriGuard AI is dedicated strictly to plant pathology and crop health. Please upload a clear photo of an agricultural plant, leaf, stem, or canopy.";

    msgDiv.innerHTML = `
        <div class="ai-avatar" style="background: rgba(239, 68, 68, 0.2); border: 1px solid rgba(239, 68, 68, 0.4); color: #EF4444;">⚠️</div>
        <div class="ai-bubble-container" style="border: 1px solid rgba(239, 68, 68, 0.35); background: rgba(239, 68, 68, 0.06); box-shadow: 0 4px 20px rgba(239, 68, 68, 0.12);">
            <div class="diag-header-row">
                <span class="status-badge" style="background: rgba(239, 68, 68, 0.2); color: #FCA5A5; border: 1px solid rgba(239, 68, 68, 0.4);">
                    <span>${icon}</span> ${badgeText}
                </span>
                <span class="severity-pill" style="background: rgba(239, 68, 68, 0.15); color: #F87171; border: 1px solid rgba(239, 68, 68, 0.3);">
                    VALIDATION REJECTED
                </span>
            </div>

            <h2 class="diag-title" style="color: #F87171; margin-top: 0.4rem;">${title}</h2>

            <div style="padding: 0.85rem; background: rgba(0,0,0,0.3); border-radius: 8px; margin: 0.8rem 0; border-left: 3px solid #EF4444; font-size: 0.9rem; line-height: 1.5; color: var(--text-main);">
                <strong>Validation Notice:</strong> ${escapeHtml(message)}
            </div>

            <div style="font-size: 0.85rem; color: var(--text-muted); line-height: 1.5; margin-bottom: 0.9rem;">
                🌱 <strong>Acceptable Upload Requirements:</strong>
                <ul style="margin: 0.4rem 0 0 1.2rem; padding: 0; list-style-type: disc;">
                    <li>High-resolution photos of plant leaves, stems, flowers, or fruit</li>
                    <li>Whole-crop canopies under natural or greenhouse lighting</li>
                    <li>Close-up photos of foliar lesions, chlorosis, or pest damage</li>
                </ul>
            </div>

            <div style="display: flex; gap: 0.6rem; flex-wrap: wrap;">
                <button type="button" class="btn btn-primary" style="font-size: 0.82rem; padding: 0.4rem 0.85rem;" onclick="document.getElementById('fileInput').click()">
                    📸 Upload Valid Plant Photo
                </button>
                <button type="button" class="btn btn-outline" style="font-size: 0.82rem; padding: 0.4rem 0.85rem;" onclick="loadSampleImage('tomato_blight')">
                    🌱 Load Sample Plant Leaf
                </button>
            </div>
        </div>
    `;

    feed.appendChild(msgDiv);
    scrollToBottom();
}

function renderAiDiagnosisMessage(data) {
    const feed = document.getElementById("messageFeed");
    const msgDiv = document.createElement("div");
    msgDiv.className = "chat-msg-ai";

    const confPct = Math.round((data.confidence || 0.85) * 100);
    const uniqueId = "diag_" + Date.now();

    // Feature Biometrics
    const green = data.featuresSummary?.green_canopy_pct || 0;
    const chlorosis = data.featuresSummary?.chlorosis_pct || 0;
    const necrosis = data.featuresSummary?.necrosis_pct || 0;
    const rust = data.featuresSummary?.rust_pct || 0;

    let statusIcon = "🌿";
    if (data.healthStatus === "DISEASED") statusIcon = "🚨";
    if (data.healthStatus === "STRESSED") statusIcon = "⚠️";

    // Build Immediate Treatments
    let immediateHtml = "";
    if (data.actionPlan?.immediateSteps) {
        data.actionPlan.immediateSteps.forEach(step => {
            immediateHtml += `<div class="advisory-item"><strong>🚨 Immediate Action:</strong> ${step}</div>`;
        });
    }
    if (data.disease?.organicControl) {
        immediateHtml += `<div class="advisory-item" style="border-left-color: #10B981;"><strong>🌿 Organic Bio-Control:</strong> ${data.disease.organicControl}</div>`;
    }
    if (data.disease?.chemicalControl) {
        immediateHtml += `<div class="advisory-item" style="border-left-color: #3B82F6;"><strong>🧪 Chemical Formulation:</strong> ${data.disease.chemicalControl}</div>`;
    }

    // Build Prevention
    let prevHtml = "";
    if (data.disease?.prevention) {
        prevHtml += `<div class="advisory-item">${data.disease.prevention}</div>`;
    }
    if (data.actionPlan?.culturalPractices) {
        data.actionPlan.culturalPractices.forEach(p => {
            prevHtml += `<div class="advisory-item"><strong>Agronomic Tip:</strong> ${p}</div>`;
        });
    }

    // Build Checklist
    let checklistHtml = "";
    if (data.diagnosticChecklist && data.diagnosticChecklist.length > 0) {
        data.diagnosticChecklist.forEach((item, idx) => {
            checklistHtml += `
                <div style="margin-bottom: 0.4rem; display: flex; gap: 0.5rem; align-items: center;">
                    <input type="checkbox" id="${uniqueId}_chk_${idx}" onchange="updateMessageChecklist('${uniqueId}')" style="accent-color: #10B981; width: 16px; height: 16px; cursor: pointer;">
                    <label for="${uniqueId}_chk_${idx}" style="cursor: pointer; font-size: 0.88rem;">${item}</label>
                </div>`;
        });
    }

    msgDiv.innerHTML = `
        <div class="ai-avatar">✨</div>
        <div class="ai-bubble-container">
            <div class="diag-header-row">
                <span class="status-badge status-${data.healthStatus}">
                    <span>${statusIcon}</span> ${data.healthStatus}
                </span>
                <span class="severity-pill sev-${data.severity}">
                    ${data.severity} SEVERITY
                </span>
            </div>

            <h2 class="diag-title">${data.disease?.displayName || data.diseaseKey}</h2>
            <div class="crop-tag">Identified Crop: <strong>${data.predictedCrop || 'General Plant'}</strong></div>

            <!-- Confidence Bar -->
            <div class="confidence-box">
                <div class="conf-header">
                    <span>AI Confidence Gauge</span>
                    <span>${confPct}% Confidence</span>
                </div>
                <div class="progress-track">
                    <div class="progress-fill" style="width: ${confPct}%;"></div>
                </div>
            </div>

            <!-- Biometrics Canopy Grid -->
            <div class="biometrics-grid">
                <div class="bio-pill">
                    <span class="bio-label">Green Canopy</span>
                    <span class="bio-val" style="color: #34D399;">${green}%</span>
                </div>
                <div class="bio-pill">
                    <span class="bio-label">Chlorosis (Yellow)</span>
                    <span class="bio-val" style="color: #FBBF24;">${chlorosis}%</span>
                </div>
                <div class="bio-pill">
                    <span class="bio-label">Necrosis (Lesions)</span>
                    <span class="bio-val" style="color: #F87171;">${necrosis}%</span>
                </div>
                <div class="bio-pill">
                    <span class="bio-label">Rust / Pustules</span>
                    <span class="bio-val" style="color: #FB923C;">${rust}%</span>
                </div>
            </div>

            <!-- Structured Result Tabs -->
            <div class="msg-tabs">
                <button type="button" class="msg-tab-btn active" onclick="switchMsgTab('${uniqueId}', 'findings')">🔍 Pathology</button>
                <button type="button" class="msg-tab-btn" onclick="switchMsgTab('${uniqueId}', 'action')">💊 Prescriptions</button>
                <button type="button" class="msg-tab-btn" onclick="switchMsgTab('${uniqueId}', 'prevention')">🛡️ Cultural Care</button>
                <button type="button" class="msg-tab-btn" onclick="switchMsgTab('${uniqueId}', 'checklist')">📋 Checklist</button>
            </div>

            <!-- Tab 1: Findings -->
            <div id="${uniqueId}_tab_findings" class="msg-tab-content active">
                <p style="margin-bottom: 0.6rem;"><strong>Overview:</strong> ${data.disease?.description || 'Canopy health evaluated.'}</p>
                <p style="margin-bottom: 0.6rem;"><strong>Diagnostic Symptoms:</strong> ${data.disease?.symptoms || 'Visual inspection verified.'}</p>
                <div style="background: rgba(255,255,255,0.03); padding: 0.6rem; border-radius: 6px; font-size: 0.82rem; color: var(--text-muted);">
                    <strong>Model Diagnostic Note:</strong> ${data.modelNotes || 'Analysis complete.'}
                </div>
            </div>

            <!-- Tab 2: Action Plan -->
            <div id="${uniqueId}_tab_action" class="msg-tab-content">
                ${immediateHtml || '<p>No emergency treatments required.</p>'}
            </div>

            <!-- Tab 3: Prevention -->
            <div id="${uniqueId}_tab_prevention" class="msg-tab-content">
                ${prevHtml || '<p>Maintain regular balanced care.</p>'}
            </div>

            <!-- Tab 4: Checklist -->
            <div id="${uniqueId}_tab_checklist" class="msg-tab-content">
                <div id="${uniqueId}_checklist_progress" style="font-size: 0.82rem; color: var(--primary); font-weight: 700; margin-bottom: 0.5rem;">
                    Step Progress: 0 completed
                </div>
                ${checklistHtml}
                <div style="margin-top: 0.85rem; background: rgba(59, 130, 246, 0.1); border: 1px solid rgba(59, 130, 246, 0.3); padding: 0.65rem; border-radius: 6px; font-size: 0.85rem; color: #93C5FD;">
                    <strong>🧑‍🌾 Agronomist Advisory:</strong> ${data.actionPlan?.expertAdvisory || 'Follow standard farm guidelines.'}
                </div>
            </div>

            <!-- Action Toolbar & Quick Follow-ups -->
            <div style="display: flex; gap: 0.5rem; margin-top: 1rem; flex-wrap: wrap;">
                <button type="button" class="btn btn-outline" style="font-size: 0.8rem; padding: 0.35rem 0.75rem;" onclick="exportDiagnosisPDF()">
                    📄 Print / Save Clinical PDF Report
                </button>
            </div>

            <div class="followup-chips-row">
                <span style="font-size: 0.78rem; color: var(--text-dim); display: flex; align-items: center;">Ask Follow-Up:</span>
                <button type="button" class="followup-chip" onclick="askFollowup('What is the exact organic treatment dosage for this condition?')">🧪 Organic dosage?</button>
                <button type="button" class="followup-chip" onclick="askFollowup('Can this disease spread to other adjacent crops?')">⚠️ Spreading risk?</button>
                <button type="button" class="followup-chip" onclick="askFollowup('What is the optimal soil pH and watering frequency for recovery?')">💧 Watering &amp; pH?</button>
            </div>
        </div>
    `;

    feed.appendChild(msgDiv);
    scrollToBottom();
}

function switchMsgTab(uniqueId, tabName) {
    const container = document.getElementById(`${uniqueId}_tab_${tabName}`)?.closest(".ai-bubble-container");
    if (!container) return;

    container.querySelectorAll(".msg-tab-btn").forEach(b => b.classList.remove("active"));
    container.querySelectorAll(".msg-tab-content").forEach(c => c.classList.remove("active"));

    event.target.classList.add("active");
    const target = document.getElementById(`${uniqueId}_tab_${tabName}`);
    if (target) target.classList.add("active");
}

function updateMessageChecklist(uniqueId) {
    const checkboxes = document.querySelectorAll(`[id^="${uniqueId}_chk_"]`);
    const total = checkboxes.length;
    let checked = 0;
    checkboxes.forEach(cb => { if (cb.checked) checked++; });

    const label = document.getElementById(`${uniqueId}_checklist_progress`);
    if (label) {
        label.textContent = `Step Progress: ${checked} of ${total} completed (${Math.round(checked/total*100)}%)`;
    }
}

function askFollowup(question) {
    document.getElementById("promptInput").value = question;
    handleUserSubmit(new Event("submit"));
}

async function executeConversationalQuery(query) {
    const qLower = query.toLowerCase();
    
    // Simulate AI thinking typing delay
    await new Promise(r => setTimeout(r, 600));

    let responseMarkdown = "";

    // Intelligent match against knowledge base
    if (qLower.includes("organic") || qLower.includes("dosage") || qLower.includes("spray")) {
        responseMarkdown = `### 🌿 Organic & Biological Remedy Protocols:
- **Neem Oil Formulation**: Cold-pressed pure neem oil @ 5ml per 1 Liter water with 2 drops organic liquid soap emulsifier. Spray early morning or after sunset to protect beneficial pollinators.
- **Bio-Fungicide (Bacillus subtilis)**: Inoculate foliage weekly (2-3g/L water) to colonize leaf cuticle and competitively suppress spore germination.
- **Copper Octanoate**: For severe bacterial spots or blight, apply low-concentration copper soap drench every 7-10 days. Ensure a 4-hour rain-free dry window.`;
    } else if (qLower.includes("spread") || qLower.includes("contagious") || qLower.includes("other crop")) {
        responseMarkdown = `### ⚠️ Contagion & Cross-Crop Risk Assessment:
- **Solanaceae Family Cross-Infection**: Blights (*Alternaria & Phytophthora*) easily transfer between **Tomatoes, Potatoes, and Eggplants** via windblown rain splash.
- **Vector Inoculum**: Whiteflies and aphids readily transport viral complexes (*Yellow Leaf Curl*) across crops within a 50-meter radius.
- **Action**: Immediately isolate symptomatic plants, sanitize shears in 70% isopropyl alcohol, and install yellow sticky vector traps.`;
    } else if (qLower.includes("water") || qLower.includes("ph") || qLower.includes("soil")) {
        responseMarkdown = `### 💧 Agronomic Soil & Irrigation Parameters:
- **Soil pH Target**: Most crops thrive in **6.0 – 6.8 pH**. If pH exceeds 7.2, iron and manganese become locked up, triggering interveinal chlorosis.
- **Irrigation Protocol**: Transition immediately to root-zone drip lines. Avoid overhead sprinklers which keep leaves wet and create ideal spore germination chambers.
- **Moisture Check**: Allow top 1.5 inches of soil to dry before deep watering to prevent root hypoxia.`;
    } else {
        // Search in allDiseases / allCrops
        const matchDisease = allDiseases.find(d => qLower.includes(d.displayName.toLowerCase()) || qLower.includes(d.diseaseKey.toLowerCase()));
        if (matchDisease) {
            responseMarkdown = `### 📖 Clinical Summary: **${matchDisease.displayName}**
- **Classification**: ${matchDisease.category}
- **Primary Symptoms**: ${matchDisease.symptoms}
- **Prescribed Treatment**: ${matchDisease.treatment}
- **Long-term Prevention**: ${matchDisease.prevention}
- **Organic Bio-Control**: ${matchDisease.organicControl || 'Bacillus subtilis / Trichoderma soil treatment'}`;
        } else {
            responseMarkdown = `### 🌱 AgriGuard AI Agronomic Advisory:
I have analyzed your query regarding **"${escapeHtml(query)}"**.
- **Best Practice**: For precise pathogen diagnosis, attach a clear full-plant photo capturing the canopy and stem.
- **General Rule**: Maintain optimal soil aeration, ensure adequate nitrogen-potassium balance, and avoid working fields while foliage is wet from morning dew.`;
        }
    }

    appendAiTextMessage(responseMarkdown);
}

function appendAiTextMessage(markdown) {
    const feed = document.getElementById("messageFeed");
    const msgDiv = document.createElement("div");
    msgDiv.className = "chat-msg-ai";

    msgDiv.innerHTML = `
        <div class="ai-avatar">✨</div>
        <div class="ai-bubble-container">
            <div style="font-size: 0.95rem; line-height: 1.6;">
                ${formatMarkdown(markdown)}
            </div>
        </div>
    `;

    feed.appendChild(msgDiv);
    scrollToBottom();
}

function scrollToBottom() {
    const area = document.getElementById("chatScrollArea");
    if (area) area.scrollTop = area.scrollHeight;
}

// ==========================================================================
// Knowledge Base & Search
// ==========================================================================
async function loadKnowledgeBase() {
    try {
        const resCrops = await fetch(`${API_BASE}/api/crops`);
        if (resCrops.ok) {
            allCrops = await resCrops.json();
            renderCropsGrid(allCrops);
            populateCropSelect(allCrops);
        }

        const resDiseases = await fetch(`${API_BASE}/api/diseases`);
        if (resDiseases.ok) {
            allDiseases = await resDiseases.json();
        }
    } catch (e) {
        console.error("Knowledge base error:", e);
    }
}

function populateCropSelect(crops) {
    const select = document.getElementById("cropSelector");
    if (!select) return;
    select.innerHTML = '<option value="">🌱 Auto-Detect Crop</option>';
    crops.forEach(c => {
        select.innerHTML += `<option value="${c.name}">${c.iconUrl || '🌱'} ${c.name}</option>`;
    });
}

function renderCropsGrid(crops) {
    const grid = document.getElementById("cropsGrid");
    if (!grid) return;
    grid.innerHTML = "";

    crops.forEach(c => {
        grid.innerHTML += `
            <div class="crop-card" onclick="openCropDetails(${c.id})">
                <div class="crop-card-header">
                    <span class="crop-icon-large">${c.iconUrl || '🌱'}</span>
                    <div>
                        <h4 style="font-size: 1.15rem; margin-bottom: 2px;">${c.name}</h4>
                        <span style="font-size: 0.8rem; color: var(--text-muted); font-style: italic;">${c.scientificName || ''}</span>
                    </div>
                </div>
                <div style="font-size: 0.82rem; color: var(--text-muted); margin-bottom: 0.6rem;">
                    <strong>Family:</strong> ${c.familyName} | <strong>Temp:</strong> ${c.optimalTemp || '20-30°C'}
                </div>
                <p style="font-size: 0.85rem; color: var(--text-muted); line-height: 1.4; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;">
                    ${c.generalCare}
                </p>
                <div style="margin-top: 0.8rem; font-size: 0.82rem; font-weight: 700; color: var(--primary);">
                    Explore Pathology & Care →
                </div>
            </div>`;
    });
}

function filterKnowledge(family) {
    document.querySelectorAll(".btn-filter").forEach(b => b.classList.remove("active"));
    event.target.classList.add("active");

    if (family === 'ALL') {
        renderCropsGrid(allCrops);
    } else if (family === 'ABIOTIC') {
        const abiotic = allDiseases.filter(d => d.category === 'ABIOTIC');
        renderAbioticGrid(abiotic);
    } else {
        const filtered = allCrops.filter(c => c.familyName.toUpperCase().includes(family));
        renderCropsGrid(filtered);
    }
}

function renderAbioticGrid(abioticList) {
    const grid = document.getElementById("cropsGrid");
    if (!grid) return;
    grid.innerHTML = "";
    abioticList.forEach(d => {
        grid.innerHTML += `
            <div class="crop-card" onclick="openDiseaseDetailsByKey('${d.diseaseKey}')" style="border-left: 4px solid #F59E0B;">
                <div class="crop-card-header">
                    <span class="crop-icon-large">☀️</span>
                    <div>
                        <h4 style="font-size: 1.1rem; color: var(--text-main);">${d.displayName}</h4>
                        <span style="font-size: 0.8rem; color: #F59E0B; font-weight: 600;">Abiotic Stress</span>
                    </div>
                </div>
                <p style="font-size: 0.85rem; color: var(--text-muted); line-height: 1.4; margin-top: 0.5rem;">
                    ${d.description}
                </p>
                <div style="margin-top: 0.8rem; font-size: 0.82rem; font-weight: 700; color: #F59E0B;">
                    View Symptoms & Correction →
                </div>
            </div>`;
    });
}

function searchKnowledgeBase(query) {
    const q = (query || "").trim().toLowerCase();
    if (!q) {
        renderCropsGrid(allCrops);
        return;
    }

    const filteredCrops = allCrops.filter(c => 
        c.name.toLowerCase().includes(q) ||
        (c.scientificName && c.scientificName.toLowerCase().includes(q)) ||
        (c.familyName && c.familyName.toLowerCase().includes(q)) ||
        (c.generalCare && c.generalCare.toLowerCase().includes(q))
    );

    const filteredDiseases = allDiseases.filter(d =>
        d.displayName.toLowerCase().includes(q) ||
        (d.description && d.description.toLowerCase().includes(q)) ||
        (d.symptoms && d.symptoms.toLowerCase().includes(q)) ||
        (d.treatment && d.treatment.toLowerCase().includes(q))
    );

    const grid = document.getElementById("cropsGrid");
    grid.innerHTML = "";

    filteredCrops.forEach(c => {
        grid.innerHTML += `
            <div class="crop-card" onclick="openCropDetails(${c.id})">
                <div class="crop-card-header">
                    <span class="crop-icon-large">${c.iconUrl || '🌱'}</span>
                    <div>
                        <h4 style="font-size: 1.15rem; margin-bottom: 2px;">${c.name}</h4>
                        <span style="font-size: 0.8rem; color: var(--text-muted); font-style: italic;">${c.scientificName || ''}</span>
                    </div>
                </div>
                <div style="font-size: 0.82rem; color: var(--text-muted); margin-bottom: 0.6rem;">
                    <strong>Family:</strong> ${c.familyName}
                </div>
                <p style="font-size: 0.85rem; color: var(--text-muted);">${c.generalCare}</p>
            </div>`;
    });

    filteredDiseases.forEach(d => {
        grid.innerHTML += `
            <div class="crop-card" onclick="openDiseaseDetailsByKey('${d.diseaseKey}')" style="border-left: 4px solid #F59E0B;">
                <div class="crop-card-header">
                    <span class="crop-icon-large">🔬</span>
                    <div>
                        <h4 style="font-size: 1.1rem;">${d.displayName}</h4>
                        <span style="font-size: 0.8rem; color: #F59E0B;">Category: ${d.category}</span>
                    </div>
                </div>
                <p style="font-size: 0.85rem; color: var(--text-muted);">${d.symptoms || d.description}</p>
            </div>`;
    });
}

function openCropDetails(cropId) {
    const crop = allCrops.find(c => c.id === cropId);
    if (!crop) return;

    const modalBody = document.getElementById("cropModalContent");
    const cropDiseases = allDiseases.filter(d => d.cropId === cropId);

    let diseaseLinks = "";
    if (cropDiseases.length > 0) {
        diseaseLinks = cropDiseases.map(d => `
            <div style="background: rgba(255,255,255,0.04); border: 1px solid var(--border-color); padding: 0.75rem; border-radius: 8px; margin-bottom: 0.5rem; cursor: pointer;" onclick="openDiseaseDetailsByKey('${d.diseaseKey}')">
                <div style="font-weight: 700; color: var(--text-main);">${d.displayName}</div>
                <div style="font-size: 0.8rem; color: var(--primary);">Category: ${d.category}</div>
            </div>
        `).join("");
    } else {
        diseaseLinks = "<p style='color: var(--text-muted);'>No specific diseases recorded yet.</p>";
    }

    modalBody.innerHTML = `
        <div style="display: flex; gap: 1rem; align-items: center; margin-bottom: 1rem;">
            <span style="font-size: 3rem;">${crop.iconUrl || '🌱'}</span>
            <div>
                <h2 style="font-size: 1.6rem; margin-bottom: 2px;">${crop.name}</h2>
                <div style="color: var(--text-muted); font-style: italic;">${crop.scientificName} (${crop.familyName})</div>
            </div>
        </div>
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 0.75rem; margin-bottom: 1.25rem;">
            <div style="background: rgba(255,255,255,0.04); padding: 0.6rem; border-radius: 8px;"><strong>Optimal Temp:</strong> ${crop.optimalTemp || 'N/A'}</div>
            <div style="background: rgba(255,255,255,0.04); padding: 0.6rem; border-radius: 8px;"><strong>Soil pH:</strong> ${crop.optimalPh || 'N/A'}</div>
        </div>
        <h4 style="margin-bottom: 0.3rem;">General Care & Growing Advice:</h4>
        <p style="color: var(--text-muted); font-size: 0.92rem; margin-bottom: 1rem;">${crop.generalCare}</p>
        <h4 style="margin-bottom: 0.3rem;">Watering Best Practices:</h4>
        <p style="color: var(--text-muted); font-size: 0.92rem; margin-bottom: 1.25rem;">${crop.wateringTips}</p>
        <h4 style="margin-bottom: 0.5rem;">Associated Pathogens & Diseases:</h4>
        ${diseaseLinks}
    `;

    openModal("cropModal");
}

function openDiseaseDetailsByKey(diseaseKey) {
    const d = allDiseases.find(item => item.diseaseKey === diseaseKey);
    if (!d) return;

    const modalBody = document.getElementById("cropModalContent");
    modalBody.innerHTML = `
        <h2 style="font-size: 1.5rem; margin-bottom: 4px;">${d.displayName}</h2>
        <span class="severity-pill sev-MEDIUM" style="margin-bottom: 1rem; display: inline-block;">
            Category: ${d.category}
        </span>
        <h4 style="margin-top: 1rem; margin-bottom: 0.3rem;">Overview:</h4>
        <p style="color: var(--text-muted); font-size: 0.92rem; margin-bottom: 0.75rem;">${d.description}</p>
        <h4 style="margin-bottom: 0.3rem;">Diagnostic Symptoms:</h4>
        <p style="color: var(--text-muted); font-size: 0.92rem; margin-bottom: 0.75rem;">${d.symptoms}</p>
        <h4 style="margin-bottom: 0.3rem;">Recommended Treatment:</h4>
        <p style="color: var(--primary); font-size: 0.92rem; margin-bottom: 0.75rem; background: rgba(16,185,129,0.1); border: 1px solid rgba(16,185,129,0.2); padding: 0.6rem; border-radius: 8px;">${d.treatment}</p>
        <h4 style="margin-bottom: 0.3rem;">Prevention & Cultural Control:</h4>
        <p style="color: var(--text-muted); font-size: 0.92rem;">${d.prevention}</p>
    `;
    openModal("cropModal");
}

// ==========================================================================
// History Journal & CSV Export
// ==========================================================================
async function loadHistory() {
    try {
        const headers = {};
        if (token) headers["Authorization"] = `Bearer ${token}`;

        const res = await fetch(`${API_BASE}/api/diagnoses`, { headers });
        if (res.ok) {
            const history = await res.json();
            renderHistoryTable(history);
            renderSidebarHistory(history);
        }
    } catch (e) {
        console.error("History load error:", e);
    }
}

function renderSidebarHistory(list) {
    const container = document.getElementById("recentScansList");
    const countBadge = document.getElementById("historyCountBadge");
    if (!container) return;

    if (countBadge) countBadge.textContent = `${list ? list.length : 0} scans`;

    if (!list || list.length === 0) {
        container.innerHTML = `<div class="empty-history-notice">No past scans yet</div>`;
        return;
    }

    container.innerHTML = "";
    list.slice(0, 8).forEach(item => {
        const name = item.disease?.displayName || item.diseaseKey;
        const crop = item.predictedCrop || 'Plant';
        container.innerHTML += `
            <div class="recent-scan-item" onclick="viewHistoryInChat(${item.id})">
                <span>🌱</span>
                <span>${crop}: ${name}</span>
            </div>`;
    });
}

function renderHistoryTable(list) {
    const tbody = document.getElementById("historyTableBody");
    if (!tbody) return;
    tbody.innerHTML = "";

    if (!list || list.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" style="text-align: center; color: var(--text-muted); padding: 2rem;">No diagnosis scans yet. Upload your first plant photo!</td></tr>`;
        return;
    }

    list.forEach(item => {
        const confPct = Math.round((item.confidence || 0.85) * 100);

        tbody.innerHTML += `
            <tr>
                <td><img src="${item.imageUrl}" class="history-thumb" alt="Plant"></td>
                <td><strong>${item.predictedCrop || 'General'}</strong></td>
                <td>${item.disease?.displayName || item.diseaseKey}</td>
                <td><span class="status-badge status-${item.healthStatus}" style="font-size: 0.75rem; padding: 0.2rem 0.5rem;">${item.healthStatus}</span></td>
                <td><span class="severity-pill sev-${item.severity}">${item.severity}</span></td>
                <td>${confPct}%</td>
                <td>
                    <button class="btn btn-outline" style="padding: 0.25rem 0.6rem; font-size: 0.78rem;" onclick="viewHistoryInChat(${item.id})">
                        🔍 View in Chat
                    </button>
                </td>
            </tr>`;
    });
}

async function viewHistoryInChat(id) {
    try {
        const res = await fetch(`${API_BASE}/api/diagnoses/${id}`);
        if (res.ok) {
            const data = await res.json();
            currentDiagnosis = data;
            switchView("chat");
            document.getElementById("welcomeHero").style.display = "none";
            renderAiDiagnosisMessage(data);
        }
    } catch (e) {
        alert("Could not load report.");
    }
}

async function exportJournalCSV() {
    try {
        const headers = {};
        if (token) headers["Authorization"] = `Bearer ${token}`;

        const res = await fetch(`${API_BASE}/api/diagnoses`, { headers });
        if (!res.ok) return;

        const history = await res.json();
        if (!history || history.length === 0) {
            alert("No records to export.");
            return;
        }

        const csvRows = [
            ["ID", "Date", "Plant Crop", "Pathogen / Condition", "Health Status", "Severity", "Confidence %", "Environment", "Growth Stage", "User Notes", "Action Recommendation"]
        ];

        history.forEach(item => {
            const dateStr = item.diagnosedAt ? new Date(item.diagnosedAt).toISOString().split('T')[0] : "";
            const confPct = Math.round((item.confidence || 0) * 100);
            const condition = (item.disease?.displayName || item.diseaseKey || "").replace(/"/g, '""');
            const crop = (item.predictedCrop || "General Plant").replace(/"/g, '""');
            const notes = (item.userNotes || "").replace(/"/g, '""');
            const expert = (item.actionPlan?.expertAdvisory || "").replace(/"/g, '""');

            csvRows.push([
                item.id, dateStr, `"${crop}"`, `"${condition}"`, item.healthStatus, item.severity, `${confPct}%`,
                `"${item.environment || ''}"`, `"${item.growthStage || ''}"`, `"${notes}"`, `"${expert}"`
            ]);
        });

        const csvContent = "data:text/csv;charset=utf-8," + csvRows.map(e => e.join(",")).join("\n");
        const encodedUri = encodeURI(csvContent);
        const link = document.createElement("a");
        link.setAttribute("href", encodedUri);
        link.setAttribute("download", `agriguard_journal_${new Date().toISOString().split('T')[0]}.csv`);
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    } catch (err) {
        alert("Failed to export CSV.");
    }
}

// ==========================================================================
// Standalone Spray Safety Calculator
// ==========================================================================
function updateStandaloneSpray() {
    const temp = parseFloat(document.getElementById("calcTemp")?.value || 22);
    const wind = parseFloat(document.getElementById("calcWind")?.value || 6);
    const humidity = parseFloat(document.getElementById("calcHumidity")?.value || 60);
    const rain = document.getElementById("calcRain")?.value || "none";

    const pill = document.getElementById("standaloneSprayPill");
    const adviceBox = document.getElementById("standaloneSprayAdvice");
    if (!pill || !adviceBox) return;

    let isSafe = true;
    let isCaution = false;
    let issues = [];

    if (rain === "high") {
        isSafe = false;
        issues.push("Imminent heavy rain will wash off treatments before absorption.");
    } else if (rain === "medium") {
        isCaution = true;
        issues.push("Moderate rain risk; sprays lack the 2-4 hour rainfast period.");
    }

    if (wind > 20) {
        isSafe = false;
        issues.push(`High wind (${wind} km/h) creates severe chemical spray drift.`);
    } else if (wind > 12) {
        isCaution = true;
        issues.push(`Moderate wind (${wind} km/h); use coarse nozzles and keep boom low.`);
    }

    if (temp > 32) {
        isCaution = true;
        issues.push(`High temperature (${temp}°C) accelerates droplet evaporation.`);
    } else if (temp < 10) {
        isCaution = true;
        issues.push(`Cold temperature (${temp}°C) inhibits foliar uptake.`);
    }

    if (humidity < 35) {
        isCaution = true;
        issues.push(`Low humidity (${humidity}%) increases droplet crystallization.`);
    }

    if (!isSafe) {
        pill.className = "spray-badge badge-danger";
        pill.textContent = "🔴 UNSAFE TO SPRAY";
        adviceBox.innerHTML = `<strong>⚠️ Hazardous Spraying Conditions:</strong> ${issues.join(" ")} Postpone spraying until conditions improve.`;
    } else if (isCaution) {
        pill.className = "spray-badge badge-caution";
        pill.textContent = "🟡 SPRAY WITH CAUTION";
        adviceBox.innerHTML = `<strong>⚠️ Caution Advised:</strong> ${issues.join(" ")} Spray during calm early morning or evening hours.`;
    } else {
        pill.className = "spray-badge badge-safe";
        pill.textContent = "🟢 SAFE TO SPRAY";
        adviceBox.innerHTML = `✅ <strong>Optimal Spray Window:</strong> Gentle breeze (${wind} km/h), favorable temperature (${temp}°C), and adequate humidity (${humidity}%) ensure maximum droplet adherence and zero drift.`;
    }
}

// ==========================================================================
// Farm Health Analytics
// ==========================================================================
async function loadDashboardStats() {
    try {
        const res = await fetch(`${API_BASE}/api/stats`);
        if (res.ok) {
            const stats = await res.json();
            document.getElementById("statTotalScans").textContent = stats.totalScans || 0;
            document.getElementById("statHealthRate").textContent = `${stats.healthRatePercent || 85}%`;
            document.getElementById("statDiseased").textContent = stats.diseasedCount || 0;
            document.getElementById("statCropsMonitored").textContent = "15+";

            // Render Distributions
            const cropDiv = document.getElementById("cropStatsList");
            if (cropDiv && stats.cropDistribution) {
                cropDiv.innerHTML = "";
                for (const [crop, count] of Object.entries(stats.cropDistribution)) {
                    cropDiv.innerHTML += `
                        <div style="display: flex; justify-content: space-between; padding: 0.4rem 0; border-bottom: 1px solid var(--border-color);">
                            <span>${crop}</span>
                            <strong>${count} scans</strong>
                        </div>`;
                }
            }

            const sevDiv = document.getElementById("severityStatsList");
            if (sevDiv && stats.severityDistribution) {
                sevDiv.innerHTML = "";
                for (const [sev, count] of Object.entries(stats.severityDistribution)) {
                    sevDiv.innerHTML += `
                        <div style="display: flex; justify-content: space-between; padding: 0.4rem 0; border-bottom: 1px solid var(--border-color);">
                            <span>${sev} Severity</span>
                            <strong>${count} cases</strong>
                        </div>`;
                }
            }
        }
    } catch (e) {
        console.error("Stats load error:", e);
    }
}

// ==========================================================================
// Authentication
// ==========================================================================
function setupAuth() {
    if (token) {
        fetchCurrentUser();
    } else {
        updateAuthUI(null);
    }
}

async function fetchCurrentUser() {
    try {
        const res = await fetch(`${API_BASE}/api/auth/me`, {
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (res.ok) {
            currentUser = await res.json();
            updateAuthUI(currentUser);
        } else {
            logout();
        }
    } catch (e) {
        console.error("Auth error:", e);
    }
}

function updateAuthUI(user) {
    const nameLabel = document.getElementById("userNameLabel");
    const roleLabel = document.getElementById("userRoleLabel");
    const avatar = document.getElementById("userAvatar");

    if (user) {
        if (nameLabel) nameLabel.textContent = user.fullName || user.username;
        if (roleLabel) roleLabel.textContent = `${user.role || 'Farmer'} (Click to Logout)`;
        if (avatar) avatar.textContent = "👨‍🌾";
        document.getElementById("userProfileCard").onclick = logout;
    } else {
        if (nameLabel) nameLabel.textContent = "Guest Mode";
        if (roleLabel) roleLabel.textContent = "Click to Sign In";
        if (avatar) avatar.textContent = "👤";
        document.getElementById("userProfileCard").onclick = () => openModal("authModal");
    }
}

function logout() {
    token = null;
    currentUser = null;
    localStorage.removeItem("agriguard_token");
    updateAuthUI(null);
    loadHistory();
}

async function handleLogin(e) {
    e.preventDefault();
    const username = document.getElementById("loginUsername").value;
    const password = document.getElementById("loginPassword").value;
    const errorEl = document.getElementById("loginError");
    errorEl.textContent = "";

    try {
        const res = await fetch(`${API_BASE}/api/auth/login`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username, password })
        });
        const data = await res.json();
        if (res.ok && data.token) {
            token = data.token;
            localStorage.setItem("agriguard_token", token);
            currentUser = { username: data.username, fullName: data.fullName, role: data.role, id: data.userId };
            updateAuthUI(currentUser);
            closeModal("authModal");
            loadHistory();
        } else {
            errorEl.textContent = data.error || "Login failed.";
        }
    } catch (err) {
        errorEl.textContent = "Network error connecting to server.";
    }
}

async function handleRegister(e) {
    e.preventDefault();
    const username = document.getElementById("regUsername").value;
    const email = document.getElementById("regEmail").value;
    const fullName = document.getElementById("regFullName").value;
    const password = document.getElementById("regPassword").value;
    const errorEl = document.getElementById("regError");
    errorEl.textContent = "";

    try {
        const res = await fetch(`${API_BASE}/api/auth/register`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username, email, fullName, password })
        });
        const data = await res.json();
        if (res.ok && data.token) {
            token = data.token;
            localStorage.setItem("agriguard_token", token);
            currentUser = { username: data.username, fullName: data.fullName, role: data.role, id: data.userId };
            updateAuthUI(currentUser);
            closeModal("authModal");
            loadHistory();
        } else {
            errorEl.textContent = data.error || "Registration failed.";
        }
    } catch (err) {
        errorEl.textContent = "Network error connecting to server.";
    }
}

function switchAuthTab(tab) {
    const loginForm = document.getElementById("loginFormContainer");
    const regForm = document.getElementById("regFormContainer");
    const tabLogin = document.getElementById("tabLoginBtn");
    const tabReg = document.getElementById("tabRegBtn");

    if (tab === 'login') {
        loginForm.style.display = 'block';
        regForm.style.display = 'none';
        tabLogin.classList.add('active');
        tabReg.classList.remove('active');
    } else {
        loginForm.style.display = 'none';
        regForm.style.display = 'block';
        tabLogin.classList.remove('active');
        tabReg.classList.add('active');
    }
}

// ==========================================================================
// Live Webcam
// ==========================================================================
async function openWebcam() {
    openModal("webcamModal");
    const video = document.getElementById("webcamVideo");
    try {
        webcamStream = await navigator.mediaDevices.getUserMedia({ video: { facingMode: "environment" } });
        video.srcObject = webcamStream;
    } catch (err) {
        alert("Camera permission denied or camera unavailable.");
        closeModal("webcamModal");
    }
}

function captureWebcamPhoto() {
    const video = document.getElementById("webcamVideo");
    const canvas = document.createElement("canvas");
    canvas.width = video.videoWidth || 640;
    canvas.height = video.videoHeight || 480;
    const ctx = canvas.getContext("2d");
    ctx.drawImage(video, 0, 0, canvas.width, canvas.height);

    canvas.toBlob((blob) => {
        const file = new File([blob], `plant_camera_${Date.now()}.jpg`, { type: "image/jpeg" });
        attachImageFile(file);
        closeWebcam();
    }, "image/jpeg", 0.92);
}

function closeWebcam() {
    if (webcamStream) {
        webcamStream.getTracks().forEach(track => track.stop());
        webcamStream = null;
    }
    closeModal("webcamModal");
}

// ==========================================================================
// Utilities
// ==========================================================================
function openModal(id) {
    const el = document.getElementById(id);
    if (el) el.style.display = "flex";
}

function closeModal(id) {
    const el = document.getElementById(id);
    if (el) el.style.display = "none";
}

function exportDiagnosisPDF() {
    if (!currentDiagnosis) {
        alert("No active diagnosis to export.");
        return;
    }
    window.print();
}

function escapeHtml(str) {
    if (!str) return "";
    return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;");
}

function formatMarkdown(text) {
    if (!text) return "";
    return text
        .replace(/^### (.*$)/gim, '<h3 style="margin-top:0.5rem; margin-bottom:0.3rem; color:var(--text-main); font-size:1.1rem;">$1</h3>')
        .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
        .replace(/^- (.*$)/gim, '<li style="margin-left:1.25rem; margin-bottom:0.25rem;">$1</li>');
}
