# Course Enrollment Management System

A Java console application where an **admin** adds short courses and **students** enroll in the available seats. Students can view courses, enroll, cancel their own enrollment and view their own history. Profiles, courses and enrollment history are saved in text files.

Built as a Core Java project to practise OOP, arrays, collections and file handling.

## Features

- Student registration with generated IDs (starting from 1)
- Built-in Admin with ID 0 (role selection only, no passwords)
- Admin adds courses: title, trainer, batch (Morning, Afternoon or Evening) and capacity (1 to 1000)
- View courses with capacity, active enrollments and free seats, with an optional batch filter
- Enroll in a course, with checks for: course exists, no duplicate ACTIVE enrollment, and free seats available
- Cancel your own ACTIVE enrollment (the old record is kept as history)
- Reports: student count, course count, ACTIVE and CANCELLED enrollments, distinct students with active enrollments, seats used per course, and an option to save `report.txt`
- Data is saved to text files and loaded again on restart

## Menus

```
MAIN MENU            ADMIN MENU              STUDENT MENU
1. Register student  1. Add course           1. View courses
2. Admin menu        2. View courses         2. Enroll in course
3. Student menu      3. View students        3. Cancel my enrollment
0. Exit              4. View enrollments     4. View my enrollment history
                     5. View reports         0. Back
                     0. Back
```

## Business rules

- A seat is used only by an ACTIVE enrollment. Free seats = capacity minus active enrollments (calculated, never stored).
- A student cannot have two ACTIVE enrollments in the same course.
- A full course refuses new enrollments.
- A student can cancel only their own ACTIVE enrollment. A cancellation cannot be repeated.
- After cancelling, a student may enroll again. This creates a new record.
- Names, titles, trainer names and departments are required and cannot contain `|` or line breaks.

## Java concepts used

| Concept | Where |
|---|---|
| Encapsulation | Private fields and validation in constructors |
| Inheritance | `Admin` and `Student` extend `User` |
| Abstraction | `User` is abstract and declares `showProfile()` |
| Polymorphism / overriding | Each child class overrides `showProfile()` |
| Composition | `CourseService` holds the student, course and enrollment lists |
| Arrays | `String[3]` batch choices: Morning, Afternoon, Evening |
| Collections | `ArrayList` for records, `HashSet<Integer>` for distinct active students |
| File handling | `BufferedReader` and `BufferedWriter` through `Files`, with try-with-resources and safe saving through a temporary file |
| Enum | `EnrollmentStatus` with `ACTIVE` and `CANCELLED` |

## Project structure

```
CourseEnrollmentManagement/
├── data/
│   ├── students.txt
│   ├── courses.txt
│   ├── enrollments.txt
│   └── report.txt
└── src/com/courseenrollmentmanagement/
    ├── Main.java            (console input and menus)
    ├── CourseService.java   (project actions and rules)
    ├── FileManager.java     (reading and writing files)
    ├── User.java            (abstract)
    ├── Admin.java
    ├── Student.java
    ├── Course.java
    ├── Enrollment.java
    └── EnrollmentStatus.java
```

## Data file formats

One record per line, fields separated by `|`.

| File | Format |
|---|---|
| `data/students.txt` | `id|name|department` |
| `data/courses.txt` | `id|title|trainer|batch|capacity` |
| `data/enrollments.txt` | `id|studentId|courseId|status` |
| `data/report.txt` | Readable totals, one item per line |

Example:

```
students.txt      1|Ananya Rao|CSE
courses.txt       1|Java Basics|R Kumar|Morning|2
enrollments.txt   1|1|1|ACTIVE
```

At startup the files are checked (field counts, values, duplicate IDs, referenced IDs). If a line is invalid, the program shows the file name and line number and does not start.

## How to run

1. Install **JDK 21** and open the project in IntelliJ IDEA (or any Java IDE).
2. Open `src/com/courseenrollmentmanagement/Main.java`.
3. Run the `main` method.
4. The `data` folder is created in the project folder if it is missing.

## Sample session

```
MAIN MENU
1. Register student
...
Enter choice: 3
Enter student ID (0 to go back): 1
...
Enter choice: 2
Enroll in course ID: 1
Enrollment 1 saved. Status: ACTIVE
Free seats in course 1: 1

Enroll in course ID: 1
You already have an active enrollment.
```