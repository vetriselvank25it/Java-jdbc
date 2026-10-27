-- =======================================================================
-- Kongu Engineering College (Autonomous), Perundurai, Erode - 638060
-- Regulation 2024 (R2024) - Student Academic & Continuous Evaluation System
-- Database Schema Script: kec_schema.sql
-- =======================================================================

DROP DATABASE IF EXISTS kec_eval_db;
CREATE DATABASE kec_eval_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE kec_eval_db;

-- -----------------------------------------------------------------------
-- Table 1: students
-- Stores core student profile data
-- -----------------------------------------------------------------------
CREATE TABLE students (
    roll_no VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    department VARCHAR(20) NOT NULL,
    current_semester INT NOT NULL CHECK (current_semester BETWEEN 1 AND 8),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- -----------------------------------------------------------------------
-- Table 2: courses
-- Stores course catalog with credits and course types under R2024
-- -----------------------------------------------------------------------
CREATE TABLE courses (
    course_code VARCHAR(20) PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    credits DECIMAL(3, 1) NOT NULL CHECK (credits > 0),
    course_type ENUM('THEORY', 'PRACTICAL', 'EMBEDDED') NOT NULL,
    department VARCHAR(20) NOT NULL,
    semester INT NOT NULL CHECK (semester BETWEEN 1 AND 8)
);

-- -----------------------------------------------------------------------
-- Table 3: enrollments
-- Links students with enrolled courses, tracking attendance and condonation
-- -----------------------------------------------------------------------
CREATE TABLE enrollments (
    enrollment_id INT AUTO_INCREMENT PRIMARY KEY,
    roll_no VARCHAR(20) NOT NULL,
    course_code VARCHAR(20) NOT NULL,
    semester INT NOT NULL,
    classes_held INT NOT NULL DEFAULT 0 CHECK (classes_held >= 0),
    classes_attended INT NOT NULL DEFAULT 0 CHECK (classes_attended >= 0),
    medical_condonation BOOLEAN NOT NULL DEFAULT FALSE,
    condonation_reason VARCHAR(255) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_enrollment_student FOREIGN KEY (roll_no) REFERENCES students(roll_no) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_enrollment_course FOREIGN KEY (course_code) REFERENCES courses(course_code) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT unique_student_course_sem UNIQUE (roll_no, course_code, semester),
    CONSTRAINT check_attendance_validity CHECK (classes_attended <= classes_held)
);

-- -----------------------------------------------------------------------
-- Table 4: assessment_marks
-- Supports phased / incremental evaluation ("Then and Now"):
-- Phase 1: Mid-Semester CIA Tests 1 & 2
-- Phase 2: End-of-Semester Assignments / Quizzes / Lab Evaluations
-- Phase 3: Post-Semester End Semester Examination (ESE) & Absentee Flag
-- -----------------------------------------------------------------------
CREATE TABLE assessment_marks (
    mark_id INT AUTO_INCREMENT PRIMARY KEY,
    enrollment_id INT NOT NULL UNIQUE,
    cia_test1 DECIMAL(5, 2) DEFAULT NULL CHECK (cia_test1 IS NULL OR (cia_test1 >= 0 AND cia_test1 <= 100)),
    cia_test2 DECIMAL(5, 2) DEFAULT NULL CHECK (cia_test2 IS NULL OR (cia_test2 >= 0 AND cia_test2 <= 100)),
    assignment_quiz_lab DECIMAL(5, 2) DEFAULT NULL CHECK (assignment_quiz_lab IS NULL OR (assignment_quiz_lab >= 0 AND assignment_quiz_lab <= 100)),
    ese_marks DECIMAL(5, 2) DEFAULT NULL CHECK (ese_marks IS NULL OR (ese_marks >= 0 AND ese_marks <= 100)),
    is_ese_absent BOOLEAN NOT NULL DEFAULT FALSE,
    computed_cia DECIMAL(5, 2) DEFAULT NULL,
    computed_ese DECIMAL(5, 2) DEFAULT NULL,
    total_marks DECIMAL(5, 2) DEFAULT NULL,
    letter_grade VARCHAR(5) DEFAULT NULL,
    grade_point INT DEFAULT NULL,
    result_status VARCHAR(30) DEFAULT 'IN_PROGRESS',
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_marks_enrollment FOREIGN KEY (enrollment_id) REFERENCES enrollments(enrollment_id) ON DELETE CASCADE
);

-- -----------------------------------------------------------------------
-- Indexes for optimized reporting and query performance
-- -----------------------------------------------------------------------
CREATE INDEX idx_student_dept_sem ON students(department, current_semester);
CREATE INDEX idx_enroll_student ON enrollments(roll_no);
CREATE INDEX idx_enroll_course ON enrollments(course_code);
CREATE INDEX idx_marks_enroll ON assessment_marks(enrollment_id);

-- -----------------------------------------------------------------------
-- Sample Seed Data for Demonstration & Testing
-- -----------------------------------------------------------------------

-- 1. Insert Sample Students (R2024 Batch)
INSERT INTO students (roll_no, name, department, current_semester) VALUES
('24ITR001', 'Aadhavan K', 'IT', 3),
('24ITR002', 'Bhavani S', 'IT', 3),
('24ITR003', 'Chandru M', 'IT', 3),
('24ITR004', 'Deepika V', 'IT', 3),
('24CSR015', 'Gokul R', 'CSE', 3),
('24ECR008', 'Harini P', 'ECE', 3);

-- 2. Insert Sample Courses (R2024 Curriculum: Theory, Practical, Embedded)
INSERT INTO courses (course_code, title, credits, course_type, department, semester) VALUES
('24ITT31', 'Database Management Systems', 3.0, 'THEORY', 'IT', 3),
('24ITP32', 'Object Oriented Programming with Java Lab', 1.5, 'PRACTICAL', 'IT', 3),
('24ITE33', 'Full Stack Web Development', 4.0, 'EMBEDDED', 'IT', 3),
('24CST31', 'Data Structures and Algorithms', 3.0, 'THEORY', 'CSE', 3),
('24ECT31', 'Digital Electronics & Logic Design', 3.0, 'THEORY', 'ECE', 3);

-- 3. Insert Enrollments with diverse attendance profiles
-- Aadhavan: High attendance (>75%) across all subjects
INSERT INTO enrollments (enrollment_id, roll_no, course_code, semester, classes_held, classes_attended, medical_condonation) VALUES
(1, '24ITR001', '24ITT31', 3, 50, 46, FALSE), -- 92% -> Eligible
(2, '24ITR001', '24ITP32', 3, 30, 28, FALSE), -- 93.3% -> Eligible
(3, '24ITR001', '24ITE33', 3, 60, 54, FALSE); -- 90% -> Eligible

-- Bhavani: One course conditionally eligible with Medical Condonation approved (70%)
INSERT INTO enrollments (enrollment_id, roll_no, course_code, semester, classes_held, classes_attended, medical_condonation, condonation_reason) VALUES
(4, '24ITR002', '24ITT31', 3, 50, 43, FALSE, NULL),                     -- 86% -> Eligible
(5, '24ITR002', '24ITP32', 3, 30, 21, TRUE, 'Hospitalized with Dengue'),  -- 70% -> Approved Condonation -> Eligible
(6, '24ITR002', '24ITE33', 3, 60, 49, FALSE, NULL);                     -- 81.6% -> Eligible

-- Chandru: Shortage of attendance (< 65%) in one course, and without condonation (68%) in another
INSERT INTO enrollments (enrollment_id, roll_no, course_code, semester, classes_held, classes_attended, medical_condonation) VALUES
(7, '24ITR003', '24ITT31', 3, 50, 28, FALSE), -- 56% -> Barred (SA)
(8, '24ITR003', '24ITP32', 3, 30, 20, FALSE), -- 66.7% No condonation -> Barred (SA)
(9, '24ITR003', '24ITE33', 3, 60, 48, FALSE); -- 80% -> Eligible

-- Deepika: Normal attendance, but Absent (AB) in one ESE and RA in another
INSERT INTO enrollments (enrollment_id, roll_no, course_code, semester, classes_held, classes_attended, medical_condonation) VALUES
(10, '24ITR004', '24ITT31', 3, 50, 42, FALSE), -- 84% -> Eligible
(11, '24ITR004', '24ITP32', 3, 30, 26, FALSE), -- 86.6% -> Eligible
(12, '24ITR004', '24ITE33', 3, 60, 50, FALSE); -- 83.3% -> Eligible

-- Gokul (CSE) & Harini (ECE)
INSERT INTO enrollments (enrollment_id, roll_no, course_code, semester, classes_held, classes_attended, medical_condonation) VALUES
(13, '24CSR015', '24CST31', 3, 50, 44, FALSE),
(14, '24ECR008', '24ECT31', 3, 50, 41, FALSE);

-- 4. Insert Assessment Marks illustrating phased evaluation & all grading outcomes
-- Aadhavan (24ITR001): Outstanding student (O / A+ grades)
-- 24ITT31 (Theory): CIA1=92, CIA2=96, Ass=90 -> CIA(100)=92.8 -> Scaled(40%)=37.12. ESE=95 -> Scaled(60%)=57.0. Total=94.12 -> 'O' (GP 10)
INSERT INTO assessment_marks (enrollment_id, cia_test1, cia_test2, assignment_quiz_lab, ese_marks, is_ese_absent, computed_cia, computed_ese, total_marks, letter_grade, grade_point, result_status) VALUES
(1, 92.00, 96.00, 90.00, 95.00, FALSE, 37.12, 57.00, 94.12, 'O', 10, 'PASS'),
-- 24ITP32 (Practical): CIA1=88, CIA2=92, Ass=94 -> CIA(100)=91.2 -> Scaled(60%)=54.72. ESE=86 -> Scaled(40%)=34.40. Total=89.12 -> 'A+' (GP 9)
(2, 88.00, 92.00, 94.00, 86.00, FALSE, 54.72, 34.40, 89.12, 'A+', 9, 'PASS'),
-- 24ITE33 (Embedded): CIA1=85, CIA2=87, Ass=88 -> CIA(100)=86.6 -> Scaled(50%)=43.30. ESE=84 -> Scaled(50%)=42.00. Total=85.30 -> 'A+' (GP 9)
(3, 85.00, 87.00, 88.00, 84.00, FALSE, 43.30, 42.00, 85.30, 'A+', 9, 'PASS');

-- Bhavani (24ITR002): Good student (A / B+ grades, medical condonation approved for lab)
-- 24ITT31 (Theory): Total = 76.5 -> 'A' (GP 8)
INSERT INTO assessment_marks (enrollment_id, cia_test1, cia_test2, assignment_quiz_lab, ese_marks, is_ese_absent, computed_cia, computed_ese, total_marks, letter_grade, grade_point, result_status) VALUES
(4, 75.00, 78.00, 80.00, 76.00, FALSE, 31.02, 45.60, 76.62, 'A', 8, 'PASS'),
-- 24ITP32 (Practical): Condonation granted -> Total = 68.4 -> 'B+' (GP 7)
(5, 70.00, 68.00, 74.00, 66.00, FALSE, 42.30, 26.40, 68.70, 'B+', 7, 'PASS'),
-- 24ITE33 (Embedded): Total = 74.2 -> 'A' (GP 8)
(6, 74.00, 76.00, 75.00, 72.00, FALSE, 37.50, 36.00, 73.50, 'A', 8, 'PASS');

-- Chandru (24ITR003): Attendance Defaulter (SA in 2 courses), Passed 1
INSERT INTO assessment_marks (enrollment_id, cia_test1, cia_test2, assignment_quiz_lab, ese_marks, is_ese_absent, computed_cia, computed_ese, total_marks, letter_grade, grade_point, result_status) VALUES
(7, 50.00, 52.00, 45.00, NULL, FALSE, 20.08, 0.00, 20.08, 'SA', 0, 'SHORTAGE_ATTENDANCE'),
(8, 48.00, 50.00, 50.00, NULL, FALSE, 29.58, 0.00, 29.58, 'SA', 0, 'SHORTAGE_ATTENDANCE'),
(9, 62.00, 65.00, 68.00, 60.00, FALSE, 32.42, 30.00, 62.42, 'B+', 7, 'PASS');

-- Deepika (24ITR004): 1 Absent, 1 Failed ESE threshold (RA), 1 Pass
-- 24ITT31 (Theory): ESE = 38 < 45 threshold! Even though total might be > 50, fails ESE min -> 'RA' (GP 0)
INSERT INTO assessment_marks (enrollment_id, cia_test1, cia_test2, assignment_quiz_lab, ese_marks, is_ese_absent, computed_cia, computed_ese, total_marks, letter_grade, grade_point, result_status) VALUES
(10, 80.00, 82.00, 85.00, 38.00, FALSE, 32.88, 22.80, 55.68, 'RA', 0, 'FAIL_ESE_MINIMUM'),
-- 24ITP32 (Practical): Absent in ESE exam -> 'AB' (GP 0)
(11, 75.00, 70.00, 78.00, NULL, TRUE, 44.49, 0.00, 44.49, 'AB', 0, 'ABSENT'),
-- 24ITE33 (Embedded): Pass -> 'B' (GP 6)
(12, 55.00, 58.00, 60.00, 56.00, FALSE, 28.78, 28.00, 56.78, 'B', 6, 'PASS');

-- Gokul & Harini
INSERT INTO assessment_marks (enrollment_id, cia_test1, cia_test2, assignment_quiz_lab, ese_marks, is_ese_absent, computed_cia, computed_ese, total_marks, letter_grade, grade_point, result_status) VALUES
(13, 85.00, 88.00, 90.00, 82.00, FALSE, 35.02, 49.20, 84.22, 'A+', 9, 'PASS'),
(14, 78.00, 82.00, 80.00, 75.00, FALSE, 32.00, 45.00, 77.00, 'A', 8, 'PASS');
