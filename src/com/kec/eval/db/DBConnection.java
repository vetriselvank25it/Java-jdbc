package com.kec.eval.db;
import java.sql.*;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/kec_eval_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASS = "";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver error: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    public static boolean initializeDatabase() {
        try (Connection conn = getConnection(); Statement s = conn.createStatement()) {
            s.executeUpdate("CREATE TABLE IF NOT EXISTS students ("
                    + "roll_no VARCHAR(20) PRIMARY KEY, name VARCHAR(100), department VARCHAR(20), current_semester INT)");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS courses ("
                    + "course_code VARCHAR(20) PRIMARY KEY, title VARCHAR(150), credits DECIMAL(3,1), course_type VARCHAR(20), department VARCHAR(20), semester INT)");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS enrollments ("
                    + "enrollment_id INT AUTO_INCREMENT PRIMARY KEY, roll_no VARCHAR(20), course_code VARCHAR(20), semester INT, classes_held INT DEFAULT 0, classes_attended INT DEFAULT 0, medical_condonation BOOLEAN DEFAULT FALSE, condonation_reason VARCHAR(255), "
                    + "FOREIGN KEY (roll_no) REFERENCES students(roll_no) ON DELETE CASCADE, "
                    + "FOREIGN KEY (course_code) REFERENCES courses(course_code) ON DELETE CASCADE, "
                    + "UNIQUE (roll_no, course_code, semester))");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS assessment_marks ("
                    + "mark_id INT AUTO_INCREMENT PRIMARY KEY, enrollment_id INT UNIQUE, cia_test1 DECIMAL(5,2), cia_test2 DECIMAL(5,2), assignment_quiz_lab DECIMAL(5,2), ese_marks DECIMAL(5,2), is_ese_absent BOOLEAN DEFAULT FALSE, "
                    + "computed_cia DECIMAL(5,2), computed_ese DECIMAL(5,2), total_marks DECIMAL(5,2), letter_grade VARCHAR(5), grade_point INT, result_status VARCHAR(30), "
                    + "FOREIGN KEY (enrollment_id) REFERENCES enrollments(enrollment_id) ON DELETE CASCADE)");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void closeQuietly(AutoCloseable c) {
        if (c != null) try { c.close(); } catch (Exception ignored) {}
    }
}