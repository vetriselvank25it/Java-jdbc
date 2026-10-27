package com.kec.eval.model;

/**
 * Encapsulates the detailed result of a single course on a student's grade sheet.
 */
public class CourseResult {
    private String courseCode;
    private String courseTitle;
    private CourseType courseType;
    private double credits;
    private int classesHeld;
    private int classesAttended;
    private double attendancePercentage;
    private boolean medicalCondonation;
    private Double rawCia1;
    private Double rawCia2;
    private Double rawAssignment;
    private Double scaledCia;
    private Double rawEse;
    private Double scaledEse;
    private boolean eseAbsent;
    private Double totalMarks;
    private String letterGrade;
    private int gradePoint;
    private String resultStatus;

    public CourseResult() {
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }

    public CourseType getCourseType() {
        return courseType;
    }

    public void setCourseType(CourseType courseType) {
        this.courseType = courseType;
    }

    public double getCredits() {
        return credits;
    }

    public void setCredits(double credits) {
        this.credits = credits;
    }

    public int getClassesHeld() {
        return classesHeld;
    }

    public void setClassesHeld(int classesHeld) {
        this.classesHeld = classesHeld;
    }

    public int getClassesAttended() {
        return classesAttended;
    }

    public void setClassesAttended(int classesAttended) {
        this.classesAttended = classesAttended;
    }

    public double getAttendancePercentage() {
        return attendancePercentage;
    }

    public void setAttendancePercentage(double attendancePercentage) {
        this.attendancePercentage = attendancePercentage;
    }

    public boolean isMedicalCondonation() {
        return medicalCondonation;
    }

    public void setMedicalCondonation(boolean medicalCondonation) {
        this.medicalCondonation = medicalCondonation;
    }

    public Double getRawCia1() {
        return rawCia1;
    }

    public void setRawCia1(Double rawCia1) {
        this.rawCia1 = rawCia1;
    }

    public Double getRawCia2() {
        return rawCia2;
    }

    public void setRawCia2(Double rawCia2) {
        this.rawCia2 = rawCia2;
    }

    public Double getRawAssignment() {
        return rawAssignment;
    }

    public void setRawAssignment(Double rawAssignment) {
        this.rawAssignment = rawAssignment;
    }

    public Double getScaledCia() {
        return scaledCia;
    }

    public void setScaledCia(Double scaledCia) {
        this.scaledCia = scaledCia;
    }

    public Double getRawEse() {
        return rawEse;
    }

    public void setRawEse(Double rawEse) {
        this.rawEse = rawEse;
    }

    public Double getScaledEse() {
        return scaledEse;
    }

    public void setScaledEse(Double scaledEse) {
        this.scaledEse = scaledEse;
    }

    public boolean isEseAbsent() {
        return eseAbsent;
    }

    public void setEseAbsent(boolean eseAbsent) {
        this.eseAbsent = eseAbsent;
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

    public int getGradePoint() {
        return gradePoint;
    }

    public void setGradePoint(int gradePoint) {
        this.gradePoint = gradePoint;
    }

    public String getResultStatus() {
        return resultStatus;
    }

    public void setResultStatus(String resultStatus) {
        this.resultStatus = resultStatus;
    }

    public boolean isPassed() {
        return "PASS".equalsIgnoreCase(resultStatus);
    }
}
