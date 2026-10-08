package com.courseenrollmentmanagement;
import java.io.IOException;
import java.util.Scanner;

public class Main {

    // The ONE Scanner for the whole program (document rule).
    private static final Scanner scanner = new Scanner(System.in);

    private static final Admin admin = new Admin();
    private static CourseService service;

    public static void main(String[] args) {
        service = new CourseService(new FileManager());

        // Load saved data. If any file is invalid, show the problem and stop.
        try {
            service.loadData();
        } catch (IOException e) {
            System.out.println("Cannot start the program. " + e.getMessage());
            System.out.println("Please correct the file in the data folder and run again.");
            scanner.close();
            return;
        }

        boolean running = true;

        while (running) {
            System.out.println();
            System.out.println("MAIN MENU");
            System.out.println("1. Register student");
            System.out.println("2. Admin menu");
            System.out.println("3. Student menu");
            System.out.println("0. Exit");

            int choice = readChoice(3);

            switch (choice) {
                case 1:
                    registerStudent();
                    break;
                case 2:
                    showAdminMenu();
                    break;
                case 3:
                    showStudentMenu();
                    break;
                case 0:
                    System.out.println("Goodbye!");
                    running = false;
                    break;
            }
        }

        scanner.close();
    }

    // ---------- Main menu action ----------

