package com.kec.eval.dao;

import com.kec.eval.db.DBConnection;
import com.kec.eval.model.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Student Management.
 */
public class StudentDAO {

    public boolean addStudent(Student student) {
        String sql = "INSERT INTO students (roll_no, name, department, current_semester) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, student.getRollNo().trim().toUpperCase());
            ps.setString(2, student.getName().trim());
            ps.setString(3, student.getDepartment().trim().toUpperCase());
            ps.setInt(4, student.getCurrentSemester());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[StudentDAO] Error adding student: " + e.getMessage());
            return false;
        }
    }

    public Student getStudentByRollNo(String rollNo) {
        String sql = "SELECT roll_no, name, department, current_semester, created_at FROM students WHERE roll_no = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rollNo.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Student(
                            rs.getString("roll_no"),
                            rs.getString("name"),
                            rs.getString("department"),
                            rs.getInt("current_semester"),
                            rs.getTimestamp("created_at")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("[StudentDAO] Error fetching student: " + e.getMessage());
        }
        return null;
    }

    public List<Student> getAllStudents() {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT roll_no, name, department, current_semester, created_at FROM students ORDER BY department, roll_no";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Student(
                        rs.getString("roll_no"),
                        rs.getString("name"),
                        rs.getString("department"),
                        rs.getInt("current_semester"),
                        rs.getTimestamp("created_at")
                ));
            }
        } catch (SQLException e) {
            System.err.println("[StudentDAO] Error listing students: " + e.getMessage());
        }
        return list;
    }

    public List<Student> getStudentsByDepartment(String dept) {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT roll_no, name, department, current_semester, created_at FROM students WHERE department = ? ORDER BY roll_no";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dept.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Student(
                            rs.getString("roll_no"),
                            rs.getString("name"),
                            rs.getString("department"),
                            rs.getInt("current_semester"),
                            rs.getTimestamp("created_at")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("[StudentDAO] Error listing department students: " + e.getMessage());
        }
        return list;
    }

    public boolean updateStudent(Student student) {
        String sql = "UPDATE students SET name = ?, department = ?, current_semester = ? WHERE roll_no = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, student.getName().trim());
            ps.setString(2, student.getDepartment().trim().toUpperCase());
            ps.setInt(3, student.getCurrentSemester());
            ps.setString(4, student.getRollNo().trim().toUpperCase());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[StudentDAO] Error updating student: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteStudent(String rollNo) {
        String sql = "DELETE FROM students WHERE roll_no = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rollNo.trim().toUpperCase());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[StudentDAO] Error deleting student: " + e.getMessage());
            return false;
        }
    }
}
