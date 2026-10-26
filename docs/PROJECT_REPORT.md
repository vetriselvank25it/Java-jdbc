# PROJECT EXECUTION REPORT
## Kongu Engineering College (Autonomous), Perundurai - 638060
### Regulation 2024 (R2024) Student Academic & Continuous Evaluation System
**Department:** Information Technology / Computer Science and Engineering  
**Technology Stack:** Java (JDK 26), JDBC 4.2, MariaDB 10.4 / MySQL, PowerShell, Windows 11  

---

## 1. Objective & Scope
The objective of this project is to develop a database-driven Java application utilizing JDBC to manage student academic records, implement phased continuous internal assessments ("Then and Now"), enforce statutory attendance eligibility criteria including medical condonation, and generate formal academic performance reports adhering strictly to **Kongu Engineering College (Autonomous) - Regulation 2024 (R2024)**.

---

## 2. System Architecture & Database Design

### 2.1 Entity Relationship Diagram (ERD)
```
 +------------------------+             +-------------------------+
 |        STUDENTS        |             |         COURSES         |
 +------------------------+             +-------------------------+
 | PK  roll_no (VARCHAR)  | 1         1 | PK  course_code (VARCHAR|
 |     name (VARCHAR)     |             |     title (VARCHAR)     |
 |     department (VAR)   |             |     credits (DECIMAL)   |
 |     current_semester   |             |     course_type (ENUM)  |
 +-----------+------------+             +------------+------------+
             |                                       |
             | 1                                     | 1
             |                                       |
             +------------------+  +-----------------+
                                |  |
                                v  v *
                     +---------------------------+
                     |        ENROLLMENTS        |
                     +---------------------------+
                     | PK  enrollment_id (INT)   |
                     | FK  roll_no (VARCHAR)     |
                     | FK  course_code (VARCHAR) |
                     |     semester (INT)        |
                     |     classes_held (INT)    |
                     |     classes_attended (INT)|
                     |     medical_condonation   |
                     +-------------+-------------+
                                   | 1
                                   |
                                   v 1
                     +---------------------------+
                     |     ASSESSMENT_MARKS      |
                     +---------------------------+
                     | PK  mark_id (INT)         |
                     | FK  enrollment_id (INT)   |
                     |     cia_test1 (DECIMAL)   |
                     |     cia_test2 (DECIMAL)   |
                     |     assignment_quiz_lab   |
                     |     ese_marks (DECIMAL)   |
                     |     is_ese_absent (BOOL)  |
                     |     computed_cia (DEC)    |
                     |     computed_ese (DEC)    |
                     |     total_marks (DECIMAL) |
                     |     letter_grade (VARCHAR)|
                     |     grade_point (INT)     |
                     |     result_status (VAR)   |
                     +---------------------------+
```

---

## 3. Kongu Engineering College (R2024) Regulation Engine

### 3.1 Course Weightage Engine
Kongu Engineering College Regulation 2024 classifies courses into three distinct categories with custom continuous internal assessment (CIA) and end semester examination (ESE) distributions:

1. **THEORY COURSES**:
   - $\text{CIA Weightage} = 40\%$
   - $\text{ESE Weightage} = 60\%$
   - $\text{ESE Minimum Threshold} = 45\%$ ($27.0 / 60$ scaled or $45 / 100$ raw)
   - $\text{Overall Pass Minimum} = 50.0$ Marks

2. **PRACTICAL COURSES**:
   - $\text{CIA Weightage} = 60\%$
   - $\text{ESE Weightage} = 40\%$
   - $\text{ESE Minimum Threshold} = 50\%$ ($20.0 / 40$ scaled or $50 / 100$ raw)
   - $\text{Overall Pass Minimum} = 50.0$ Marks

3. **EMBEDDED COURSES** (Integrated Theory & Lab):
   - $\text{CIA Weightage} = 50\%$
   - $\text{ESE Weightage} = 50\%$
   - $\text{ESE Minimum Threshold} = 45\%$ ($22.5 / 50$ scaled or $45 / 100$ raw)
   - $\text{Overall Pass Minimum} = 50.0$ Marks

### 3.2 Phased Assessment Workflow ("Then and Now")
Assessment marks entry is decoupled into 3 distinct phases to simulate real-world academic semesters:
- **Phase 1 (Mid-Semester)**: Evaluates and records `cia_test1` (0–100) and `cia_test2` (0–100).
- **Phase 2 (End-of-Semester CIA)**: Evaluates and records `assignment_quiz_lab` (0–100).
- **Phase 3 (Post Semester ESE)**: Records `ese_marks` (0–100) and checks absentee status (`is_ese_absent`).

$$\text{CIA}_{100} = (0.35 \times \text{CIA}_1) + (0.35 \times \text{CIA}_2) + (0.30 \times \text{Assignment})$$
$$\text{Scaled CIA} = \text{CIA}_{100} \times \left(\frac{\text{CIA Weightage}}{100}\right)$$
$$\text{Scaled ESE} = \text{ESE}_{100} \times \left(\frac{\text{ESE Weightage}}{100}\right)$$
$$\text{Total Marks} = \text{Scaled CIA} + \text{Scaled ESE}$$

### 3.3 Attendance Eligibility Rules
- **$\ge 75.0\%$**: Eligible for ESE.
- **$65.0\% - 74.9\%$**: Conditionally eligible. Requires approved Medical Condonation. If approved, the student is permitted to sit for ESE. Otherwise, barred and awarded **SA** grade.
- **$< 65.0\%$**: Shortage of Attendance. Barred from ESE, awarded **SA** grade with 0 grade points.

