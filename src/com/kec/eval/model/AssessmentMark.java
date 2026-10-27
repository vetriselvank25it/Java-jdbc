package com.kec.eval.model;

/**
 * Model representing phased assessment marks and evaluation grades under KEC R2024.
 */
public class AssessmentMark {
    private int markId;
    private int enrollmentId;
    private Double ciaTest1;            // Phase 1 (0-100)
    private Double ciaTest2;            // Phase 1 (0-100)
    private Double assignmentQuizLab;   // Phase 2 (0-100)
    private Double eseMarks;            // Phase 3 (0-100)
    private boolean isEseAbsent;        // Phase 3 (Absent in ESE)
    private Double computedCia;         // Weighted CIA component (out of 40/60/50)
    private Double computedEse;         // Weighted ESE component (out of 60/40/50)
    private Double totalMarks;          // Total marks (0-100)
    private String letterGrade;         // O, A+, A, B+, B, RA, SA, AB
    private Integer gradePoint;         // 10, 9, 8, 7, 6, 0
    private String resultStatus;        // PASS, RA, SA, AB, IN_PROGRESS

    public AssessmentMark() {
    }

    public AssessmentMark(int enrollmentId) {
        this.enrollmentId = enrollmentId;
        this.resultStatus = "IN_PROGRESS";
        this.isEseAbsent = false;
    }

    public int getMarkId() {
        return markId;
    }

    public void setMarkId(int markId) {
        this.markId = markId;
    }

    public int getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(int enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public Double getCiaTest1() {
        return ciaTest1;
    }

    public void setCiaTest1(Double ciaTest1) {
        this.ciaTest1 = ciaTest1;
    }

    public Double getCiaTest2() {
        return ciaTest2;
    }

    public void setCiaTest2(Double ciaTest2) {
        this.ciaTest2 = ciaTest2;
    }

    public Double getAssignmentQuizLab() {
        return assignmentQuizLab;
    }

    public void setAssignmentQuizLab(Double assignmentQuizLab) {
        this.assignmentQuizLab = assignmentQuizLab;
    }

    public Double getEseMarks() {
        return eseMarks;
    }

    public void setEseMarks(Double eseMarks) {
        this.eseMarks = eseMarks;
    }

    public boolean isEseAbsent() {
        return isEseAbsent;
    }

    public void setEseAbsent(boolean eseAbsent) {
        isEseAbsent = eseAbsent;
    }

    public Double getComputedCia() {
        return computedCia;
    }

    public void setComputedCia(Double computedCia) {
        this.computedCia = computedCia;
    }

    public Double getComputedEse() {
        return computedEse;
    }

    public void setComputedEse(Double computedEse) {
        this.computedEse = computedEse;
    }

    public Double getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(Double totalMarks) {
        this.totalMarks = totalMarks;
    }

    public String getLetterGrade() {
        return letterGrade;
    }

    public void setLetterGrade(String letterGrade) {
        this.letterGrade = letterGrade;
    }

    public Integer getGradePoint() {
        return gradePoint;
    }

    public void setGradePoint(Integer gradePoint) {
        this.gradePoint = gradePoint;
    }

    public String getResultStatus() {
        return resultStatus;
    }

    public void setResultStatus(String resultStatus) {
        this.resultStatus = resultStatus;
    }

    public boolean isPhase1Complete() {
        return ciaTest1 != null && ciaTest2 != null;
    }

    public boolean isPhase2Complete() {
        return assignmentQuizLab != null;
    }

    public boolean isPhase3Complete() {
        return eseMarks != null || isEseAbsent;
    }
}
