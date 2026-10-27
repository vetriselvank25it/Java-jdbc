package com.kec.eval.dao;

import com.kec.eval.db.DBConnection;
import com.kec.eval.engine.EvaluationEngine;
import com.kec.eval.model.AssessmentMark;
import com.kec.eval.model.ClassSummary;
import com.kec.eval.model.Course;
import com.kec.eval.model.CourseResult;
import com.kec.eval.model.CourseType;
import com.kec.eval.model.Enrollment;
import com.kec.eval.model.Student;
import com.kec.eval.model.StudentSemesterResult;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for Courses, Enrollments, Phased Marks Entry,
 * and Formal Academic Performance Reports under KEC R2024.
 */
public class AcademicDAO {

    // =========================================================================
    // 1. Course Catalog Management
    // =========================================================================

    public boolean addCourse(Course course) {
        String sql = "INSERT INTO courses (course_code, title, credits, course_type, department, semester) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, course.getCourseCode().trim().toUpperCase());
            ps.setString(2, course.getTitle().trim());
            ps.setDouble(3, course.getCredits());
            ps.setString(4, course.getCourseType().name());
            ps.setString(5, course.getDepartment().trim().toUpperCase());
            ps.setInt(6, course.getSemester());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[AcademicDAO] Error adding course: " + e.getMessage());
            return false;
        }
    }

    public Course getCourseByCode(String courseCode) {
        String sql = "SELECT course_code, title, credits, course_type, department, semester FROM courses WHERE course_code = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, courseCode.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Course(
                            rs.getString("course_code"),
                            rs.getString("title"),
                            rs.getDouble("credits"),
                            CourseType.valueOf(rs.getString("course_type")),
                            rs.getString("department"),
                            rs.getInt("semester")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("[AcademicDAO] Error fetching course: " + e.getMessage());
        }
        return null;
    }

    public List<Course> getAllCourses() {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT course_code, title, credits, course_type, department, semester FROM courses ORDER BY semester, course_code";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Course(
                        rs.getString("course_code"),
                        rs.getString("title"),
                        rs.getDouble("credits"),
                        CourseType.valueOf(rs.getString("course_type")),
                        rs.getString("department"),
                        rs.getInt("semester")
                ));
            }
        } catch (SQLException e) {
            System.err.println("[AcademicDAO] Error listing courses: " + e.getMessage());
        }
        return list;
    }

    // =========================================================================
    // 2. Enrollment & Attendance Tracking
    // =========================================================================

    public int enrollStudent(String rollNo, String courseCode, int semester, int classesHeld, int classesAttended) {
        String insertEnroll = "INSERT INTO enrollments (roll_no, course_code, semester, classes_held, classes_attended) VALUES (?, ?, ?, ?, ?)";
        String insertMark = "INSERT INTO assessment_marks (enrollment_id, result_status) VALUES (?, 'IN_PROGRESS')";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            int enrollmentId = -1;

            try (PreparedStatement ps = conn.prepareStatement(insertEnroll, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, rollNo.trim().toUpperCase());
                ps.setString(2, courseCode.trim().toUpperCase());
                ps.setInt(3, semester);
                ps.setInt(4, classesHeld);
                ps.setInt(5, classesAttended);
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        enrollmentId = rs.getInt(1);
                    }
                }
            }

            if (enrollmentId > 0) {
                try (PreparedStatement psMark = conn.prepareStatement(insertMark)) {
                    psMark.setInt(1, enrollmentId);
                    psMark.executeUpdate();
                }
            }

            conn.commit();
            return enrollmentId;
        } catch (SQLException e) {
            System.err.println("[AcademicDAO] Error enrolling student: " + e.getMessage());
            return -1;
        }
    }

    public boolean updateAttendance(int enrollmentId, int classesHeld, int classesAttended) {
        String sql = "UPDATE enrollments SET classes_held = ?, classes_attended = ? WHERE enrollment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classesHeld);
            ps.setInt(2, classesAttended);
            ps.setInt(3, enrollmentId);
            boolean updated = ps.executeUpdate() > 0;
            if (updated) {
                recalculateAndPersistEvaluation(enrollmentId);
            }
            return updated;
        } catch (SQLException e) {
            System.err.println("[AcademicDAO] Error updating attendance: " + e.getMessage());
            return false;
        }
    }

    public boolean setMedicalCondonation(int enrollmentId, boolean approved, String reason) {
        String sql = "UPDATE enrollments SET medical_condonation = ?, condonation_reason = ? WHERE enrollment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, approved);
            ps.setString(2, reason);
            ps.setInt(3, enrollmentId);
            boolean updated = ps.executeUpdate() > 0;
            if (updated) {
                recalculateAndPersistEvaluation(enrollmentId);
            }
            return updated;
        } catch (SQLException e) {
            System.err.println("[AcademicDAO] Error setting medical condonation: " + e.getMessage());
            return false;
        }
    }

    public Enrollment getEnrollment(int enrollmentId) {
        String sql = "SELECT e.enrollment_id, e.roll_no, e.course_code, e.semester, e.classes_held, e.classes_attended, "
                + "e.medical_condonation, e.condonation_reason, s.name AS student_name, s.department AS student_dept, "
                + "c.title AS course_title, c.course_type, c.credits "
                + "FROM enrollments e "
                + "JOIN students s ON e.roll_no = s.roll_no "
                + "JOIN courses c ON e.course_code = c.course_code "
                + "WHERE e.enrollment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, enrollmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapEnrollment(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[AcademicDAO] Error fetching enrollment: " + e.getMessage());
        }
        return null;
    }

    public Enrollment getEnrollmentByStudentAndCourse(String rollNo, String courseCode, int semester) {
        String sql = "SELECT e.enrollment_id, e.roll_no, e.course_code, e.semester, e.classes_held, e.classes_attended, "
                + "e.medical_condonation, e.condonation_reason, s.name AS student_name, s.department AS student_dept, "
                + "c.title AS course_title, c.course_type, c.credits "
                + "FROM enrollments e "
                + "JOIN students s ON e.roll_no = s.roll_no "
                + "JOIN courses c ON e.course_code = c.course_code "
                + "WHERE e.roll_no = ? AND e.course_code = ? AND e.semester = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rollNo.trim().toUpperCase());
            ps.setString(2, courseCode.trim().toUpperCase());
            ps.setInt(3, semester);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapEnrollment(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[AcademicDAO] Error fetching student course enrollment: " + e.getMessage());
        }
        return null;
    }

    public List<Enrollment> getEnrollmentsByStudent(String rollNo, int semester) {
        List<Enrollment> list = new ArrayList<>();
        String sql = "SELECT e.enrollment_id, e.roll_no, e.course_code, e.semester, e.classes_held, e.classes_attended, "
                + "e.medical_condonation, e.condonation_reason, s.name AS student_name, s.department AS student_dept, "
                + "c.title AS course_title, c.course_type, c.credits "
                + "FROM enrollments e "
                + "JOIN students s ON e.roll_no = s.roll_no "
                + "JOIN courses c ON e.course_code = c.course_code "
                + "WHERE e.roll_no = ? AND e.semester = ? "
                + "ORDER BY c.course_code";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rollNo.trim().toUpperCase());
            ps.setInt(2, semester);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapEnrollment(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[AcademicDAO] Error fetching student enrollments: " + e.getMessage());
        }
        return list;
    }

    public List<Enrollment> getAllEnrollments() {
        List<Enrollment> list = new ArrayList<>();
        String sql = "SELECT e.enrollment_id, e.roll_no, e.course_code, e.semester, e.classes_held, e.classes_attended, "
                + "e.medical_condonation, e.condonation_reason, s.name AS student_name, s.department AS student_dept, "
                + "c.title AS course_title, c.course_type, c.credits "
                + "FROM enrollments e "
                + "JOIN students s ON e.roll_no = s.roll_no "
                + "JOIN courses c ON e.course_code = c.course_code "
                + "ORDER BY e.semester, e.roll_no, e.course_code";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapEnrollment(rs));
            }
        } catch (SQLException e) {
            System.err.println("[AcademicDAO] Error listing all enrollments: " + e.getMessage());
        }
        return list;
    }

    // =========================================================================
    // 3. Incremental Marks Entry ("Then and Now")
    // =========================================================================

    /**
     * Phase 1 (Mid-Semester): Record CIA Test 1 (0-100) and CIA Test 2 (0-100).
     */
    public boolean recordPhase1Marks(int enrollmentId, double cia1, double cia2) {
        String sql = "UPDATE assessment_marks SET cia_test1 = ?, cia_test2 = ? WHERE enrollment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, cia1);
            ps.setDouble(2, cia2);
            ps.setInt(3, enrollmentId);
            boolean updated = ps.executeUpdate() > 0;
            if (updated) {
                recalculateAndPersistEvaluation(enrollmentId);
            }
            return updated;
        } catch (SQLException e) {
            System.err.println("[AcademicDAO] Error recording Phase 1 marks: " + e.getMessage());
            return false;
        }
    }

    /**
     * Phase 2 (End of Semester - CIA): Record Assignments / Quizzes / Lab Evaluations (0-100).
     */
    public boolean recordPhase2Marks(int enrollmentId, double assignmentMarks) {
        String sql = "UPDATE assessment_marks SET assignment_quiz_lab = ? WHERE enrollment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, assignmentMarks);
            ps.setInt(2, enrollmentId);
            boolean updated = ps.executeUpdate() > 0;
            if (updated) {
                recalculateAndPersistEvaluation(enrollmentId);
            }
            return updated;
        } catch (SQLException e) {
            System.err.println("[AcademicDAO] Error recording Phase 2 marks: " + e.getMessage());
            return false;
        }
    }

    /**
     * Phase 3 (Post Semester): Record End Semester Examination (ESE) Marks (0-100) or mark Absent.
     */
    public boolean recordPhase3Marks(int enrollmentId, Double eseMarks, boolean isAbsent) {
        String sql = "UPDATE assessment_marks SET ese_marks = ?, is_ese_absent = ? WHERE enrollment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (isAbsent || eseMarks == null) {
                ps.setNull(1, Types.DECIMAL);
                ps.setBoolean(2, true);
            } else {
                ps.setDouble(1, eseMarks);
                ps.setBoolean(2, false);
            }
            ps.setInt(3, enrollmentId);
            boolean updated = ps.executeUpdate() > 0;
            if (updated) {
                recalculateAndPersistEvaluation(enrollmentId);
            }
            return updated;
        } catch (SQLException e) {
            System.err.println("[AcademicDAO] Error recording Phase 3 marks: " + e.getMessage());
            return false;
        }
    }

    /**
     * Recalculates and persists final grades using KEC R2024 EvaluationEngine.
     */
    public boolean recalculateAndPersistEvaluation(int enrollmentId) {
        Enrollment enrollment = getEnrollment(enrollmentId);
        if (enrollment == null) return false;

        AssessmentMark mark = getAssessmentMarkByEnrollmentId(enrollmentId);
        if (mark == null) {
            mark = new AssessmentMark(enrollmentId);
        }

        Course course = getCourseByCode(enrollment.getCourseCode());
        if (course == null) return false;

        // Apply KEC R2024 evaluation engine
        EvaluationEngine.evaluateCourse(enrollment, course.getCourseType(), mark);

        String sql = "UPDATE assessment_marks SET "
                + "computed_cia = ?, computed_ese = ?, total_marks = ?, "
                + "letter_grade = ?, grade_point = ?, result_status = ? "
                + "WHERE enrollment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (mark.getComputedCia() != null) ps.setDouble(1, mark.getComputedCia()); else ps.setNull(1, Types.DECIMAL);
            if (mark.getComputedEse() != null) ps.setDouble(2, mark.getComputedEse()); else ps.setNull(2, Types.DECIMAL);
            if (mark.getTotalMarks() != null) ps.setDouble(3, mark.getTotalMarks()); else ps.setNull(3, Types.DECIMAL);
            if (mark.getLetterGrade() != null) ps.setString(4, mark.getLetterGrade()); else ps.setNull(4, Types.VARCHAR);
            if (mark.getGradePoint() != null) ps.setInt(5, mark.getGradePoint()); else ps.setNull(5, Types.INTEGER);
            ps.setString(6, mark.getResultStatus());
            ps.setInt(7, enrollmentId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[AcademicDAO] Error persisting marks evaluation: " + e.getMessage());
            return false;
        }
    }

    public AssessmentMark getAssessmentMarkByEnrollmentId(int enrollmentId) {
        String sql = "SELECT mark_id, enrollment_id, cia_test1, cia_test2, assignment_quiz_lab, "
                + "ese_marks, is_ese_absent, computed_cia, computed_ese, total_marks, "
                + "letter_grade, grade_point, result_status FROM assessment_marks WHERE enrollment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, enrollmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    AssessmentMark m = new AssessmentMark();
                    m.setMarkId(rs.getInt("mark_id"));
                    m.setEnrollmentId(rs.getInt("enrollment_id"));
                    m.setCiaTest1(getNullableDouble(rs, "cia_test1"));
                    m.setCiaTest2(getNullableDouble(rs, "cia_test2"));
                    m.setAssignmentQuizLab(getNullableDouble(rs, "assignment_quiz_lab"));
                    m.setEseMarks(getNullableDouble(rs, "ese_marks"));
                    m.setEseAbsent(rs.getBoolean("is_ese_absent"));
                    m.setComputedCia(getNullableDouble(rs, "computed_cia"));
                    m.setComputedEse(getNullableDouble(rs, "computed_ese"));
                    m.setTotalMarks(getNullableDouble(rs, "total_marks"));
                    m.setLetterGrade(rs.getString("letter_grade"));
                    m.setGradePoint(getNullableInteger(rs, "grade_point"));
                    m.setResultStatus(rs.getString("result_status"));
                    return m;
                }
            }
        } catch (SQLException e) {
            System.err.println("[AcademicDAO] Error fetching assessment marks: " + e.getMessage());
        }
        return null;
    }

    // =========================================================================
    // 4. Expected Reporting Outputs
    // =========================================================================

    /**
     * Report 1: Student Semester Mark Sheet
     * Generates a complete formal mark sheet for a student and semester,
     * including internal CIA, external ESE, final total, letter grade, SGPA, and %
     */
    public StudentSemesterResult generateStudentSemesterMarkSheet(String rollNo, int semester) {
        StudentDAO studentDAO = new StudentDAO();
        Student student = studentDAO.getStudentByRollNo(rollNo);
        if (student == null) {
            return null;
        }

        StudentSemesterResult result = new StudentSemesterResult(student, semester);

        String sql = "SELECT e.enrollment_id, e.roll_no, e.course_code, e.semester, e.classes_held, e.classes_attended, "
                + "e.medical_condonation, c.title AS course_title, c.course_type, c.credits, "
                + "m.cia_test1, m.cia_test2, m.assignment_quiz_lab, m.computed_cia, "
                + "m.ese_marks, m.is_ese_absent, m.computed_ese, m.total_marks, "
                + "m.letter_grade, m.grade_point, m.result_status "
                + "FROM enrollments e "
                + "JOIN courses c ON e.course_code = c.course_code "
                + "LEFT JOIN assessment_marks m ON e.enrollment_id = m.enrollment_id "
                + "WHERE e.roll_no = ? AND e.semester = ? "
                + "ORDER BY c.course_code";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rollNo.trim().toUpperCase());
            ps.setInt(2, semester);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CourseResult cr = new CourseResult();
                    cr.setCourseCode(rs.getString("course_code"));
                    cr.setCourseTitle(rs.getString("course_title"));
                    cr.setCourseType(CourseType.valueOf(rs.getString("course_type")));
                    cr.setCredits(rs.getDouble("credits"));
                    cr.setClassesHeld(rs.getInt("classes_held"));
                    cr.setClassesAttended(rs.getInt("classes_attended"));
                    double attPct = (cr.getClassesHeld() > 0)
                            ? (cr.getClassesAttended() * 100.0) / cr.getClassesHeld()
                            : 100.0;
                    cr.setAttendancePercentage(Math.round(attPct * 100.0) / 100.0);
                    cr.setMedicalCondonation(rs.getBoolean("medical_condonation"));

                    cr.setRawCia1(getNullableDouble(rs, "cia_test1"));
                    cr.setRawCia2(getNullableDouble(rs, "cia_test2"));
                    cr.setRawAssignment(getNullableDouble(rs, "assignment_quiz_lab"));
                    cr.setScaledCia(getNullableDouble(rs, "computed_cia"));
                    cr.setRawEse(getNullableDouble(rs, "ese_marks"));
                    cr.setScaledEse(getNullableDouble(rs, "computed_ese"));
                    cr.setEseAbsent(rs.getBoolean("is_ese_absent"));
                    cr.setTotalMarks(getNullableDouble(rs, "total_marks"));
                    cr.setLetterGrade(rs.getString("letter_grade") != null ? rs.getString("letter_grade") : "-");
                    cr.setGradePoint(rs.getInt("grade_point"));
                    cr.setResultStatus(rs.getString("result_status") != null ? rs.getString("result_status") : "IN_PROGRESS");

                    result.addCourseResult(cr);
                }
            }
        } catch (SQLException e) {
            System.err.println("[AcademicDAO] Error generating semester mark sheet: " + e.getMessage());
        }

        result.computeSummary();
        return result;
    }

    /**
     * Report 2: Attendance Defaulter Report
     * Lists all students with attendance < 65% or 65%-74.9% without medical condonation approval,
     * flagged with grade SA.
     */
    public List<Enrollment> getAttendanceDefaulters(Integer semester) {
        List<Enrollment> defaulters = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT e.enrollment_id, e.roll_no, e.course_code, e.semester, e.classes_held, e.classes_attended, "
                + "e.medical_condonation, e.condonation_reason, s.name AS student_name, s.department AS student_dept, "
                + "c.title AS course_title, c.course_type, c.credits "
                + "FROM enrollments e "
                + "JOIN students s ON e.roll_no = s.roll_no "
                + "JOIN courses c ON e.course_code = c.course_code "
                + "WHERE ( (e.classes_attended * 100.0 / e.classes_held) < 65.0 "
                + "        OR ((e.classes_attended * 100.0 / e.classes_held) < 75.0 AND e.medical_condonation = FALSE) ) "
        );

        if (semester != null) {
            sql.append("AND e.semester = ? ");
        }
        sql.append("ORDER BY e.semester, s.department, e.roll_no");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (semester != null) {
                ps.setInt(1, semester);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    defaulters.add(mapEnrollment(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[AcademicDAO] Error fetching attendance defaulters: " + e.getMessage());
        }
        return defaulters;
    }

    /**
     * Report 3: Class Performance & Result Summary
     * Department-wise summary showing total students, pass/fail count, SA/AB count,
     * pass percentage, and average SGPA.
     */
    public List<ClassSummary> getClassPerformanceSummary(int semester) {
        List<ClassSummary> summaries = new ArrayList<>();

        // Group students by department
        String deptSql = "SELECT DISTINCT department FROM students ORDER BY department";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(deptSql)) {

            List<String> depts = new ArrayList<>();
            while (rs.next()) {
                depts.add(rs.getString("department"));
            }

            for (String dept : depts) {
                ClassSummary summary = computeDepartmentSummary(dept, semester);
                if (summary.getTotalStudents() > 0) {
                    summaries.add(summary);
                }
            }
        } catch (SQLException e) {
            System.err.println("[AcademicDAO] Error compiling class performance summary: " + e.getMessage());
        }

        return summaries;
    }

    private ClassSummary computeDepartmentSummary(String department, int semester) {
        ClassSummary summary = new ClassSummary();
        summary.setDepartment(department);
        summary.setSemester(semester);

        // Get students in department who have enrollments in this semester
        String sqlStudents = "SELECT DISTINCT s.roll_no FROM students s "
                + "JOIN enrollments e ON s.roll_no = e.roll_no "
                + "WHERE s.department = ? AND e.semester = ?";

        List<String> rollNos = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlStudents)) {
            ps.setString(1, department);
            ps.setInt(2, semester);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rollNos.add(rs.getString("roll_no"));
                }
            }
        } catch (SQLException e) {
            System.err.println("[AcademicDAO] Error in computeDepartmentSummary: " + e.getMessage());
        }

        summary.setTotalStudents(rollNos.size());

        int totalEnrollments = 0;
        int passedEnrollments = 0;
        int raEnrollments = 0;
        int saEnrollments = 0;
        int abEnrollments = 0;
        double sgpaSum = 0.0;
        int studentsWithSgpa = 0;

        for (String rollNo : rollNos) {
            StudentSemesterResult sr = generateStudentSemesterMarkSheet(rollNo, semester);
            if (sr != null && !sr.getCourseResults().isEmpty()) {
                sgpaSum += sr.getSgpa();
                studentsWithSgpa++;

                for (CourseResult cr : sr.getCourseResults()) {
                    totalEnrollments++;
                    if ("PASS".equalsIgnoreCase(cr.getResultStatus())) {
                        passedEnrollments++;
                    } else if ("SA".equalsIgnoreCase(cr.getLetterGrade())) {
                        saEnrollments++;
                    } else if ("AB".equalsIgnoreCase(cr.getLetterGrade())) {
                        abEnrollments++;
                    } else {
                        raEnrollments++;
                    }
                }
            }
        }

        summary.setTotalEnrollments(totalEnrollments);
        summary.setPassedEnrollments(passedEnrollments);
        summary.setRaEnrollments(raEnrollments);
        summary.setSaEnrollments(saEnrollments);
        summary.setAbEnrollments(abEnrollments);

        if (totalEnrollments > 0) {
            double passPct = (passedEnrollments * 100.0) / totalEnrollments;
            summary.setPassPercentage(Math.round(passPct * 100.0) / 100.0);
        } else {
            summary.setPassPercentage(0.0);
        }

        if (studentsWithSgpa > 0) {
            double avgSgpa = sgpaSum / studentsWithSgpa;
            summary.setAverageSgpa(Math.round(avgSgpa * 100.0) / 100.0);
        } else {
            summary.setAverageSgpa(0.0);
        }

        return summary;
    }

    private Enrollment mapEnrollment(ResultSet rs) throws SQLException {
        Enrollment e = new Enrollment();
        e.setEnrollmentId(rs.getInt("enrollment_id"));
        e.setRollNo(rs.getString("roll_no"));
        e.setCourseCode(rs.getString("course_code"));
        e.setSemester(rs.getInt("semester"));
        e.setClassesHeld(rs.getInt("classes_held"));
        e.setClassesAttended(rs.getInt("classes_attended"));
        e.setMedicalCondonation(rs.getBoolean("medical_condonation"));
        e.setCondonationReason(rs.getString("condonation_reason"));
        e.setStudentName(rs.getString("student_name"));
        e.setStudentDepartment(rs.getString("student_dept"));
        e.setCourseTitle(rs.getString("course_title"));
        e.setCourseType(CourseType.valueOf(rs.getString("course_type")));
        e.setCredits(rs.getDouble("credits"));
        return e;
    }

    private Double getNullableDouble(ResultSet rs, String col) throws SQLException {
        double val = rs.getDouble(col);
        return rs.wasNull() ? null : val;
    }

    private Integer getNullableInteger(ResultSet rs, String col) throws SQLException {
        int val = rs.getInt(col);
        return rs.wasNull() ? null : val;
    }
}
