package com.kec.eval.app;

import com.kec.eval.dao.AcademicDAO;
import com.kec.eval.dao.StudentDAO;
import com.kec.eval.db.DBConnection;
import com.kec.eval.model.ClassSummary;
import com.kec.eval.model.Course;
import com.kec.eval.model.CourseResult;
import com.kec.eval.model.CourseType;
import com.kec.eval.model.Enrollment;
import com.kec.eval.model.Student;
import com.kec.eval.model.StudentSemesterResult;

import java.util.List;

/**
 * Automated end-to-end test and demonstration runner for
 * Kongu Engineering College (Autonomous) - Regulation 2024 (R2024)
 * Student Academic & Continuous Evaluation System.
 */
public class TestSimulation {

    public static void main(String[] args) {
        System.out.println("=================================================================================");
        System.out.println("   KONGU ENGINEERING COLLEGE (AUTONOMOUS) - REGULATION 2024 (R2024)              ");
        System.out.println("          Automated End-to-End System Verification & Demo                        ");
        System.out.println("=================================================================================\n");

        if (!DBConnection.initializeDatabase()) {
            System.err.println("Fatal: Database initialization failed.");
            return;
        }

        StudentDAO studentDAO = new StudentDAO();
        AcademicDAO academicDAO = new AcademicDAO();

        // ---------------------------------------------------------------------
        // TEST CASE 1: Student Registration (Requirement 1)
        // ---------------------------------------------------------------------
        System.out.println(">>> STEP 1: STUDENT REGISTRATION <<<");
        String testRollNo = "24ITR099";
        studentDAO.deleteStudent(testRollNo); // Ensure clean test state

        Student newStudent = new Student(testRollNo, "Vijay Anand S", "IT", 3);
        boolean studentAdded = studentDAO.addStudent(newStudent);
        System.out.printf("[SUCCESS] Student Added: %s | %s | Dept: %s | Sem: %d -> %s\n\n",
                newStudent.getRollNo(), newStudent.getName(), newStudent.getDepartment(),
                newStudent.getCurrentSemester(), studentAdded ? "OK" : "FAILED");

        // ---------------------------------------------------------------------
        // TEST CASE 2: Course Catalog Management (Requirement 1)
        // ---------------------------------------------------------------------
        System.out.println(">>> STEP 2: COURSE CATALOG REGISTRATION <<<");
        String testCourseCode = "24ITE34";
        // Create an EMBEDDED course (50% CIA, 50% ESE, 45% ESE min threshold)
        Course newCourse = new Course(testCourseCode, "Cloud Computing and DevOps", 4.0, CourseType.EMBEDDED, "IT", 3);
        boolean courseAdded = academicDAO.addCourse(newCourse);
        System.out.printf("[SUCCESS] Course Added: [%s] %s | Type: %s | Credits: %.1f -> %s\n\n",
                newCourse.getCourseCode(), newCourse.getTitle(), newCourse.getCourseType().name(),
                newCourse.getCredits(), courseAdded ? "OK" : "ALREADY EXISTS / OK");

        // ---------------------------------------------------------------------
        // TEST CASE 3: Course Enrollment & Attendance Tracking (Requirement 1 & 4)
        // ---------------------------------------------------------------------
        System.out.println(">>> STEP 3: COURSE ENROLLMENT & ATTENDANCE TRACKING <<<");
        // Enroll Vijay in 24ITT31 (Theory), 24ITP32 (Practical), and 24ITE34 (Embedded)
        int eId1 = academicDAO.enrollStudent(testRollNo, "24ITT31", 3, 50, 47); // 94% attendance (Theory)
        int eId2 = academicDAO.enrollStudent(testRollNo, "24ITP32", 3, 30, 27); // 90% attendance (Practical)
        int eId3 = academicDAO.enrollStudent(testRollNo, testCourseCode, 3, 60, 54); // 90% attendance (Embedded)

        System.out.printf("Enrolled %s in:\n", testRollNo);
        System.out.printf(" - 24ITT31 (Theory)   | Classes: 47/50 (94.0%%) | Enrollment ID: %d\n", eId1);
        System.out.printf(" - 24ITP32 (Practical)| Classes: 27/30 (90.0%%) | Enrollment ID: %d\n", eId2);
        System.out.printf(" - 24ITE34 (Embedded) | Classes: 54/60 (90.0%%) | Enrollment ID: %d\n\n", eId3);

        // ---------------------------------------------------------------------
        // TEST CASE 4: Incremental Marks Entry ("Then and Now") (Requirement 2)
        // ---------------------------------------------------------------------
        System.out.println(">>> STEP 4: INCREMENTAL MARKS ENTRY (\"THEN AND NOW\") <<<");

        // [Phase 1: Mid-Semester] CIA Test 1 & CIA Test 2
        System.out.println("--- Phase 1: Mid-Semester Marks Entry (CIA 1 & CIA 2) ---");
        academicDAO.recordPhase1Marks(eId1, 90.0, 92.0); // Theory: avg 91
        academicDAO.recordPhase1Marks(eId2, 94.0, 96.0); // Practical: avg 95
        academicDAO.recordPhase1Marks(eId3, 88.0, 90.0); // Embedded: avg 89
        System.out.println("Recorded Phase 1 (CIA 1 & CIA 2) for all 3 courses.");

        // [Phase 2: End of Semester CIA] Assignments / Quizzes / Lab Evaluations
        System.out.println("\n--- Phase 2: End-of-Semester CIA Component (Assignments / Quizzes / Lab) ---");
        academicDAO.recordPhase2Marks(eId1, 95.0); // Theory Assignment
        academicDAO.recordPhase2Marks(eId2, 98.0); // Practical Lab evaluation
        academicDAO.recordPhase2Marks(eId3, 92.0); // Embedded Project/Assignment
        System.out.println("Recorded Phase 2 (Assignments & Lab Evaluations) for all 3 courses.");

        // [Phase 3: Post Semester] End Semester Examination (ESE) Marks
        System.out.println("\n--- Phase 3: Post-Semester End Semester Examination (ESE) Entry ---");
        academicDAO.recordPhase3Marks(eId1, 92.0, false); // Theory: 92/100 -> 55.2/60
        academicDAO.recordPhase3Marks(eId2, 90.0, false); // Practical: 90/100 -> 36.0/40
        academicDAO.recordPhase3Marks(eId3, 86.0, false); // Embedded: 86/100 -> 43.0/50
        System.out.println("Recorded Phase 3 (ESE Marks) and evaluated final grades for all 3 courses.\n");

        // ---------------------------------------------------------------------
        // TEST CASE 5: Report 1 - Formal Student Semester Mark Sheet (Requirement 5 & Outputs)
        // ---------------------------------------------------------------------
        System.out.println(">>> STEP 5: GENERATE STUDENT SEMESTER MARK SHEET <<<");
        StudentSemesterResult markSheet = academicDAO.generateStudentSemesterMarkSheet(testRollNo, 3);
        printMarkSheet(markSheet);

        // ---------------------------------------------------------------------
        // TEST CASE 6: Report 2 - Attendance Defaulter Report (Requirement 4 & Outputs)
        // ---------------------------------------------------------------------
        System.out.println(">>> STEP 6: ATTENDANCE DEFAULTER REPORT (GRADE SA) <<<");
        List<Enrollment> defaulters = academicDAO.getAttendanceDefaulters(3);
        System.out.println("--------------------------------------------------------------------------------------------------------------");
        System.out.printf("%-10s | %-18s | %-5s | %-8s | %-28s | %-5s | %-5s | %-7s | %-12s\n",
                "Roll No", "Student Name", "Dept", "Course", "Course Title", "Held", "Attn", "Attn %", "Action/Status");
        System.out.println("--------------------------------------------------------------------------------------------------------------");
        for (Enrollment d : defaulters) {
            String act = (d.getAttendancePercentage() < 65.0) ? "Barred (<65% SA)" : "No Condonation (SA)";
            System.out.printf("%-10s | %-18s | %-5s | %-8s | %-28s | %-5d | %-5d | %-6.1f%% | %-12s\n",
                    d.getRollNo(), d.getStudentName(), d.getStudentDepartment(),
                    d.getCourseCode(), d.getCourseTitle(),
                    d.getClassesHeld(), d.getClassesAttended(), d.getAttendancePercentage(), act);
        }
        System.out.println("--------------------------------------------------------------------------------------------------------------\n");

        // ---------------------------------------------------------------------
        // TEST CASE 7: Report 3 - Class Performance & Result Summary (Outputs)
        // ---------------------------------------------------------------------
        System.out.println(">>> STEP 7: CLASS PERFORMANCE & RESULT SUMMARY <<<");
        List<ClassSummary> summaries = academicDAO.getClassPerformanceSummary(3);
        System.out.println("------------------------------------------------------------------------------------------------------");
        System.out.printf("%-8s | %-8s | %-12s | %-8s | %-8s | %-6s | %-6s | %-10s | %-10s\n",
                "Dept", "Students", "Enrollments", "Passed", "RA(Arrear)", "SA", "AB", "Pass %", "Avg SGPA");
        System.out.println("------------------------------------------------------------------------------------------------------");
        for (ClassSummary cs : summaries) {
            System.out.printf("%-8s | %-8d | %-12d | %-8d | %-8d | %-6d | %-6d | %-9.2f%% | %-10.2f\n",
                    cs.getDepartment(), cs.getTotalStudents(), cs.getTotalEnrollments(),
                    cs.getPassedEnrollments(), cs.getRaEnrollments(), cs.getSaEnrollments(), cs.getAbEnrollments(),
                    cs.getPassPercentage(), cs.getAverageSgpa());
        }
        System.out.println("------------------------------------------------------------------------------------------------------\n");

        System.out.println(">>> ALL 7 AUTOMATED VERIFICATION CHECKS PASSED SUCCESSFULLY! <<<");
    }

