package com.kec.eval.model;

import java.sql.Timestamp;

/**
 * Model representing a student at Kongu Engineering College.
 */
public class Student {
    private String rollNo;
    private String name;
    private String department;
    private int currentSemester;
    private Timestamp createdAt;

    public Student() {
    }

    public Student(String rollNo, String name, String department, int currentSemester) {
        this.rollNo = rollNo;
        this.name = name;
        this.department = department;
        this.currentSemester = currentSemester;
    }

    public Student(String rollNo, String name, String department, int currentSemester, Timestamp createdAt) {
        this.rollNo = rollNo;
        this.name = name;
        this.department = department;
        this.currentSemester = currentSemester;
        this.createdAt = createdAt;
    }

    public String getRollNo() {
        return rollNo;
    }

    public void setRollNo(String rollNo) {
        this.rollNo = rollNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getCurrentSemester() {
        return currentSemester;
    }

    public void setCurrentSemester(int currentSemester) {
        this.currentSemester = currentSemester;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return String.format("[%s] %-20s | Dept: %-5s | Sem: %d", rollNo, name, department, currentSemester);
    }
}
