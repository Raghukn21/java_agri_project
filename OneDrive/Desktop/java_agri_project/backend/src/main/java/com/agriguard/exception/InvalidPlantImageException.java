package com.agriguard.exception;

public class InvalidPlantImageException extends RuntimeException {

    private final String rejectionReason;
    private final Double plantProbability;

    public InvalidPlantImageException(String message, String rejectionReason, Double plantProbability) {
        super(message);
        this.rejectionReason = rejectionReason;
        this.plantProbability = plantProbability;
    }

    public InvalidPlantImageException(String message, String rejectionReason) {
        this(message, rejectionReason, 0.0);
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public Double getPlantProbability() {
        return plantProbability;
    }
}
