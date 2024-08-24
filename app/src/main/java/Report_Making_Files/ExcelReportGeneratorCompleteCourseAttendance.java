package Report_Making_Files;

import static Report_Making_Files.ExcelReportGeneratorCompleteCourseAttendance.getCurrentDate;

import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import com.nextgen.hasnatfyp.AttendancePreprocessor;
import com.nextgen.hasnatfyp.TeacherInstanceModel;
import com.nextgen.hasnatfyp.UserInstituteModel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import Display_Course_Attendance_Activity.StudentAttendanceModel;
import Display_Course_Attendance_Activity.StudentAttendanceRecordModel;

public class ExcelReportGeneratorCompleteCourseAttendance {

    public static Uri generateExcelReport(List<StudentAttendanceRecordModel> attendanceRecords, Context context, boolean areRepeaters) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Attendance Report");
        TeacherInstanceModel teacherInstanceModel = TeacherInstanceModel.getInstance(context);
        String courseName = teacherInstanceModel.getCourseName();
        String className = teacherInstanceModel.getClassName();
        String semesterName = teacherInstanceModel.getSemesterName();
        String repeaterStatus = areRepeaters ? "(Repeaters)" : "";
        String CampusName;
        String TName;
        if(UserInstituteModel.getInstance(context).isSoloUser())
        {
            CampusName = UserInstituteModel.getInstance(context).getCampusName();
            TName = UserInstituteModel.getInstance(context).getAdminName();
        }
        else
        {
            CampusName = TeacherInstanceModel.getInstance(context).getInstituteName();
            TName = TeacherInstanceModel.getInstance(context).getTeacherName();
        }
        createStyledRow(workbook, sheet, 0, CampusName, IndexedColors.BLUE, (short) 18);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 11));

        createStyledRow(workbook, sheet, 1, "Attendance Report", IndexedColors.RED, (short) 16);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 11));

        createStyledRow(workbook, sheet, 2, semesterName, IndexedColors.BLACK, (short) 14);
        sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 11));

        createInfoRow(sheet, workbook, 5, "Class : " + className, "Teacher name: " + TName);
        createInfoRow(sheet, workbook, 8, "Course Name: " + courseName + repeaterStatus, "Report Creation Date : " + getCurrentDate());
        List<StudentAttendanceRecordModel> preattendanceRecords = AttendancePreprocessor.preprocessAttendanceRecords(attendanceRecords);

        createHeaderRow(sheet, preattendanceRecords);
        populateDataRows(sheet, preattendanceRecords);
        return saveWorkbook(workbook, context, areRepeaters);
    }

    private static void createStyledRow(Workbook workbook, Sheet sheet, int rowIndex, String cellValue, IndexedColors fontColor, short fontSize) {
        Row row = sheet.createRow(rowIndex);
        row.setHeightInPoints(fontSize + 10);
        CellStyle cellStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints(fontSize);
        font.setColor(fontColor.getIndex());
        cellStyle.setFont(font);
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        Cell cell = row.createCell(0);
        cell.setCellValue(cellValue);
        cell.setCellStyle(cellStyle);
    }

    private static void createInfoRow(Sheet sheet, Workbook workbook, int rowIndex, String firstCellValue, String secondCellValue) {
        Row row = sheet.createRow(rowIndex);
        row.setHeightInPoints(20);
        CellStyle cellStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        cellStyle.setFont(font);
        Cell firstCell = row.createCell(1);
        firstCell.setCellValue(firstCellValue);
        firstCell.setCellStyle(cellStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 1, 5));
        Cell secondCell = row.createCell(6);
        secondCell.setCellValue(secondCellValue);
        secondCell.setCellStyle(cellStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 6, 11));
    }

    private static void createHeaderRow(Sheet sheet, List<StudentAttendanceRecordModel> attendanceRecords) {
        Row headerRow = sheet.createRow(10);
        CellStyle headerCellStyle = sheet.getWorkbook().createCellStyle();
        headerCellStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerCellStyle.setBorderBottom(BorderStyle.THIN);
        headerCellStyle.setBorderTop(BorderStyle.THIN);
        headerCellStyle.setBorderLeft(BorderStyle.THIN);
        headerCellStyle.setBorderRight(BorderStyle.THIN);
        Font headerFont = sheet.getWorkbook().createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setAlignment(HorizontalAlignment.CENTER);

        headerRow.createCell(0).setCellValue("Sr#");
        headerRow.createCell(1).setCellValue("Student Name");
        headerRow.createCell(2).setCellValue("Roll No");

        List<StudentAttendanceModel> firstRecordAttendanceList = attendanceRecords.get(0).getAttendanceList();
        for (int i = 0; i < firstRecordAttendanceList.size(); i++) {
            String date = firstRecordAttendanceList.get(i).getDate();
            headerRow.createCell(i + 3).setCellValue(date);
        }

        headerRow.createCell(firstRecordAttendanceList.size() + 3).setCellValue("Total");
        headerRow.createCell(firstRecordAttendanceList.size() + 4).setCellValue("Presents");
        headerRow.createCell(firstRecordAttendanceList.size() + 5).setCellValue("Absents");
        headerRow.createCell(firstRecordAttendanceList.size() + 6).setCellValue("Leave");
        headerRow.createCell(firstRecordAttendanceList.size() + 7).setCellValue("Percentage");

        for (Cell cell : headerRow) {
            cell.setCellStyle(headerCellStyle);
        }

        // Set column widths
        sheet.setColumnWidth(0, 4 * 256);

        // Get the maximum length of names and roll numbers to set column widths
        int maxNameLength = getMaxNameLength(attendanceRecords);
        int maxRollNoLength = getMaxRollNoLength(attendanceRecords);

        sheet.setColumnWidth(1, (maxNameLength + 5) * 256);
        sheet.setColumnWidth(2, (maxRollNoLength + 2) * 256);

        for (int i = 3; i < firstRecordAttendanceList.size() + 8; i++) {
            sheet.setColumnWidth(i, 12 * 256);
        }
    }

    private static int getMaxRollNoLength(List<StudentAttendanceRecordModel> attendanceRecords) {
        return attendanceRecords.stream()
                .map(StudentAttendanceRecordModel::getStudentRollNo)
                .mapToInt(String::length)
                .max().orElse(0);
    }


    private static void populateDataRows(Sheet sheet, List<StudentAttendanceRecordModel> attendanceRecords) {
        int maxDates = 0;
        for (StudentAttendanceRecordModel record : attendanceRecords) {
            List<StudentAttendanceModel> attendanceList = record.getAttendanceList();
            maxDates = Math.max(maxDates, attendanceList.size());
        }

        int rowNum = 11;
        for (StudentAttendanceRecordModel record : attendanceRecords) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(rowNum - 11);
            String studentName = record.getName();
            studentName = studentName.replaceAll("(?i)(muhammad|mohammad)", "M.");
            row.createCell(1).setCellValue(studentName);
            row.createCell(2).setCellValue(record.getStudentRollNo());

            List<StudentAttendanceModel> attendanceList = record.getAttendanceList();
            int presentCount = 0;
            int absentCount = 0;
            int leaveCount = 0;
            for (StudentAttendanceModel attendance : attendanceList) {
                String status = attendance.getAttendanceStatus();
                if (status.equals("P")) {
                    presentCount++;
                } else if (status.equals("A")) {
                    absentCount++;
                } else if (status.equals("L")) {
                    leaveCount++;
                }
            }

            int total = presentCount + absentCount + leaveCount;
            double percentage = total == 0 ? 0 : Math.round((presentCount * 100.0) / total);

            int cellIndex = 3;
            for (StudentAttendanceModel attendance : attendanceList) {
                row.createCell(cellIndex++).setCellValue(attendance.getAttendanceStatus());
            }

            row.createCell(maxDates + 3).setCellValue(total);
            row.createCell(maxDates + 4).setCellValue(presentCount);
            row.createCell(maxDates + 5).setCellValue(absentCount);
            row.createCell(maxDates + 6).setCellValue(leaveCount);
            row.createCell(maxDates + 7).setCellValue(percentage);

            CellStyle dataCellStyle = sheet.getWorkbook().createCellStyle();
            dataCellStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
            dataCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            dataCellStyle.setBorderBottom(BorderStyle.THIN);
            dataCellStyle.setBorderTop(BorderStyle.THIN);
            dataCellStyle.setBorderLeft(BorderStyle.THIN);
            dataCellStyle.setBorderRight(BorderStyle.THIN);
            dataCellStyle.setAlignment(HorizontalAlignment.CENTER);


            for (Cell cell : row) {
                cell.setCellStyle(dataCellStyle);
            }
        }

        for (int i = 0; i < maxDates + 8; i++) {
            adjustColumnWidths(sheet, maxDates, attendanceRecords);
        }
    }

    private static void adjustColumnWidths(Sheet sheet, int maxDates, List<StudentAttendanceRecordModel> attendanceRecords) {
        int maxLengthName = getMaxNameLength(attendanceRecords);
        sheet.setColumnWidth(1, Math.max(12 * 256, (maxLengthName + 5) * 256));

        for (int i = 0; i < maxDates + 8; i++) {
            int maxLength = getMaxCellContentLength(sheet, i);
            if (i == 0) {
                maxLength = Math.min(maxLength, 3);
            }
            if (i != 1 && i != 6) {
                sheet.setColumnWidth(i, Math.max(12 * 256, (maxLength + 1) * 256));
            }
        }
    }

    private static int getMaxNameLength(List<StudentAttendanceRecordModel> attendanceRecords) {
        int maxLength = 0;
        for (StudentAttendanceRecordModel record : attendanceRecords) {
            String name = record.getName();
            name = name.replaceAll("(?i)(muhammad|mohammad)", "M.");
            int length = name.length();
            maxLength = Math.max(maxLength, length);
        }
        return maxLength;
    }

    private static int getMaxCellContentLength(Sheet sheet, int columnIndex) {
        int maxLength = 0;
        for (Row row : sheet) {
            Cell cell = row.getCell(columnIndex);
            if (cell != null) {
                int length = cell.toString().length();
                maxLength = Math.max(maxLength, length);
            }
        }
        return maxLength;
    }

    private static Uri saveWorkbook(Workbook workbook, Context context, boolean areRepeaters) {
        TeacherInstanceModel teacherInstanceModel = TeacherInstanceModel.getInstance(context);
        String courseName = teacherInstanceModel.getCourseName();
        String className = teacherInstanceModel.getClassName();
        String semesterName = teacherInstanceModel.getSemesterName();
        String repeaterStatus = areRepeaters ? "(Repeaters)" : "";

        String fileName = "Attendance Report — " + courseName + repeaterStatus + " — " + className + " — " + semesterName + ".xlsx";

        File documentsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if (documentsDir != null) {
            File file = new File(documentsDir, fileName);

            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
                workbook.close();
                Uri fileUri = FileProvider.getUriForFile(context, "com.nextgen.hasnatfyp.provider", file);
                context.getContentResolver().notifyChange(fileUri, null);

                return fileUri;

            } catch (IOException e) {
                e.printStackTrace();
                Log.e("SaveWorkbook", "Error writing workbook to file", e);
            }
        } else {
            Log.e("SaveWorkbook", "External files directory is null");
        }
        Toast.makeText(context, "Failed to generate report", Toast.LENGTH_SHORT).show();
        return null;
    }

    static String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
        return dateFormat.format(date);
    }
}
