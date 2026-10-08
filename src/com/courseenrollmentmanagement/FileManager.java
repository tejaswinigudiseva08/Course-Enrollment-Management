package com.courseenrollmentmanagement;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class FileManager {
    private static final Path DATA_FOLDER = Paths.get("data");
    private static final Path STUDENTS_FILE = DATA_FOLDER.resolve("students.txt");
    private static final Path COURSES_FILE = DATA_FOLDER.resolve("courses.txt");
    private static final Path ENROLLMENTS_FILE = DATA_FOLDER.resolve("enrollments.txt");
    private static final Path REPORT_FILE = DATA_FOLDER.resolve("report.txt");



    public void createDataFolder() throws IOException {
        Files.createDirectories(DATA_FOLDER);
    }


    public ArrayList<Student> loadStudents() throws IOException {
        ArrayList<Student> students = new ArrayList<>();
        HashSet<Integer> usedIds = new HashSet<>();
        List<String> lines = readLines(STUDENTS_FILE);

        for (int i = 0; i < lines.size(); i++) {
            int lineNumber = i + 1;
            String[] fields = lines.get(i).split("\\|", -1);
            if (fields.length != 3) {
                throw invalid(STUDENTS_FILE, lineNumber,
                        "expected 3 fields but found " + fields.length);
            }
            try {
                int id = Integer.parseInt(fields[0].trim());
                Student student = new Student(id, fields[1], fields[2]);
                if (!usedIds.add(id)) {
                    throw invalid(STUDENTS_FILE, lineNumber, "duplicate ID " + id);
                }
                students.add(student);
            } catch (NumberFormatException e) {
                throw invalid(STUDENTS_FILE, lineNumber, "ID is not a number");
            } catch (IllegalArgumentException e) {
                throw invalid(STUDENTS_FILE, lineNumber, e.getMessage());
            }
        }
        return students;
    }

    public ArrayList<Course> loadCourses() throws IOException {
        ArrayList<Course> courses = new ArrayList<>();
        HashSet<Integer> usedIds = new HashSet<>();
        List<String> lines = readLines(COURSES_FILE);

        for (int i = 0; i < lines.size(); i++) {
            int lineNumber = i + 1;
            String[] fields = lines.get(i).split("\\|", -1);
            if (fields.length != 5) {
                throw invalid(COURSES_FILE, lineNumber,
                        "expected 5 fields but found " + fields.length);
            }
            try {
                int id = Integer.parseInt(fields[0].trim());
                int capacity = Integer.parseInt(fields[4].trim());
                Course course = new Course(id, fields[1], fields[2], fields[3].trim(), capacity);
                if (!usedIds.add(id)) {
                    throw invalid(COURSES_FILE, lineNumber, "duplicate ID " + id);
                }
                courses.add(course);
            } catch (NumberFormatException e) {
                throw invalid(COURSES_FILE, lineNumber, "ID or capacity is not a number");
            } catch (IllegalArgumentException e) {
                throw invalid(COURSES_FILE, lineNumber, e.getMessage());
            }
        }
        return courses;
    }


    public ArrayList<Enrollment> loadEnrollments(List<Student> students, List<Course> courses)
            throws IOException {
        ArrayList<Enrollment> enrollments = new ArrayList<>();
        HashSet<Integer> usedIds = new HashSet<>();
        HashSet<String> activePairs = new HashSet<>();
        List<String> lines = readLines(ENROLLMENTS_FILE);

        for (int i = 0; i < lines.size(); i++) {
            int lineNumber = i + 1;
            String[] fields = lines.get(i).split("\\|", -1);
            if (fields.length != 4) {
                throw invalid(ENROLLMENTS_FILE, lineNumber,
                        "expected 4 fields but found " + fields.length);
            }

            int id;
            int studentId;
            int courseId;
            try {
                id = Integer.parseInt(fields[0].trim());
                studentId = Integer.parseInt(fields[1].trim());
                courseId = Integer.parseInt(fields[2].trim());
            } catch (NumberFormatException e) {
                throw invalid(ENROLLMENTS_FILE, lineNumber, "an ID is not a number");
            }

            EnrollmentStatus status;
            try {
                status = EnrollmentStatus.valueOf(fields[3].trim());
            } catch (IllegalArgumentException e) {
                throw invalid(ENROLLMENTS_FILE, lineNumber,
                        "status must be ACTIVE or CANCELLED");
            }

            Enrollment enrollment;
            try {
                enrollment = new Enrollment(id, studentId, courseId, status);
            } catch (IllegalArgumentException e) {
                throw invalid(ENROLLMENTS_FILE, lineNumber, e.getMessage());
            }

            if (!usedIds.add(id)) {
                throw invalid(ENROLLMENTS_FILE, lineNumber, "duplicate ID " + id);
            }
            if (!studentExists(students, studentId)) {
                throw invalid(ENROLLMENTS_FILE, lineNumber,
                        "student ID " + studentId + " does not exist");
            }
            Course course = findCourse(courses, courseId);
            if (course == null) {
                throw invalid(ENROLLMENTS_FILE, lineNumber,
                        "course ID " + courseId + " does not exist");
            }

            if (status == EnrollmentStatus.ACTIVE) {
                String pair = studentId + "-" + courseId;
                if (!activePairs.add(pair)) {
                    throw invalid(ENROLLMENTS_FILE, lineNumber,
                            "duplicate ACTIVE enrollment for this student and course");
                }
                int activeSoFar = 0;
                for (Enrollment e : enrollments) {
                    if (e.getCourseId() == courseId && e.getStatus() == EnrollmentStatus.ACTIVE) {
                        activeSoFar++;
                    }
                }
                if (activeSoFar + 1 > course.getCapacity()) {
                    throw invalid(ENROLLMENTS_FILE, lineNumber,
                            "active enrollments are above the capacity of course " + courseId);
                }
            }

            enrollments.add(enrollment);
        }
        return enrollments;
    }



    public void saveStudents(List<Student> students) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Student s : students) {
            lines.add(s.getId() + "|" + s.getName() + "|" + s.getDepartment());
        }
        writeLinesSafely(STUDENTS_FILE, lines);
    }

    public void saveCourses(List<Course> courses) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Course c : courses) {
            lines.add(c.getId() + "|" + c.getTitle() + "|" + c.getTrainer()
                    + "|" + c.getBatch() + "|" + c.getCapacity());
        }
        writeLinesSafely(COURSES_FILE, lines);
    }

    public void saveEnrollments(List<Enrollment> enrollments) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Enrollment e : enrollments) {
            lines.add(e.getId() + "|" + e.getStudentId() + "|" + e.getCourseId()
                    + "|" + e.getStatus().name());
        }
        writeLinesSafely(ENROLLMENTS_FILE, lines);
    }

    // report.txt is only written. It is never loaded as project data.
    public void saveReport(List<String> reportLines) throws IOException {
        writeLinesSafely(REPORT_FILE, reportLines);
    }


    private List<String> readLines(Path file) throws IOException {
        List<String> lines = new ArrayList<>();
        if (!Files.exists(file)) {
            return lines;
        }
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            throw new IOException("Cannot read " + file.getFileName() + ": " + e.getMessage(), e);
        }
        return lines;
    }


    private void writeLinesSafely(Path target, List<String> lines) throws IOException {
        Path temp = DATA_FOLDER.resolve(target.getFileName() + ".tmp");
        try {
            Files.createDirectories(DATA_FOLDER);
            try (BufferedWriter writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
                for (String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }
            }
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            try {
                Files.deleteIfExists(temp);
            } catch (IOException ignored) {
                // nothing more we can do
            }
            throw new IOException("Could not save " + target.getFileName() + ": " + e.getMessage(), e);
        }
    }

    private IOException invalid(Path file, int lineNumber, String problem) {
        return new IOException(file.getFileName() + " line " + lineNumber + ": " + problem);
    }

    private boolean studentExists(List<Student> students, int studentId) {
        for (Student s : students) {
            if (s.getId() == studentId) {
                return true;
            }
        }
        return false;
    }

    private Course findCourse(List<Course> courses, int courseId) {
        for (Course c : courses) {
            if (c.getId() == courseId) {
                return c;
            }
        }
        return null;
    }
}

