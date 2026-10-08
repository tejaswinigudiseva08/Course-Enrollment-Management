package com.courseenrollmentmanagement;

public class Student extends User {

    private final String department;

    public Student(int id, String name, String department) {
        super(id, name);
        if (id < 1) {
            throw new IllegalArgumentException("Student ID must be 1 or more.");
        }
        this.department = requireText(department, "Department");
    }

    public String getDepartment() {
        return department;
    }

    @Override
    public void showProfile() {
        System.out.println("Student ID: " + getId());
        System.out.println("Name: " + getName());
        System.out.println("Department: " + department);
    }
}