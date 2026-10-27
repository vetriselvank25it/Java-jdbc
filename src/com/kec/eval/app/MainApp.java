package com.kec.eval.app;
import com.kec.eval.dao.*; import com.kec.eval.db.DBConnection; import com.kec.eval.model.*; import java.util.*;
public class MainApp {
  static Scanner sc = new Scanner(System.in);
  static StudentDAO sDao = new StudentDAO();
  static AcademicDAO aDao = new AcademicDAO();
  public static void main(String[] a) {
    DBConnection.initializeDatabase();
    while (true) {
      System.out.println("\n=== KEC (R2024) EVALUATION SYSTEM ===");
      System.out.println("1.Add Student 2.Add Course 3.Enroll 4.Attendance 5.Marks (P1/2/3) 6.MarkSheet 7.Defaulters 8.Summary 9.Exit");
      System.out.print("Choice: ");
      switch (sc.nextLine().trim()) {
        case "1" -> addStudent();
        case "2" -> addCourse();
        case "3" -> enroll();
        case "4" -> attendance();
        case "5" -> marks();
        case "6" -> markSheet();
        case "7" -> defaulters();
        case "8" -> summary();
        case "9" -> { return; }
        default -> System.out.println("Invalid!");
      }
    }
  }
  static void addStudent() {
    System.out.print("RollNo Name Dept Sem: "); String[] p = sc.nextLine().trim().split("\\s+");
    System.out.println(sDao.addStudent(new Student(p[0], p[1], p[2], Integer.parseInt(p[3]))) ? "Added!" : "Failed!");
  }
  static void addCourse() {
    System.out.print("Code Title Credits Type(THEORY/PRACTICAL/EMBEDDED) Dept Sem: "); String[] p = sc.nextLine().trim().split("\\s+");
    System.out.println(aDao.addCourse(new Course(p[0], p[1], Double.parseDouble(p[2]), CourseType.valueOf(p[3].toUpperCase()), p[4], Integer.parseInt(p[5]))) ? "Added!" : "Failed!");
  }
  static void enroll() {
    System.out.print("RollNo CourseCode Sem Held Attended: "); String[] p = sc.nextLine().trim().split("\\s+");
    int id = aDao.enrollStudent(p[0], p[1], Integer.parseInt(p[2]), Integer.parseInt(p[3]), Integer.parseInt(p[4]));
    System.out.println(id > 0 ? "Enrolled ID: " + id : "Failed!");
  }
  static void attendance() {
    System.out.print("EnrollID 1.Hours(Held Attn) 2.Condonation(Y/N Reason): "); String[] p = sc.nextLine().trim().split("\\s+", 3);
    int id = Integer.parseInt(p[0]);
    if (p[1].equals("1")) { String[] h = p[2].split("\\s+"); System.out.println(aDao.updateAttendance(id, Integer.parseInt(h[0]), Integer.parseInt(h[1])) ? "Updated!" : "Failed!"); }
    else { String[] c = p[2].split("\\s+", 2); System.out.println(aDao.setMedicalCondonation(id, c[0].equalsIgnoreCase("Y"), c.length > 1 ? c[1] : "") ? "Updated!" : "Failed!"); }
  }
  static void marks() {
    System.out.print("EnrollID Phase(1.CIA1 CIA2 | 2.Assig | 3.ESE/AB): "); String[] p = sc.nextLine().trim().split("\\s+");
    int id = Integer.parseInt(p[0]);
    if (p[1].equals("1")) System.out.println(aDao.recordPhase1Marks(id, Double.parseDouble(p[2]), Double.parseDouble(p[3])) ? "P1 Saved!" : "Failed!");
    else if (p[1].equals("2")) System.out.println(aDao.recordPhase2Marks(id, Double.parseDouble(p[2])) ? "P2 Saved!" : "Failed!");
    else if (p[1].equals("3")) {
      Enrollment e = aDao.getEnrollment(id);
      if (e != null && !e.isEligibleForESE()) { System.out.println("Barred from ESE (SA)! " + e.getAttendancePercentage() + "%"); aDao.recalculateAndPersistEvaluation(id); return; }
      boolean ab = p[2].equalsIgnoreCase("AB");
      System.out.println(aDao.recordPhase3Marks(id, ab ? null : Double.parseDouble(p[2]), ab) ? "P3 Saved!" : "Failed!");
    }
  }
  static void markSheet() {
    System.out.print("RollNo Sem: "); String[] p = sc.nextLine().trim().split("\\s+");
    StudentSemesterResult r = aDao.generateStudentSemesterMarkSheet(p[0], Integer.parseInt(p[1]));
    if (r == null || r.getStudent() == null) { System.out.println("Not found!"); return; }
    System.out.printf("--- GRADE SHEET: %s (%s) Sem:%s ---\n", r.getStudent().getName(), p[0], p[1]);
    System.out.printf("%-8s %-20s %-4s %-6s %-6s %-6s %-5s %-2s\n", "Course", "Title", "Cred", "Att%", "CIA", "ESE", "Grade", "GP");
    for (CourseResult c : r.getCourseResults()) {
      String ese = c.isEseAbsent() ? "AB" : (c.getScaledEse() != null ? "" + c.getScaledEse() : "-");
      System.out.printf("%-8s %-20s %-4.1f %-5.1f%% %-6s %-6s %-5s %-2d\n", c.getCourseCode(), c.getCourseTitle(), c.getCredits(), c.getAttendancePercentage(), c.getScaledCia(), ese, c.getLetterGrade(), c.getGradePoint());
    }
    System.out.printf("Credits: %.1f | SGPA: %.2f | Percentage: %.2f%% | Result: %s\n", r.getTotalEnrolledCredits(), r.getSgpa(), r.getEquivalentPercentage(), r.getSemesterStatus());
  }
  static void defaulters() {
    System.out.print("Semester (0 for All): "); int s = Integer.parseInt(sc.nextLine().trim());
    for (Enrollment e : aDao.getAttendanceDefaulters(s == 0 ? null : s))
      System.out.printf("%-10s %-15s %-8s %d/%d (%.1f%%) -> BARRED (SA)\n", e.getRollNo(), e.getStudentName(), e.getCourseCode(), e.getClassesAttended(), e.getClassesHeld(), e.getAttendancePercentage());
  }
  static void summary() {
    System.out.print("Semester: "); int sem = Integer.parseInt(sc.nextLine().trim());
    for (ClassSummary c : aDao.getClassPerformanceSummary(sem))
      System.out.printf("%-6s | Total:%-3d | Pass:%-3d | RA:%-2d | SA:%-2d | Pass:%.1f%% | Avg SGPA:%.2f\n", c.getDepartment(), c.getTotalStudents(), c.getPassedEnrollments(), c.getRaEnrollments(), c.getSaEnrollments(), c.getPassPercentage(), c.getAverageSgpa());
  }
}