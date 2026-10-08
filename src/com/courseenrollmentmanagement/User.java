package com.courseenrollmentmanagement;

public abstract class User {

    private final int id;
    private final String name;

    public User(int id, String name) {
        this.id = id;
        this.name = requireText(name, "Name");
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }


    public abstract void showProfile();


    protected static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        if (value.contains("|") || value.contains("\n") || value.contains("\r")) {
            throw new IllegalArgumentException(fieldName + " cannot contain '|' or line breaks.");
        }
        return value.trim();
    }
}
