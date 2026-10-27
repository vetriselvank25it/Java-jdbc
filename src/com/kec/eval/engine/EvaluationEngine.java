package com.kec.eval.engine;

import com.kec.eval.model.AssessmentMark;
import com.kec.eval.model.CourseType;
import com.kec.eval.model.Enrollment;

/**
 * Kongu Engineering College (Autonomous) - Regulation 2024 (R2024)
 * Comprehensive Evaluation & Weightage Engine.
 * 
 * Enforces:
 * 1. Attendance eligibility filtering & medical condonation verification.
 * 2. Course-type specific weightage (THEORY, PRACTICAL, EMBEDDED).
 * 3. ESE minimum qualifying thresholds.
 * 4. Overall aggregate pass minimums.
 * 5. KEC 10-Point Absolute Grading System.
 * 6. SGPA and Percentage conversion.
 */
public class EvaluationEngine {

    /**
     * Evaluates attendance and calculates final marks and grades for a course enrollment.
     * 
     * @param enrollment Current student enrollment with attendance details
     * @param courseType Type of course (THEORY, PRACTICAL, EMBEDDED)
     * @param mark       Assessment mark record containing phased entries
     * @return updated AssessmentMark with computed components, letter grade, grade points, and status
     */
    public static AssessmentMark evaluateCourse(Enrollment enrollment, CourseType courseType, AssessmentMark mark) {
        if (enrollment == null || courseType == null || mark == null) {
            return mark;
        }

        double attendancePct = enrollment.getAttendancePercentage();

        // 1. Attendance Eligibility Check
        if (attendancePct < 65.0) {
            // Less than 65%: Shortage of Attendance (SA), barred from ESE
            mark.setLetterGrade("SA");
            mark.setGradePoint(0);
            mark.setResultStatus("SHORTAGE_ATTENDANCE");
            mark.setComputedEse(0.0);
            mark.setTotalMarks(mark.getComputedCia() != null ? mark.getComputedCia() : 0.0);
            return mark;
        } else if (attendancePct < 75.0 && !enrollment.isMedicalCondonation()) {
            // 65% to 74.9% without medical condonation: Shortage of Attendance (SA)
            mark.setLetterGrade("SA");
            mark.setGradePoint(0);
            mark.setResultStatus("SHORTAGE_ATTENDANCE");
            mark.setComputedEse(0.0);
            mark.setTotalMarks(mark.getComputedCia() != null ? mark.getComputedCia() : 0.0);
            return mark;
        }

        // 2. Check if student was Absent in ESE
        if (mark.isEseAbsent()) {
            mark.setLetterGrade("AB");
            mark.setGradePoint(0);
            mark.setResultStatus("ABSENT");
            mark.setComputedEse(0.0);
            mark.setTotalMarks(mark.getComputedCia() != null ? mark.getComputedCia() : 0.0);
            return mark;
        }

        // 3. Compute CIA internal component (out of 100)
        Double cia100 = null;
        if (mark.getCiaTest1() != null && mark.getCiaTest2() != null && mark.getAssignmentQuizLab() != null) {
            // KEC R2024: CIA1 (35%) + CIA2 (35%) + Assignments/Quizzes/Lab (30%)
            cia100 = (mark.getCiaTest1() * 0.35) + (mark.getCiaTest2() * 0.35) + (mark.getAssignmentQuizLab() * 0.30);
        } else if (mark.getCiaTest1() != null && mark.getCiaTest2() != null) {
            // Mid-semester tentative CIA
            cia100 = (mark.getCiaTest1() + mark.getCiaTest2()) / 2.0;
        }

        // 4. Scale CIA according to Course Type Weightage
        Double scaledCia = null;
        if (cia100 != null) {
            scaledCia = Math.round((cia100 * (courseType.getCiaWeightage() / 100.0)) * 100.0) / 100.0;
            mark.setComputedCia(scaledCia);
        }

        // If ESE marks haven't been entered yet, mark as IN_PROGRESS
        if (mark.getEseMarks() == null) {
            mark.setResultStatus("IN_PROGRESS");
            mark.setLetterGrade("-");
            mark.setGradePoint(0);
            return mark;
        }

        // 5. Scale ESE according to Course Type Weightage
        double rawEse = mark.getEseMarks();
        double scaledEse = Math.round((rawEse * (courseType.getEseWeightage() / 100.0)) * 100.0) / 100.0;
        mark.setComputedEse(scaledEse);

        // Total marks (CIA scaled + ESE scaled)
        double total = (scaledCia != null ? scaledCia : 0.0) + scaledEse;
        total = Math.round(total * 100.0) / 100.0;
        mark.setTotalMarks(total);

        // 6. Enforce KEC R2024 Qualifying Criteria
        // A. ESE Minimum Threshold:
        //    THEORY: 45% (raw >= 45.0, or scaled >= 27.0 out of 60)
        //    PRACTICAL: 50% (raw >= 50.0, or scaled >= 20.0 out of 40)
        //    EMBEDDED: 45% (raw >= 45.0, or scaled >= 22.5 out of 50)
        boolean passedEseMinimum = (rawEse >= courseType.getEseMinThresholdPct());

        // B. Overall Pass Minimum: Total aggregate >= 50.0 Marks out of 100
        boolean passedOverallMinimum = (total >= courseType.getOverallPassMinimum());

        if (!passedEseMinimum) {
            mark.setLetterGrade("RA");
            mark.setGradePoint(0);
            mark.setResultStatus("FAIL_ESE_MINIMUM");
        } else if (!passedOverallMinimum) {
            mark.setLetterGrade("RA");
            mark.setGradePoint(0);
            mark.setResultStatus("FAIL_OVERALL_MINIMUM");
        } else {
            // Student Passed! Map Total Marks to 10-Point Absolute Grading System
            int roundedTotal = (int) Math.round(total);
            if (roundedTotal >= 91) {
                mark.setLetterGrade("O");
                mark.setGradePoint(10);
            } else if (roundedTotal >= 81) {
                mark.setLetterGrade("A+");
                mark.setGradePoint(9);
            } else if (roundedTotal >= 71) {
                mark.setLetterGrade("A");
                mark.setGradePoint(8);
            } else if (roundedTotal >= 61) {
                mark.setLetterGrade("B+");
                mark.setGradePoint(7);
            } else if (roundedTotal >= 50) {
                mark.setLetterGrade("B");
                mark.setGradePoint(6);
            } else {
                mark.setLetterGrade("RA");
                mark.setGradePoint(0);
            }
            mark.setResultStatus("PASS");
        }

        return mark;
    }

    /**
     * Calculates SGPA = Sum(Credits * GradePoints) / Sum(Enrolled Credits)
     */
    public static double calculateSGPA(double totalWeightedPoints, double totalEnrolledCredits) {
        if (totalEnrolledCredits <= 0.0) return 0.0;
        return Math.round((totalWeightedPoints / totalEnrolledCredits) * 100.0) / 100.0;
    }

    /**
     * Calculates Equivalent Percentage = SGPA * 10
     */
    public static double calculateEquivalentPercentage(double sgpa) {
        return Math.round((sgpa * 10.0) * 100.0) / 100.0;
    }
}
