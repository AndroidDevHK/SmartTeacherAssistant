package com.nextgen.hasnatfyp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtils {
    // Method to format ISO date to a user-friendly format
    public static String formatAvailableWhen(String isoDate) {
        String formattedDateTime = "";

        // Define the input ISO format for Pakistani time
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        isoFormat.setTimeZone(TimeZone.getTimeZone("Asia/Karachi")); // Set Pakistan time zone

        // Define the output format for Pakistan date and time
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Karachi")); // Set Pakistan time zone

        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        timeFormat.setTimeZone(TimeZone.getTimeZone("Asia/Karachi")); // Set Pakistan time zone

        try {
            Date date = isoFormat.parse(isoDate);
            if (date != null) {
                String formattedDate = dateFormat.format(date);
                String formattedTime = timeFormat.format(date);
                formattedDateTime = formattedDate + " at " + formattedTime;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return formattedDateTime;
    }

    // Method to format quiz duration in a user-friendly format
    public static String formatQuizDuration(String duration) {
        try {
            String[] parts = duration.split(",");
            if (parts.length == 2) {
                String hoursPart = parts[0].trim(); // "0 hours"
                String minutesPart = parts[1].trim(); // "20 minutes"

                int hours = extractHours(hoursPart); // Extract hours
                int minutes = extractMinutes(minutesPart); // Extract minutes

                if (hours > 0 && minutes > 0) {
                    return String.format("%d hour%s %d minute%s", hours, (hours > 1 ? "s" : ""), minutes, (minutes > 1 ? "s" : ""));
                } else if (hours > 0) {
                    return String.format("%d hour%s", hours, (hours > 1 ? "s" : ""));
                } else {
                    return String.format("%d minute%s", minutes, (minutes > 1 ? "s" : ""));
                }
            } else {
                // Handle unexpected format, return the original string
                return duration;
            }
        } catch (NumberFormatException e) {
            // Handle unexpected input format
            return duration; // Returning the original string if parsing fails
        }
    }

    private static int extractHours(String hoursPart) throws NumberFormatException {
        // Extract hours from "X hours" format
        return Integer.parseInt(hoursPart.split(" ")[0]);
    }

    private static int extractMinutes(String minutesPart) throws NumberFormatException {
        // Extract minutes from "X minutes" format
        return Integer.parseInt(minutesPart.split(" ")[0]);
    }
}