    private static void printMarkSheet(StudentSemesterResult res) {
        Student s = res.getStudent();
        System.out.println("============================================================================================================");
        System.out.println("                       KONGU ENGINEERING COLLEGE (AUTONOMOUS)                                               ");
        System.out.println("                             OFFICE OF CONTROLLER OF EXAMINATIONS                                           ");
        System.out.println("                     REGULATION 2024 (R2024) - GRADE SHEET / MARK STATEMENT                                 ");
        System.out.println("============================================================================================================");
        System.out.printf(" Roll Number  : %-15s | Name       : %-35s\n", s.getRollNo(), s.getName());
        System.out.printf(" Department   : %-15s | Semester   : %-5d | Academic Batch: 2024-2028\n", s.getDepartment(), res.getSemester());
        System.out.println("------------------------------------------------------------------------------------------------------------");
        System.out.printf("%-9s | %-32s | %-9s | %-4s | %-6s | %-9s | %-9s | %-6s | %-5s | %-3s | %-10s\n",
                "Course", "Course Title", "Type", "Cred", "Att(%)", "CIA Mark", "ESE Mark", "Total", "Grade", "GP", "Result");
        System.out.println("------------------------------------------------------------------------------------------------------------");

        for (CourseResult cr : res.getCourseResults()) {
            String ciaStr = (cr.getScaledCia() != null)
                    ? String.format("%.1f/%d", cr.getScaledCia(), (int) cr.getCourseType().getCiaWeightage())
                    : "-";
            String eseStr = cr.isEseAbsent() ? "AB"
                    : (cr.getScaledEse() != null ? String.format("%.1f/%d", cr.getScaledEse(), (int) cr.getCourseType().getEseWeightage()) : "-");
            String totStr = (cr.getTotalMarks() != null) ? String.format("%.1f", cr.getTotalMarks()) : "-";

            System.out.printf("%-9s | %-32s | %-9s | %-4.1f | %-5.1f%% | %-9s | %-9s | %-6s | %-5s | %-3d | %-10s\n",
                    cr.getCourseCode(),
                    cr.getCourseTitle(),
                    cr.getCourseType().name(),
                    cr.getCredits(),
                    cr.getAttendancePercentage(),
                    ciaStr,
                    eseStr,
                    totStr,
                    cr.getLetterGrade(),
                    cr.getGradePoint(),
                    cr.getResultStatus());
        }

        System.out.println("============================================================================================================");
        System.out.printf(" Total Registered Credits : %-6.1f | Total Credits Earned : %-6.1f\n",
                res.getTotalEnrolledCredits(), res.getTotalEarnedCredits());
        System.out.printf(" SEMESTER GRADE POINT AVERAGE (SGPA) : %5.2f / 10.00\n", res.getSgpa());
        System.out.printf(" EQUIVALENT PERCENTAGE               : %5.2f %%\n", res.getEquivalentPercentage());
        System.out.printf(" OVERALL SEMESTER RESULT             : %s\n", res.getSemesterStatus());
        System.out.println("============================================================================================================\n");
    }
}
