package org.example.models;

public class Course {
    private int course_id;
    private String name;
    private double attendance;

    public Course(int course_id, String name, double attendance) {
        this.course_id = course_id;
        this.name = name;
        this.attendance = attendance;
    }

    public void setAttendance(double attendance) {
        this.attendance = attendance;
    }

    public double getAttendance() {
        return attendance;
    }

    @Override
    public String toString() {
        return "Course name: " + name +
                " Course attendance: " + attendance + "%";
    }
}
