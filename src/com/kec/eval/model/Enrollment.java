package com.kec.eval.model;

/**
 * Model representing student course enrollment with continuous attendance tracking.
 */
public class Enrollment {
    private int enrollmentId;
    private String rollNo;
    private String courseCode;
    private int semester;
    private int classesHeld;
    private int classesAttended;
    private boolean medicalCondonation;
    private String condonationReason;

    // Joined metadata for convenient reporting
    private String studentName;
    private String studentDepartment;
    private String courseTitle;
    private CourseType courseType;
    private double credits;

    public Enrollment() {
    }

    public Enrollment(String rollNo, String courseCode, int semester, int classesHeld, int classesAttended) {
        this.rollNo = rollNo;
        this.courseCode = courseCode;
        this.semester = semester;
        this.classesHeld = classesHeld;
        this.classesAttended = classesAttended;
        this.medicalCondonation = false;
    }

    public int getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(int enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public String getRollNo() {
        return rollNo;
    }

    public void setRollNo(String rollNo) {
        this.rollNo = rollNo;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
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

    public boolean isMedicalCondonation() {
        return medicalCondonation;
    }

    public void setMedicalCondonation(boolean medicalCondonation) {
        this.medicalCondonation = medicalCondonation;
    }

    public String getCondonationReason() {
        return condonationReason;
    }

    public void setCondonationReason(String condonationReason) {
        this.condonationReason = condonationReason;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentDepartment() {
        return studentDepartment;
    }

    public void setStudentDepartment(String studentDepartment) {
        this.studentDepartment = studentDepartment;
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

    /**
     * Calculates attendance percentage
     */
    public double getAttendancePercentage() {
        if (classesHeld <= 0) return 100.0;
        return (classesAttended * 100.0) / classesHeld;
    }

    /**
     * Enforces KEC R2024 Attendance Eligibility:
     * >= 75%: Eligible for ESE
     * 65% to 74.9%: Conditionally eligible (Requires Medical Condonation approval)
     * < 65%: Shortage of Attendance (SA), Barred from ESE
     */
    public boolean isEligibleForESE() {
        double pct = getAttendancePercentage();
        if (pct >= 75.0) {
            return true;
        } else if (pct >= 65.0) {
            return medicalCondonation;
        } else {
            return false;
        }
    }

    public String getAttendanceStatus() {
        double pct = getAttendancePercentage();
        if (pct >= 75.0) {
            return "Eligible";
        } else if (pct >= 65.0) {
            return medicalCondonation ? "Conditionally Eligible (Condoned)" : "Condonation Required";
        } else {
            return "Barred (<65% SA)";
        }
    }
}
