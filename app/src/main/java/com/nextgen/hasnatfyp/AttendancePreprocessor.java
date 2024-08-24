package com.nextgen.hasnatfyp;

import java.util.List;
import Display_Course_Attendance_Activity.StudentAttendanceModel;
import Display_Course_Attendance_Activity.StudentAttendanceRecordModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class AttendancePreprocessor {
    public static List<StudentAttendanceRecordModel> preprocessAttendanceRecords(List<StudentAttendanceRecordModel> attendanceRecords) {
        int maxDates = findMaxAttendanceDates(attendanceRecords);
        List<String> maxDatesList = extractDatesWithMaxAttendance(attendanceRecords, maxDates);

        for (StudentAttendanceRecordModel record : attendanceRecords) {
            List<StudentAttendanceModel> attendanceList = record.getAttendanceList();

            for (String date : maxDatesList) {
                if (!containsDate(attendanceList, date)) {
                    attendanceList.add(new StudentAttendanceModel(date, "N/A"));
                }
            }
            Collections.sort(attendanceList, Comparator.comparing(StudentAttendanceModel::getDate));
        }

        return attendanceRecords;
    }

    private static int findMaxAttendanceDates(List<StudentAttendanceRecordModel> attendanceRecords) {
        int maxDates = 0;
        for (StudentAttendanceRecordModel record : attendanceRecords) {
            List<StudentAttendanceModel> attendanceList = record.getAttendanceList();
            maxDates = Math.max(maxDates, attendanceList.size());
        }
        return maxDates;
    }

    private static List<String> extractDatesWithMaxAttendance(List<StudentAttendanceRecordModel> attendanceRecords, int maxDates) {
        List<String> maxDatesList = new ArrayList<>();
        for (StudentAttendanceRecordModel record : attendanceRecords) {
            List<StudentAttendanceModel> attendanceList = record.getAttendanceList();
            if (attendanceList.size() == maxDates) {
                // Extract dates for the student with maximum attendance dates
                for (StudentAttendanceModel attendance : attendanceList) {
                    maxDatesList.add(attendance.getDate());
                }
                break; // Found the student with maximum attendance dates
            }
        }
        return maxDatesList;
    }

    private static boolean containsDate(List<StudentAttendanceModel> attendanceList, String date) {
        for (StudentAttendanceModel attendance : attendanceList) {
            if (attendance.getDate().equals(date)) {
                return true;
            }
        }
        return false;
    }
}