    private static void registerStudent() {
        String name = readRequiredText("Enter student name: ");
        String department = readRequiredText("Enter department: ");

        try {
            Student student = service.registerStudent(name, department);
            System.out.println("Student registered. Your student ID is " + student.getId());
        } catch (IOException | IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    // ---------- Admin menu ----------

    public static void showAdminMenu() {
        admin.showProfile();
        boolean inAdminMenu = true;

        while (inAdminMenu) {
            System.out.println();
            System.out.println("ADMIN MENU");
            System.out.println("1. Add course");
            System.out.println("2. View courses");
            System.out.println("3. View students");
            System.out.println("4. View enrollments");
            System.out.println("5. View reports");
            System.out.println("0. Back");

            int choice = readChoice(5);

            switch (choice) {
                case 1:
                    addCourse();
                    break;
                case 2:
                    viewCourses();
                    break;
                case 3:
                    service.showStudents();
                    break;
                case 4:
                    service.showEnrollments();
                    break;
                case 5:
                    viewReports();
                    break;
                case 0:
                    inAdminMenu = false;
                    break;
            }
        }
    }

    private static void addCourse() {
        String title = readRequiredText("Enter course title: ");
        String trainer = readRequiredText("Enter trainer name: ");

        // The batch array: show the choices, check the number, then use the array.
        String[] batches = Course.getBatches();
        System.out.println("Choose a batch:");
        for (int i = 0; i < batches.length; i++) {
            System.out.println((i + 1) + ". " + batches[i]);
        }
        int batchChoice = readIntInRange("Enter batch choice: ", 1, batches.length);
        String selectedBatch = batches[batchChoice - 1];

        int capacity = readIntInRange("Enter capacity (" + Course.MIN_CAPACITY + " to "
                + Course.MAX_CAPACITY + "): ", Course.MIN_CAPACITY, Course.MAX_CAPACITY);

        try {
            Course course = service.addCourse(title, trainer, selectedBatch, capacity);
            System.out.println("Course " + course.getId() + " saved.");
        } catch (IOException | IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    // Shared by the Admin and Student menus. The batch array filters the list.
    private static void viewCourses() {
        String[] batches = Course.getBatches();
        System.out.println("Filter courses by batch:");
        System.out.println("0. All batches");
        for (int i = 0; i < batches.length; i++) {
            System.out.println((i + 1) + ". " + batches[i]);
        }
        int choice = readIntInRange("Enter choice: ", 0, batches.length);

        if (choice == 0) {
            service.showCourses(null);
        } else {
            service.showCourses(batches[choice - 1]);
        }
    }

    private static void viewReports() {
        service.showReport();

        if (readYesNo("Save report? (yes/no): ")) {
            try {
                service.saveReport();
                System.out.println("Report saved to data/report.txt");
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    // ---------- Student menu ----------

    public static void showStudentMenu() {
        Student student = null;
        while (student == null) {
            int id = readWholeNumber("Enter student ID (0 to go back): ");
            if (id == 0) {
                return;
            }
            student = service.findStudentById(id);
            if (student == null) {
                System.out.println("Student ID " + id + " not found. Please try again.");
            }
        }

        int studentId = student.getId();
        student.showProfile();
        boolean inStudentMenu = true;

        while (inStudentMenu) {
            System.out.println();
            System.out.println("STUDENT MENU");
            System.out.println("1. View courses");
            System.out.println("2. Enroll in course");
            System.out.println("3. Cancel my enrollment");
            System.out.println("4. View my enrollment history");
            System.out.println("0. Back");

            int choice = readChoice(4);

            switch (choice) {
                case 1:
                    viewCourses();
                    break;
                case 2:
                    enrollInCourse(studentId);
                    break;
                case 3:
                    cancelMyEnrollment(studentId);
                    break;
                case 4:
                    service.showEnrollmentHistory(studentId);
                    break;
                case 0:
                    inStudentMenu = false;
                    break;
            }
        }
    }

    private static void enrollInCourse(int studentId) {
        int courseId = readPositiveId("Enroll in course ID: ");

        try {
            Enrollment enrollment = service.enroll(studentId, courseId);
            System.out.println("Enrollment " + enrollment.getId()
                    + " saved. Status: " + enrollment.getStatus());
            Course course = service.findCourseById(courseId);
            System.out.println("Free seats in course " + courseId + ": "
                    + service.getFreeSeats(course));
        } catch (IOException | IllegalArgumentException | IllegalStateException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void cancelMyEnrollment(int studentId) {
        System.out.println("Your enrollments:");
        service.showEnrollmentHistory(studentId);

        int enrollmentId = readPositiveId("Cancel my enrollment ID: ");

        try {
            Enrollment enrollment = service.cancelEnrollment(studentId, enrollmentId);
            System.out.println("Enrollment " + enrollment.getId()
                    + " saved. Status: " + enrollment.getStatus());
            Course course = service.findCourseById(enrollment.getCourseId());
            System.out.println("Free seats in course " + enrollment.getCourseId() + ": "
                    + service.getFreeSeats(course));
        } catch (IOException | IllegalArgumentException | IllegalStateException e) {
            System.out.println(e.getMessage());
        }
    }

    // ---------- Input helpers (all use the ONE Scanner) ----------

    // Keeps asking until the user types a whole number.
    private static int readWholeNumber(String prompt) {
        while (true) {
            System.out.print(prompt);
            String text = scanner.nextLine().trim();
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a whole number.");
            }
        }
    }

    // Keeps asking until the number is from min to max.
    private static int readIntInRange(String prompt, int min, int max) {
        while (true) {
            int number = readWholeNumber(prompt);
            if (number >= min && number <= max) {
                return number;
            }
            System.out.println("Invalid choice. Please enter a number from " + min + " to " + max + ".");
        }
    }

    private static int readChoice(int max) {
        return readIntInRange("Enter choice: ", 0, max);
    }

    // Keeps asking until the ID is 1 or more.
    private static int readPositiveId(String prompt) {
        while (true) {
            int number = readWholeNumber(prompt);
            if (number >= 1) {
                return number;
            }
            System.out.println("ID must be 1 or more.");
        }
    }

    // Keeps asking until the text is not blank and has no '|'.
    private static String readRequiredText(String prompt) {
        while (true) {
            System.out.print(prompt);
            String text = scanner.nextLine().trim();
            if (text.isEmpty()) {
                System.out.println("This field is required.");
            } else if (text.contains("|")) {
                System.out.println("The '|' character is not allowed.");
            } else {
                return text;
            }
        }
    }

    private static boolean readYesNo(String prompt) {
        while (true) {
            System.out.print(prompt);
            String text = scanner.nextLine().trim().toLowerCase();
            if (text.equals("yes") || text.equals("y")) {
                return true;
            }
            if (text.equals("no") || text.equals("n")) {
                return false;
            }
            System.out.println("Please type yes or no.");
        }
    }
}