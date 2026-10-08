package com.courseenrollmentmanagement;

public class Course {
        private static final String[] BATCHES = {"Morning", "Afternoon", "Evening"};

        public static final int MIN_CAPACITY = 1;
        public static final int MAX_CAPACITY = 1000;

        private final int id;
        private final String title;
        private final String trainer;
        private final String batch;
        private final int capacity;

        public Course(int id, String title, String trainer, String batch, int capacity) {
            if (id < 1) {
                throw new IllegalArgumentException("Course ID must be 1 or more.");
            }
            this.id = id;
            this.title = requireText(title, "Title");
            this.trainer = requireText(trainer, "Trainer name");

            if (!isValidBatch(batch)) {
                throw new IllegalArgumentException("Batch must be Morning, Afternoon, or Evening.");
            }
            this.batch = batch;

            if (capacity < MIN_CAPACITY || capacity > MAX_CAPACITY) {
                throw new IllegalArgumentException(
                        "Capacity must be from " + MIN_CAPACITY + " to " + MAX_CAPACITY + ".");
            }
            this.capacity = capacity;
        }


        public static String[] getBatches() {
            return BATCHES.clone();
        }

        public static boolean isValidBatch(String batch) {
            for (int i = 0; i < BATCHES.length; i++) {
                if (BATCHES[i].equals(batch)) {
                    return true;
                }
            }
            return false;
        }

        public int getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getTrainer() {
            return trainer;
        }

        public String getBatch() {
            return batch;
        }

        public int getCapacity() {
            return capacity;
        }

        public void showDetails() {
            System.out.println(id + " | " + title + " | " + trainer + " | " + batch
                    + " | Capacity: " + capacity);
        }
    public void showDetails(int activeCount) {
        int freeSeats = capacity - activeCount;

        System.out.println(id + " | " + title + " | " + trainer + " | " + batch
                + " | Capacity: " + capacity
                + " | Active: " + activeCount
                + " | Free seats: " + freeSeats);
    }

        private static String requireText(String value, String fieldName) {
            if (value == null || value.trim().isEmpty()) {
                throw new IllegalArgumentException(fieldName + " is required.");
            }
            if (value.contains("|") || value.contains("\n") || value.contains("\r")) {
                throw new IllegalArgumentException(fieldName + " cannot contain '|' or line breaks.");
            }
            return value.trim();
        }
    }