### 3.4 10-Point Absolute Grading System
- **O (91 - 100)**: Grade Point 10 (Outstanding)
- **A+ (81 - 90)**: Grade Point 9 (Excellent)
- **A (71 - 80)**: Grade Point 8 (Very Good)
- **B+ (61 - 70)**: Grade Point 7 (Good)
- **B (50 - 60)**: Grade Point 6 (Average)
- **RA**: Re-Appearance (Failed ESE minimum threshold or aggregate $< 50$) $\to$ Grade Point 0
- **SA**: Shortage of Attendance $\to$ Grade Point 0
- **AB**: Absent in ESE $\to$ Grade Point 0

### 3.5 SGPA and Percentage Formulas
$$\text{SGPA} = \frac{\sum (\text{Course Credits} \times \text{Grade Points Earned})}{\sum \text{Total Enrolled Credits}}$$
$$\text{Equivalent Percentage} = \text{SGPA} \times 10$$

---

## 4. Verification Proofs & Execution Screenshots

### Proof 1: Student Registration & Course Catalog Management
The system successfully registers students with Department and Semester, and courses with credit weightages and course categories (THEORY, PRACTICAL, EMBEDDED).

![Screenshot 1 - Student and Course Registration](screenshots/1_student_course_registration.jpg)

### Proof 2: Incremental Marks Entry ("Then and Now")
Phased continuous internal assessments entered incrementally across Phase 1 (CIA 1 & CIA 2), Phase 2 (Assignments & Lab), and Phase 3 (End Semester Examination & Absentee check).

![Screenshot 2 - Incremental Marks Entry](screenshots/2_incremental_marks_entry.jpg)

### Proof 3: Official KEC R2024 Grade Sheet with SGPA & Equivalent Percentage
Student Grade Sheet with scaled CIA, scaled ESE, overall totals, letter grades, GP, SGPA (9.53 / 10.00), and Equivalent Percentage (95.30%).

![Screenshot 3 - Grade Sheet and SGPA Calculation](screenshots/3_grade_sheet_sgpa.jpg)

---

## 5. Formal Reporting Outputs

### 5.1 Sample Grade Sheet Output (Terminal Format)
```
============================================================================================================
                       KONGU ENGINEERING COLLEGE (AUTONOMOUS)                                               
                             OFFICE OF CONTROLLER OF EXAMINATIONS                                           
                     REGULATION 2024 (R2024) - GRADE SHEET / MARK STATEMENT                                 
============================================================================================================
 Roll Number  : 24ITR099        | Name       : Vijay Anand S                      
 Department   : IT              | Semester   : 3     | Academic Batch: 2024-2028
------------------------------------------------------------------------------------------------------------
Course    | Course Title                     | Type      | Cred | Att(%) | CIA Mark  | ESE Mark  | Total  | Grade | GP  | Result    
------------------------------------------------------------------------------------------------------------
24ITE34   | Cloud Computing and DevOps       | EMBEDDED  | 4.0  | 90.0 % | 45.0/50   | 43.0/50   | 88.0   | A+    | 9   | PASS      
24ITP32   | Object Oriented Programming Lab  | PRACTICAL | 1.5  | 90.0 % | 57.5/60   | 36.0/40   | 93.5   | O     | 10  | PASS      
24ITT31   | Database Management Systems      | THEORY    | 3.0  | 94.0 % | 36.9/40   | 55.2/60   | 92.1   | O     | 10  | PASS      
============================================================================================================
 Total Registered Credits : 8.5    | Total Credits Earned : 8.5   
 SEMESTER GRADE POINT AVERAGE (SGPA) :  9.53 / 10.00
 EQUIVALENT PERCENTAGE               : 95.30 %
 OVERALL SEMESTER RESULT             : ALL COURSES PASSED
============================================================================================================
```

### 5.2 Attendance Defaulter Report (Grade SA)
```
--------------------------------------------------------------------------------------------------------------
Roll No    | Student Name       | Dept  | Course   | Course Title                 | Held  | Attn  | Attn %  | Action/Status
--------------------------------------------------------------------------------------------------------------
24ITR003   | Chandru M          | IT    | 24ITT31  | Database Management Systems  | 50    | 28    | 56.0  % | Barred (<65% SA)
24ITR003   | Chandru M          | IT    | 24ITP32  | Object Oriented Java Lab     | 30    | 20    | 66.7  % | No Condonation (SA)
--------------------------------------------------------------------------------------------------------------
```

### 5.3 Class Performance & Department Result Summary
```
------------------------------------------------------------------------------------------------------
Dept     | Students | Enrollments  | Passed   | RA(Arrear) | SA     | AB     | Pass %     | Avg SGPA  
------------------------------------------------------------------------------------------------------
CSE      | 1        | 1            | 1        | 0        | 0      | 0      | 100.00   % | 9.00      
ECE      | 1        | 1            | 1        | 0        | 0      | 0      | 100.00   % | 8.00      
IT       | 5        | 15           | 11       | 1        | 2      | 1      | 73.33    % | 6.56      
------------------------------------------------------------------------------------------------------
```

---

## 6. Conclusion
The application meets and exceeds all specifications outlined in Kongu Engineering College Regulation 2024. All database tables, JDBC DAOs, weightage calculations, attendance filter thresholds, and academic reporting mechanisms function with 100% precision and compliance.
