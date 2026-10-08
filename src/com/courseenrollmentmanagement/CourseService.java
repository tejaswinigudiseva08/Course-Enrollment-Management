package com.courseenrollmentmanagement;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
public class CourseService {
    private final ArrayList<Student> students = new ArrayList<>();
    private final ArrayList<Course> courses = new ArrayList<>();
    private final ArrayList<Enrollment> enrollments = new ArrayList<>();
    private final FileManager fileManager;
    public CourseService(FileManager fileManager) {
        this.fileManager = fileManager;
    }
    public void loadData() throws IOException {
        fileManager.createDataFolder();
        ArrayList<Student> loadedStudents = fileManager.loadStudents();
        ArrayList<Course> loadedCourses = fileManager.loadCourses();
        ArrayList<Enrollment> loadedEnrollments =
                fileManager.loadEnrollments(loadedStudents, loadedCourses);

        students.clear();
        students.addAll(loadedStudents);
        courses.clear();
        courses.addAll(loadedCourses);
        enrollments.clear();
        enrollments.addAll(loadedEnrollments);
    }

    public int getStudentCount() {
        return students.size();
    }

    public int getCourseCount() {
        return courses.size();
    }

    public int getEnrollmentCount() {
        return enrollments.size();
    }

    public int countEnrollments(EnrollmentStatus status) {
        int count = 0;
        for (Enrollment e : enrollments) {
            if (e.getStatus() == status) {
                count++;
            }
        }
        return count;
    }

    public int countDistinctActiveStudents() {
        HashSet<Integer> activeStudents = new HashSet<>();
        for (Enrollment e : enrollments) {
            if (e.getStatus() == EnrollmentStatus.ACTIVE) {
                activeStudents.add(e.getStudentId());
            }
        }
        return activeStudents.size();
    }
    public Student findStudentById(int studentId) {
        for (Student s : students) {
            if (s.getId() == studentId) {
                return s;
            }
        }
        return null;
    }

    public Course findCourseById(int courseId) {
        for (Course c : courses) {
            if (c.getId() == courseId) {
                return c;
            }
        }
        return null;
    }
    public Enrollment findEnrollmentById(int enrollmentId) {
        for (Enrollment e : enrollments) {
            if (e.getId() == enrollmentId) {
                return e;
            }
        }
        return null;
    }

    public int countActive(int courseId) {
        int count = 0;
        for (Enrollment e : enrollments) {
            if (e.getCourseId() == courseId && e.getStatus() == EnrollmentStatus.ACTIVE) {
                count++;
            }
        }
        return count;
    }

    public int getFreeSeats(Course course) {
        return course.getCapacity() - countActive(course.getId());
    }

    private boolean hasActiveEnrollment(int studentId, int courseId) {
        for (Enrollment e : enrollments) {
            if (e.getStudentId() == studentId
                    && e.getCourseId() == courseId
                    && e.getStatus() == EnrollmentStatus.ACTIVE) {
                return true;
            }
        }
        return false;
    }

    private int getNextStudentId() {
        int largest = 0;
        for (Student s : students) {
            if (s.getId() > largest) {
                largest = s.getId();
            }
        }
        return largest + 1;
    }
    private int getNextCourseId() {
        int largest = 0;
        for (Course c : courses) {
            if (c.getId() > largest) {
                largest = c.getId();
            }
        }
        return largest + 1;
    }
    private int getNextEnrollmentId() {
        int largest = 0;
        for (Enrollment e : enrollments) {
            if (e.getId() > largest) {
                largest = e.getId();
            }
        }
        return largest + 1;
    }
    public Student registerStudent(String name, String department) throws IOException {
        Student student = new Student(getNextStudentId(), name, department);

        ArrayList<Student> updated = new ArrayList<>(students);
        updated.add(student);
        fileManager.saveStudents(updated);

        students.add(student);
        return student;
    }

    public Course addCourse(String title, String trainer, String batch, int capacity)
            throws IOException {
        Course course = new Course(getNextCourseId(), title, trainer, batch, capacity);

        ArrayList<Course> updated = new ArrayList<>(courses);
        updated.add(course);
        fileManager.saveCourses(updated);

        courses.add(course);
        return course;
    }

