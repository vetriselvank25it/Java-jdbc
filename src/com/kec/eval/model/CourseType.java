package com.kec.eval.model;

/**
 * Kongu Engineering College (Autonomous) - Regulation 2024 (R2024)
 * Course Types and Weightage Specifications
 */
public enum CourseType {
    THEORY("Theory Course", 40.0, 60.0, 45.0, 50.0),
    PRACTICAL("Practical / Laboratory Course", 60.0, 40.0, 50.0, 50.0),
    EMBEDDED("Embedded / Integrated Theory + Lab Course", 50.0, 50.0, 45.0, 50.0);

    private final String displayName;
    private final double ciaWeightage;       // % weightage for Continuous Internal Assessment
    private final double eseWeightage;       // % weightage for End Semester Examination
    private final double eseMinThresholdPct; // Minimum % needed in ESE to qualify (on 100 scale)
    private final double overallPassMinimum;  // Minimum overall aggregate marks out of 100 to pass

    CourseType(String displayName, double ciaWeightage, double eseWeightage, double eseMinThresholdPct, double overallPassMinimum) {
        this.displayName = displayName;
        this.ciaWeightage = ciaWeightage;
        this.eseWeightage = eseWeightage;
        this.eseMinThresholdPct = eseMinThresholdPct;
        this.overallPassMinimum = overallPassMinimum;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getCiaWeightage() {
        return ciaWeightage;
    }

    public double getEseWeightage() {
        return eseWeightage;
    }

    public double getEseMinThresholdPct() {
        return eseMinThresholdPct;
    }

    /**
     * Minimum scaled mark required in ESE component
     * e.g., Theory: 45% of 60 = 27.0
     * Practical: 50% of 40 = 20.0
     * Embedded: 45% of 50 = 22.5
     */
    public double getEseScaledMinimum() {
        return (eseMinThresholdPct / 100.0) * eseWeightage;
    }

    public double getOverallPassMinimum() {
        return overallPassMinimum;
    }
}
