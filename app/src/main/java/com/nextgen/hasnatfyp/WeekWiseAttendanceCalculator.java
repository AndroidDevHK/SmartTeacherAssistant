package com.nextgen.hasnatfyp;

import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WeekWiseAttendanceCalculator {

    public static String calculate(String startDate, String endDate, List<String> attendanceDates, TextView creditHoursTextView, int creditHours) {
        StringBuilder builder = new StringBuilder();

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(start);

            int weekCounter = 1;
            int totalExpectedClasses = 0; // Variable to store total expected classes

            // Calculate total expected classes based on the duration between start and end dates
            long durationInMillis = end.getTime() - start.getTime();
            long durationInDays = durationInMillis / (1000 * 60 * 60 * 24); // Convert milliseconds to days
            int numberOfWeeks = (int) Math.ceil(durationInDays / 7.0); // Round up to the nearest whole number of weeks
            int expectedClassesPerWeek = numberOfWeeks * creditHours; // Total classes expected per week
            totalExpectedClasses += expectedClassesPerWeek;

            while (calendar.getTime().before(end)) {
                int weekClasses = 0;

                for (String attendanceDate : attendanceDates) {
                    try {
                        Date date = sdf.parse(attendanceDate);
                        if (isDateInWeek(date, calendar)) {
                            weekClasses++;
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                builder.append("Week ").append(weekCounter).append(": ").append(weekClasses).append(" classes\n");
                weekCounter++;
                calendar.add(Calendar.WEEK_OF_YEAR, 1);
            }

            // Add total expected classes to the bottom of the week-wise attendance
            creditHoursTextView.setText("\nExpected Classes: " + totalExpectedClasses);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return builder.toString();
    }

    private static boolean isDateInWeek(Date date, Calendar weekStart) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.YEAR) == weekStart.get(Calendar.YEAR) &&
                calendar.get(Calendar.WEEK_OF_YEAR) == weekStart.get(Calendar.WEEK_OF_YEAR);
    }

    public static int calculateExpectedClasses(String startDate, String endDate, int creditHours) {
        int totalExpectedClasses = 0;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);

            // Calculate total duration in days
            long durationInMillis = end.getTime() - start.getTime();
            long durationInDays = durationInMillis / (1000 * 60 * 60 * 24); // Convert milliseconds to days

            // Calculate the number of weeks
            int numberOfWeeks = (int) Math.ceil(durationInDays / 7.0); // Round up to the nearest whole number of weeks

            // Calculate total expected classes
            totalExpectedClasses = numberOfWeeks * creditHours;

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return totalExpectedClasses;
    }

}
