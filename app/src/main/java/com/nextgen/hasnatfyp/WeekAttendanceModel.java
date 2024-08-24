package com.nextgen.hasnatfyp;

public class WeekAttendanceModel {


    private int weekNumber;
    private String startDate;
    private String endDate;
    private int totalClasses;
    private int classesAttended;


    public WeekAttendanceModel(int weekNumber, String startDate, String endDate, int totalClasses, int classesAttended) {
        this.weekNumber = weekNumber;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalClasses = totalClasses;
        this.classesAttended = classesAttended;
    }

    // Getters and setters

    public int getWeekNumber() {
        return weekNumber;
    }

    public void setWeekNumber(int weekNumber) {
        this.weekNumber = weekNumber;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public int getTotalClasses() {
        return totalClasses;
    }

    public void setTotalClasses(int totalClasses) {
        this.totalClasses = totalClasses;
    }

    public int getClassesAttended() {
        return classesAttended;
    }

    public void setClassesAttended(int classesAttended) {
        this.classesAttended = classesAttended;
    }
}

