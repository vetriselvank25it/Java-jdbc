package com.kec.eval.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Model representing a student's formal semester academic performance report,
 * including individual course marks, SGPA, and equivalent percentage under KEC R2024.
 */
public class StudentSemesterResult {
    private Student student;
    private int semester;
    private List<CourseResult> courseResults = new ArrayList<>();
    private double totalEnrolledCredits;
    private double totalEarnedCredits;
    private double totalWeightedPoints;
    private double sgpa;
    private double equivalentPercentage;
    private String semesterStatus;

    public StudentSemesterResult() {
    }

    public StudentSemesterResult(Student student, int semester) {
        this.student = student;
        this.semester = semester;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    public List<CourseResult> getCourseResults() {
        return courseResults;
    }

    public void setCourseResults(List<CourseResult> courseResults) {
        this.courseResults = courseResults;
    }

    public void addCourseResult(CourseResult result) {
        this.courseResults.add(result);
    }

    public double getTotalEnrolledCredits() {
        return totalEnrolledCredits;
    }

    public void setTotalEnrolledCredits(double totalEnrolledCredits) {
        this.totalEnrolledCredits = totalEnrolledCredits;
    }

    public double getTotalEarnedCredits() {
        return totalEarnedCredits;
    }

    public void setTotalEarnedCredits(double totalEarnedCredits) {
        this.totalEarnedCredits = totalEarnedCredits;
    }

    public double getTotalWeightedPoints() {
        return totalWeightedPoints;
    }

    public void setTotalWeightedPoints(double totalWeightedPoints) {
        this.totalWeightedPoints = totalWeightedPoints;
    }

    public double getSgpa() {
        return sgpa;
    }

    public void setSgpa(double sgpa) {
        this.sgpa = sgpa;
    }

    public double getEquivalentPercentage() {
        return equivalentPercentage;
    }

    public void setEquivalentPercentage(double equivalentPercentage) {
        this.equivalentPercentage = equivalentPercentage;
    }

    public String getSemesterStatus() {
        return semesterStatus;
    }

    public void setSemesterStatus(String semesterStatus) {
        this.semesterStatus = semesterStatus;
    }

    /**
     * Computes SGPA and Equivalent Percentage according to KEC R2024:
     * SGPA = Sum(Course Credits * Grade Points Earned) / Sum(Total Enrolled Credits)
     * Equivalent Percentage = SGPA * 10
     */
    public void computeSummary() {
        double enrolledCreditsSum = 0.0;
        double earnedCreditsSum = 0.0;
        double weightedPointsSum = 0.0;
        boolean hasArrear = false;
        boolean hasSA = false;

        for (CourseResult cr : courseResults) {
            enrolledCreditsSum += cr.getCredits();
            weightedPointsSum += (cr.getCredits() * cr.getGradePoint());

            if (cr.isPassed()) {
                earnedCreditsSum += cr.getCredits();
            } else {
                if ("SA".equalsIgnoreCase(cr.getLetterGrade())) {
                    hasSA = true;
                } else {
                    hasArrear = true;
                }
            }
        }

        this.totalEnrolledCredits = enrolledCreditsSum;
        this.totalEarnedCredits = earnedCreditsSum;
        this.totalWeightedPoints = weightedPointsSum;

        if (enrolledCreditsSum > 0) {
            this.sgpa = Math.round((weightedPointsSum / enrolledCreditsSum) * 100.0) / 100.0;
        } else {
            this.sgpa = 0.0;
        }

        this.equivalentPercentage = Math.round((this.sgpa * 10.0) * 100.0) / 100.0;

        if (hasSA) {
            this.semesterStatus = "BARRED (SHORTAGE OF ATTENDANCE)";
        } else if (hasArrear) {
            this.semesterStatus = "RE-APPEARANCE (ARREAR)";
        } else if (courseResults.isEmpty()) {
            this.semesterStatus = "NO COURSES ENROLLED";
        } else {
            this.semesterStatus = "ALL COURSES PASSED";
        }
    }
}