    public Enrollment enroll(int studentId, int courseId) throws IOException {
        if (findStudentById(studentId) == null) {
            throw new IllegalArgumentException("Student ID " + studentId + " not found.");
        }

        Course course = findCourseById(courseId);
        if (course == null) {
            throw new IllegalArgumentException("Course ID " + courseId + " not found.");
        }
        if (hasActiveEnrollment(studentId, courseId)) {
            throw new IllegalStateException("You already have an active enrollment.");
        }
        if (countActive(courseId) >= course.getCapacity()) {
            throw new IllegalStateException("This course is full.");
        }

        Enrollment enrollment = new Enrollment(getNextEnrollmentId(), studentId, courseId);

        ArrayList<Enrollment> updated = new ArrayList<>(enrollments);
        updated.add(enrollment);
        fileManager.saveEnrollments(updated);

        enrollments.add(enrollment);
        return enrollment;
    }
    public Enrollment cancelEnrollment(int studentId, int enrollmentId) throws IOException {
        Enrollment enrollment = findEnrollmentById(enrollmentId);
        if (enrollment == null) {
            throw new IllegalArgumentException("Enrollment ID " + enrollmentId + " not found.");
        }
        if (enrollment.getStudentId() != studentId) {
            throw new IllegalStateException("You can cancel only your own enrollment.");
        }
        if (enrollment.getStatus() != EnrollmentStatus.ACTIVE) {
            throw new IllegalStateException("Enrollment " + enrollmentId + " is already cancelled.");
        }

        ArrayList<Enrollment> updated = new ArrayList<>();
        for (Enrollment e : enrollments) {
            if (e == enrollment) {
                updated.add(new Enrollment(e.getId(), e.getStudentId(), e.getCourseId(),
                        EnrollmentStatus.CANCELLED));
            } else {
                updated.add(e);
            }
        }
        fileManager.saveEnrollments(updated);

        enrollment.cancel();
        return enrollment;
    }
    public void showCourses(String batchFilter) {
        boolean found = false;
        for (Course c : courses) {
            if (batchFilter == null || c.getBatch().equals(batchFilter)) {
                c.showDetails(countActive(c.getId()));
                found = true;
            }
        }
        if (!found) {
            System.out.println("No records found");
        }
    }

    public void showStudents() {
        if (students.isEmpty()) {
            System.out.println("No records found");
            return;
        }
        for (Student s : students) {
            s.showProfile();
        }
    }

    public void showEnrollments() {
        if (enrollments.isEmpty()) {
            System.out.println("No records found");
            return;
        }
        for (Enrollment e : enrollments) {
            printEnrollment(e);
        }
    }

    public void showEnrollmentHistory(int studentId) {
        boolean found = false;
        for (Enrollment e : enrollments) {
            if (e.getStudentId() == studentId) {
                printEnrollment(e);
                found = true;
            }
        }
        if (!found) {
            System.out.println("No records found");
        }
    }
    private void printEnrollment(Enrollment e) {
        System.out.println("Enrollment " + e.getId()
                + " | Student " + e.getStudentId()
                + " | Course " + e.getCourseId()
                + " | " + e.getStatus());
    }
    public List<String> buildReport() {
        List<String> lines = new ArrayList<>();
        lines.add("COURSE ENROLLMENT REPORT");
        lines.add("Students: " + students.size());
        lines.add("Courses: " + courses.size());
        lines.add("ACTIVE enrollments: " + countEnrollments(EnrollmentStatus.ACTIVE));
        lines.add("CANCELLED enrollments: " + countEnrollments(EnrollmentStatus.CANCELLED));
        lines.add("Distinct students with active enrollments: " + countDistinctActiveStudents());
        lines.add("Seats used per course:");
        if (courses.isEmpty()) {
            lines.add("No records found");
        } else {
            for (Course c : courses) {
                lines.add("Course " + c.getId() + " | " + c.getTitle()
                        + " | Seats used: " + countActive(c.getId()) + " of " + c.getCapacity());
            }
        }
        return lines;
    }
    public void showReport() {
        for (String line : buildReport()) {
            System.out.println(line);
        }
    }
    public void saveReport() throws IOException {
        fileManager.saveReport(buildReport());
    }
}