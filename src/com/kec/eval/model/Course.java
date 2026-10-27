package com.kec.eval.model;

/**
 * Model representing a course under KEC R2024 regulations.
 */
public class Course {
    private String courseCode;
    private String title;
    private double credits;
    private CourseType courseType;
    private String department;
    private int semester;

    public Course() {
    }

    public Course(String courseCode, String title, double credits, CourseType courseType, String department, int semester) {
        this.courseCode = courseCode;
        this.title = title;
        this.credits = credits;
        this.courseType = courseType;
        this.department = department;
        this.semester = semester;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getCredits() {
        return credits;
    }

    public void setCredits(double credits) {
        this.credits = credits;
    }

    public CourseType getCourseType() {
        return courseType;
    }

    public void setCourseType(CourseType courseType) {
        this.courseType = courseType;
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

    @Override
    public String toString() {
        return String.format("[%s] %-35s | Credits: %.1f | Type: %-9s | Sem: %d",
                courseCode, title, credits, courseType.name(), semester);
    }
}
