package com.kec.eval.model;

/**
 * Model representing class performance and result summary.
 */
public class ClassSummary {
    private String department;
    private int semester;
    private int totalStudents;
    private int totalEnrollments;
    private int passedEnrollments;
    private int raEnrollments;
    private int saEnrollments;
    private int abEnrollments;
    private double passPercentage;
    private double averageSgpa;

    public ClassSummary() {
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    public int getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(int totalStudents) {
        this.totalStudents = totalStudents;
    }

    public int getTotalEnrollments() {
        return totalEnrollments;
    }

    public void setTotalEnrollments(int totalEnrollments) {
        this.totalEnrollments = totalEnrollments;
    }

    public int getPassedEnrollments() {
        return passedEnrollments;
    }

    public void setPassedEnrollments(int passedEnrollments) {
        this.passedEnrollments = passedEnrollments;
    }

    public int getRaEnrollments() {
        return raEnrollments;
    }

    public void setRaEnrollments(int raEnrollments) {
        this.raEnrollments = raEnrollments;
    }

    public int getSaEnrollments() {
        return saEnrollments;
    }

    public void setSaEnrollments(int saEnrollments) {
        this.saEnrollments = saEnrollments;
    }

    public int getAbEnrollments() {
        return abEnrollments;
    }

    public void setAbEnrollments(int abEnrollments) {
        this.abEnrollments = abEnrollments;
    }

    public double getPassPercentage() {
        return passPercentage;
    }

    public void setPassPercentage(double passPercentage) {
        this.passPercentage = passPercentage;
    }

    public double getAverageSgpa() {
        return averageSgpa;
    }

    public void setAverageSgpa(double averageSgpa) {
        this.averageSgpa = averageSgpa;
    }
}
