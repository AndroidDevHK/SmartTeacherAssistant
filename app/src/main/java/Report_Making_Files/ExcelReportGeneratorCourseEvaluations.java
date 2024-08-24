package Report_Making_Files;

import static Report_Making_Files.ExcelReportGeneratorCompleteCourseAttendance.getCurrentDate;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import Display_Course_Students_Evaluation_Activity.CourseStudentEvaluationListModel;
import com.nextgen.hasnatfyp.StudentEvaluationDetailsModel;
import com.nextgen.hasnatfyp.TeacherInstanceModel;
import com.nextgen.hasnatfyp.UserInstituteModel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ExcelReportGeneratorCourseEvaluations {

    public static Uri generateEvaluationReport(List<CourseStudentEvaluationListModel> evaluationRecords, Context context, boolean areRepeaters) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Evaluation Report");
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

        createStyledRow(workbook, sheet, 1, "Evaluation Report", IndexedColors.RED, (short) 16);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 11));

        createStyledRow(workbook, sheet, 2, semesterName, IndexedColors.BLACK, (short) 14);
        sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 11));

        // Adjusted merged region width for Teacher Name and Report Creation Date
        createInfoRow(sheet, workbook, 5, "Class : " + className, "Teacher name: " + TName);
        createInfoRow(sheet, workbook, 8, "Course Name: " + courseName + repeaterStatus, "Report Creation Date : " + getCurrentDate());
        List<CourseStudentEvaluationListModel> preprocessedRecords = EvaluationPreprocessHelper.preprocessData(evaluationRecords);

        createHeaderRow(sheet, preprocessedRecords);
        populateDataRows(sheet, preprocessedRecords);
        return saveWorkbook(workbook, context, areRepeaters);
    }

    private static int getMaxEvaluations(List<CourseStudentEvaluationListModel> evaluationRecords) {
        int maxEvaluations = 0;
        for (CourseStudentEvaluationListModel record : evaluationRecords) {
            List<StudentEvaluationDetailsModel> evaluationList = record.getStudentEvalList();
            maxEvaluations = Math.max(maxEvaluations, evaluationList.size());
        }
        return maxEvaluations;
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
        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 1, 4)); // Merge cells for "Teacher Name"
        Cell secondCell = row.createCell(6);
        secondCell.setCellValue(secondCellValue);
        secondCell.setCellStyle(cellStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 6, 9)); // Merge cells for "Report Creation Date"
    }


    private static void createHeaderRow(Sheet sheet, List<CourseStudentEvaluationListModel> evaluationRecords) {
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

        // Get the first record's evaluation list since all students have the same order
        List<StudentEvaluationDetailsModel> evaluationList = evaluationRecords.get(0).getStudentEvalList();

        // Create header cells for each evaluation in the preprocessed order
        int evaluationColumnIndex = 3;
        Map<String, Integer> evaluationMaxMarksMap = new HashMap<>();
        for (StudentEvaluationDetailsModel evaluation : evaluationList) {
            String evalName = evaluation.getEvaluationName();
            int maxMarks = Integer.parseInt(evaluation.getTotalMarks());
            String header = evalName + " (" + maxMarks + ")";
            Cell evalCell = headerRow.createCell(evaluationColumnIndex);
            evalCell.setCellValue(header);
            evalCell.setCellStyle(headerCellStyle); // Set cell style

            // Adjust column width based on header length
            int headerLength = header.length();
            sheet.setColumnWidth(evaluationColumnIndex, (headerLength + 2) * 256);

            evaluationMaxMarksMap.put(evalName, maxMarks);
            evaluationColumnIndex++;
        }

        // Calculate the total marks and obtained marks for the evaluations
        int totalMaxMarks = 0;
        for (Map.Entry<String, Integer> entry : evaluationMaxMarksMap.entrySet()) {
            totalMaxMarks += entry.getValue();
        }

        headerRow.createCell(evaluationColumnIndex).setCellValue("Total");
        headerRow.createCell(evaluationColumnIndex + 1).setCellValue("Obtained");
        headerRow.createCell(evaluationColumnIndex + 2).setCellValue("Percentage");

        for (Cell cell : headerRow) {
            cell.setCellStyle(headerCellStyle);
        }

        // Set column widths
        sheet.setColumnWidth(0, 4 * 256);
        int maxNameLength = getMaxNameLength(evaluationRecords);
        int maxRollNoLength = getMaxRollNoLength(evaluationRecords);
        sheet.setColumnWidth(1, (maxNameLength + 5) * 256);
        sheet.setColumnWidth(2, (maxRollNoLength + 2) * 256);
        sheet.setColumnWidth(evaluationColumnIndex, 12 * 256);
        sheet.setColumnWidth(evaluationColumnIndex + 1, 12 * 256);
        sheet.setColumnWidth(evaluationColumnIndex + 2, 12 * 256);
    }

    private static int getMaxRollNoLength(List<CourseStudentEvaluationListModel> evaluationRecords) {
        return evaluationRecords.stream()
                .map(CourseStudentEvaluationListModel::getStudentRollNo)
                .mapToInt(String::length)
                .max().orElse(0);
    }


    private static void populateDataRows(Sheet sheet, @NonNull List<CourseStudentEvaluationListModel> evaluationRecords) {
        int maxEvaluations = getMaxEvaluations(evaluationRecords);

        int rowNum = 11;
        for (CourseStudentEvaluationListModel record : evaluationRecords) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(rowNum - 11);
            String studentName = record.getStudentName();
            studentName = studentName.replaceAll("(?i)(muhammad|mohammad)", "M.");
            row.createCell(1).setCellValue(studentName);
            row.createCell(2).setCellValue(record.getStudentRollNo());

            List<StudentEvaluationDetailsModel> evaluationList = record.getStudentEvalList();
            float totalObtained = 0;
            float totalMarks = 0;
            int columnIndex = 3; // Start index for evaluation columns
            for (StudentEvaluationDetailsModel evaluation : evaluationList) {
                String obtainedMarks = evaluation.getObtainedMarks();
                if (!"N/A".equalsIgnoreCase(obtainedMarks)) {
                    row.createCell(columnIndex).setCellValue(obtainedMarks);
                    totalObtained += Float.parseFloat(obtainedMarks);
                    totalMarks += Float.parseFloat(evaluation.getTotalMarks());
                } else {
                    row.createCell(columnIndex).setCellValue(obtainedMarks);
                }
                columnIndex++;
            }

            String finalTotalMarks = removeDecimalIfNotNecessary(totalMarks);
            String finalTotalObtained = removeDecimalIfNotNecessary(totalObtained);

            float percentage = totalMarks == 0 ? 0 : (totalObtained * 100.0f) / totalMarks;
            String finalPercentage = formatPercentage(percentage);

            row.createCell(maxEvaluations + 3).setCellValue(finalTotalMarks);
            row.createCell(maxEvaluations + 4).setCellValue(finalTotalObtained);
            row.createCell(maxEvaluations + 5).setCellValue(finalPercentage);

            // Apply styles to data rows
            CellStyle dataCellStyle = sheet.getWorkbook().createCellStyle();
            dataCellStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
            dataCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            dataCellStyle.setBorderBottom(BorderStyle.THIN);
            dataCellStyle.setBorderTop(BorderStyle.THIN);
            dataCellStyle.setBorderLeft(BorderStyle.THIN);
            dataCellStyle.setBorderRight(BorderStyle.THIN);
            dataCellStyle.setAlignment(HorizontalAlignment.CENTER); // Center-align data cells
            for (Cell cell : row) {
                cell.setCellStyle(dataCellStyle);
            }
        }

        // Adjust column widths based on content
        for (int i = 0; i < maxEvaluations + 6; i++) {
            adjustColumnWidths(sheet, maxEvaluations, evaluationRecords);
        }
        // Set the width of the G column (index 6) to accommodate 10 characters
        sheet.setColumnWidth(6, 10 * 256);
    }

    private static void adjustColumnWidths(Sheet sheet, int maxEvaluations, List<CourseStudentEvaluationListModel> evaluationRecords) {
        int maxLengthName = getMaxNameLength(evaluationRecords);
        sheet.setColumnWidth(1, Math.max(12 * 256, (maxLengthName + 5) * 256));

        for (int i = 0; i < maxEvaluations + 6; i++) {
            int maxLength = getMaxCellContentLength(sheet, i);
            if (i == 0) {
                maxLength = Math.min(maxLength, 3);
            }
            if (i != 1 && i != 6) { // Skip setting width for column 1 (Student Name) and column 6 (Obtained)
                sheet.setColumnWidth(i, Math.max(12 * 256, (maxLength + 1) * 256));
            }
        }
    }

    private static int getMaxNameLength(List<CourseStudentEvaluationListModel> evaluationRecords) {
        return evaluationRecords.stream()
                .map(record -> record.getStudentName().replaceAll("(?i)(muhammad|mohammad)", "M."))
                .mapToInt(String::length)
                .max().orElse(0);
    }

    private static int getMaxCellContentLength(Sheet sheet, int columnIndex) {
        int maxLength = 0;
        for (Row row : sheet) {
            Cell cell = row.getCell(columnIndex);
            if (cell != null) {
                maxLength = Math.max(maxLength, cell.toString().length());
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

        String fileName = "Evaluation Report " + " — " + courseName + repeaterStatus + " — " + className + " — " + semesterName + ".xlsx";

        File documentsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if (documentsDir != null) {
            File file = new File(documentsDir, fileName);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
                workbook.close();
                return FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
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

    @NonNull
    private static String removeDecimalIfNotNecessary(float number) {
        return number % 1 == 0 ? String.valueOf((int) number) : String.valueOf(number);
    }

    @SuppressLint("DefaultLocale")
    @NonNull
    private static String formatPercentage(float percentage) {
        return String.format("%.0f", percentage);
    }
}
