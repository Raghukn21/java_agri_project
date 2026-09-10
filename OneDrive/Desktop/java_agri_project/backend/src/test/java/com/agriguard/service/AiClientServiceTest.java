package com.agriguard.service;

import com.agriguard.dto.AiPredictionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class AiClientServiceTest {

    private AiClientService aiClientService;

    @BeforeEach
    void setUp() {
        aiClientService = new AiClientService();
    }

    private byte[] createTestImage(Color color) throws IOException {
        BufferedImage image = new BufferedImage(120, 120, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(color);
        g.fillRect(0, 0, 120, 120);
        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        return baos.toByteArray();
    }

    @Test
    @DisplayName("Should detect nitrogen deficiency via Java engine when foliage is yellow and notes specify lower leaf chlorosis")
    void testNativeInferenceNitrogenDeficiency() throws IOException {
        byte[] imgBytes = createTestImage(new Color(220, 220, 60)); // Yellowish
        MockMultipartFile file = new MockMultipartFile("image", "test_yellow.jpg", "image/jpeg", imgBytes);

        AiPredictionResult result = aiClientService.predict(file, "Tomato", "Open Field", "Vegetative", "Lower leaves turning yellow");

        assertNotNull(result);
        assertEquals("nitrogen_deficiency_general", result.getDiseaseKey());
        assertEquals("STRESSED", result.getHealthStatus());
        assertEquals("ABIOTIC", result.getCategory());
        assertTrue(result.getConfidence() >= 0.70);
        assertNotNull(result.getFeaturesSummary());
    }

    @Test
    @DisplayName("Should detect healthy plant via Java engine when foliage is predominantly green")
    void testNativeInferenceHealthyPlant() throws IOException {
        byte[] imgBytes = createTestImage(new Color(34, 139, 34)); // Forest green
        MockMultipartFile file = new MockMultipartFile("image", "test_green.jpg", "image/jpeg", imgBytes);

        AiPredictionResult result = aiClientService.predict(file, "Potato", "Open Field", "Vegetative", "Vibrant green foliage, no spots");

        assertNotNull(result);
        assertTrue(result.getDiseaseKey().contains("healthy"));
        assertEquals("HEALTHY", result.getHealthStatus());
        assertEquals("LOW", result.getSeverity());
    }

    @Test
    @DisplayName("Should detect early blight when notes specify concentric rings or blight")
    void testNativeInferenceEarlyBlight() throws IOException {
        byte[] imgBytes = createTestImage(new Color(100, 120, 50));
        MockMultipartFile file = new MockMultipartFile("image", "test_blight.jpg", "image/jpeg", imgBytes);

        AiPredictionResult result = aiClientService.predict(file, "Tomato", "Greenhouse", "Fruiting", "Concentric dark rings and blight spots on lower foliage");

        assertNotNull(result);
        assertTrue(result.getDiseaseKey().contains("blight") || result.getDiseaseKey().contains("spot"));
        assertEquals("DISEASED", result.getHealthStatus());
    }

    @Test
    @DisplayName("Should detect corn common rust when symptoms describe cinnamon pustules")
    void testNativeInferenceCornRust() throws IOException {
        byte[] imgBytes = createTestImage(new Color(180, 80, 30)); // Rust tone
        MockMultipartFile file = new MockMultipartFile("image", "test_rust.jpg", "image/jpeg", imgBytes);

        AiPredictionResult result = aiClientService.predict(file, "Corn / Maize", "Open Field", "Flowering", "Cinnamon-brown rust pustules on leaf surface");

        assertNotNull(result);
        assertEquals("corn_common_rust", result.getDiseaseKey());
        assertEquals("DISEASED", result.getHealthStatus());
        assertEquals("FUNGAL", result.getCategory());
    }

    @Test
    @DisplayName("Should detect iron chlorosis when notes specify interveinal yellowing on new leaves")
    void testNativeInferenceIronChlorosis() throws IOException {
        byte[] imgBytes = createTestImage(new Color(230, 230, 80));
        MockMultipartFile file = new MockMultipartFile("image", "test_iron.jpg", "image/jpeg", imgBytes);

        AiPredictionResult result = aiClientService.predict(file, "Citrus", "Container / Pot", "Vegetative", "Interveinal yellowing on young new growth");

        assertNotNull(result);
        assertEquals("iron_chlorosis_general", result.getDiseaseKey());
        assertEquals("STRESSED", result.getHealthStatus());
        assertEquals("ABIOTIC", result.getCategory());
    }

    @Test
    @DisplayName("Should reject image when predominant color profile is human skin / face")
    void testRejectHumanSkinImage() throws IOException {
        // Typical human skin tone (RGB: 215, 160, 125 -> high R, moderate G, low B, (R-G) >= 12, Cb/Cr in skin window)
        byte[] imgBytes = createTestImage(new Color(215, 160, 125));
        MockMultipartFile file = new MockMultipartFile("image", "human_portrait.jpg", "image/jpeg", imgBytes);

        AiPredictionResult result = aiClientService.predict(file, "Tomato", "Open Field", "Vegetative", "Person selfie");

        assertNotNull(result);
        assertFalse(result.isPlant());
        assertEquals("HUMAN_DETECTED", result.getRejectionReason());
        assertEquals("INVALID_IMAGE", result.getHealthStatus());
        assertNotNull(result.getRejectionMessage());
        assertTrue(result.getRejectionMessage().toLowerCase().contains("human"));
    }

    @Test
    @DisplayName("Should reject non-plant objects with insufficient botanical canopy")
    void testRejectNonPlantObject() throws IOException {
        // Synthetic deep blue / non-plant metallic color
        byte[] imgBytes = createTestImage(new Color(30, 50, 200));
        MockMultipartFile file = new MockMultipartFile("image", "car.jpg", "image/jpeg", imgBytes);

        AiPredictionResult result = aiClientService.predict(file, "Tomato", "Open Field", "Vegetative", "Photo of a vehicle");

        assertNotNull(result);
        assertFalse(result.isPlant());
        assertEquals("NON_PLANT_DETECTED", result.getRejectionReason());
        assertEquals("INVALID_IMAGE", result.getHealthStatus());
        assertNotNull(result.getRejectionMessage());
    }
}
