package Report_Making_Files;

import static Report_Making_Files.ExcelReportGeneratorCompleteCourseAttendance.getCurrentDate;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

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

import Display_Complete_Course_Att_Eval_data_Activity.CourseStudentDetailsModel;

public class CourseStudentsCompleteExcelReportGenerator {

    public static Uri generateEvaluationReport(List<CourseStudentDetailsModel> evaluationRecords, Context context, boolean areRepeaters) {
        TeacherInstanceModel teacherInstanceModel = TeacherInstanceModel.getInstance(context);
        String courseName = teacherInstanceModel.getCourseName();
        String className = teacherInstanceModel.getClassName();
        String semesterName = teacherInstanceModel.getSemesterName();
        String repeaterStatus = areRepeaters ? "(Repeaters)" : "";

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Course Complete Report");
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

        createStyledRow(workbook, sheet, 1, "Course Complete Report", IndexedColors.RED, (short) 16);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 11));

        createStyledRow(workbook, sheet, 2, semesterName, IndexedColors.BLACK, (short) 14);
        sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 11));

        createInfoRow(sheet, workbook, 5, "Class : " + className, "Teacher name: " + TName);
        createInfoRow(sheet, workbook, 8, "Course Name: " + courseName + repeaterStatus, "Report Creation Date : " + getCurrentDate());
        List<CourseStudentDetailsModel> preprocessedRecords = EvaluationPreprocessHelper2.preprocessData(evaluationRecords);

        createHeaderRow(sheet, preprocessedRecords);

        populateDataRows(sheet, preprocessedRecords);

        return saveWorkbook(workbook, context,areRepeaters);
    }

    private static int getMaxEvaluations(List<CourseStudentDetailsModel> evaluationRecords) {
        int maxEvaluations = 0;
        for (CourseStudentDetailsModel record : evaluationRecords) {
            List<StudentEvaluationDetailsModel> evaluationList = record.getEvaluationDetailsList();
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
        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 1, 5));
        Cell secondCell = row.createCell(6);
        secondCell.setCellValue(secondCellValue);
        secondCell.setCellStyle(cellStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 6, 11));
    }
    private static void createHeaderRow(Sheet sheet, List<CourseStudentDetailsModel> evaluationRecords) {
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
        headerRow.createCell(3).setCellValue("Total");
        headerRow.createCell(4).setCellValue("Presents");
        headerRow.createCell(5).setCellValue("Absents");
        headerRow.createCell(6).setCellValue("Leave");
        headerRow.createCell(7).setCellValue("Attendance %Age");

        List<StudentEvaluationDetailsModel> evaluationList = evaluationRecords.get(0).getEvaluationDetailsList();

        int evaluationColumnIndex = 8;
        Map<String, Integer> evaluationMaxMarksMap = new HashMap<>();
        for (StudentEvaluationDetailsModel evaluation : evaluationList) {
            String evalName = evaluation.getEvaluationName();
            int maxMarks = Integer.parseInt(evaluation.getTotalMarks());
            String header = evalName + " (" + maxMarks + ")";
            Cell evalCell = headerRow.createCell(evaluationColumnIndex);
            evalCell.setCellValue(header);
            evalCell.setCellStyle(headerCellStyle); // Set cell style
            evaluationMaxMarksMap.put(evalName, maxMarks);

            // Adjust column width based on header length
            int headerLength = header.length();
            sheet.setColumnWidth(evaluationColumnIndex, (headerLength + 2) * 256);

            evaluationColumnIndex++;
        }

        headerRow.createCell(evaluationColumnIndex).setCellValue("Total");
        headerRow.createCell(evaluationColumnIndex + 1).setCellValue("Obtained");
        headerRow.createCell(evaluationColumnIndex + 2).setCellValue("Percentage");

        for (Cell cell : headerRow) {
            cell.setCellStyle(headerCellStyle);
        }

        sheet.setColumnWidth(0, 4 * 256);
        int maxNameLength = getMaxNameLength(evaluationRecords);
        int maxRollNoLength = getMaxRollNoLength(evaluationRecords);
        sheet.setColumnWidth(1, (maxNameLength + 5) * 256);
        sheet.setColumnWidth(2, (maxRollNoLength + 2) * 256);
        sheet.setColumnWidth(3, 12 * 256);
        sheet.setColumnWidth(4, 12 * 256);
        sheet.setColumnWidth(5, 12 * 256);
        sheet.setColumnWidth(6, 12 * 256);
        sheet.setColumnWidth(7, 15 * 256);
        sheet.setColumnWidth(evaluationColumnIndex, 10 * 256);
        sheet.setColumnWidth(evaluationColumnIndex + 1, 10 * 256);
        sheet.setColumnWidth(evaluationColumnIndex + 2, 10 * 256);
    }

    private static int getMaxRollNoLength(List<CourseStudentDetailsModel> evaluationRecords) {
        return evaluationRecords.stream()
                .map(CourseStudentDetailsModel::getStudentRollNo)
                .mapToInt(String::length)
                .max().orElse(0);
    }


    private static void populateDataRows(Sheet sheet, @NonNull List<CourseStudentDetailsModel> evaluationRecords) {
        int maxEvaluations = getMaxEvaluations(evaluationRecords);

        int rowNum = 11;
        for (CourseStudentDetailsModel record : evaluationRecords) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(rowNum - 11);
            String studentName = record.getStudentName();
            studentName = studentName.replaceAll("(?i)(muhammad|mohammad)", "M.");
            row.createCell(1).setCellValue(studentName);
            row.createCell(2).setCellValue(record.getStudentRollNo());
            row.createCell(3).setCellValue(record.getTotalCount());
            row.createCell(4).setCellValue(record.getPresents());
            row.createCell(5).setCellValue(record.getAbsents());
            row.createCell(6).setCellValue(record.getLeaves());
            row.createCell(7).setCellValue(formatPercentage(record.getPresentPercentage())); // Attendance %

            List<StudentEvaluationDetailsModel> evaluationList = record.getEvaluationDetailsList();
            float totalObtained = 0;
            float totalMarks = 0;
            int columnIndex = 8;
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

            row.createCell(maxEvaluations + 8).setCellValue(finalTotalMarks); // Total marks
            row.createCell(maxEvaluations + 9).setCellValue(finalTotalObtained); // Obtained marks
            row.createCell(maxEvaluations + 10).setCellValue(finalPercentage); // Percentage

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

        for (int i = 0; i < maxEvaluations + 11; i++) {
            adjustColumnWidths(sheet, maxEvaluations, evaluationRecords);
        }
    }


    private static void adjustColumnWidths(Sheet sheet, int maxEvaluations, List<CourseStudentDetailsModel> evaluationRecords) {
        int maxLengthName = getMaxNameLength(evaluationRecords);
        sheet.setColumnWidth(1, Math.max(12 * 256, (maxLengthName + 5) * 256));

        for (int i = 0; i < maxEvaluations + 6; i++) {
            int maxLength = getMaxCellContentLength(sheet, i);
            if (i == 0) {
                maxLength = Math.min(maxLength, 3);
            }
            if (i != 1 && i != 6) {
                sheet.setColumnWidth(i, Math.max(12 * 256, (maxLength + 1) * 256));
            }
        }
    }

    private static int getMaxNameLength(List<CourseStudentDetailsModel> evaluationRecords) {
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

        String fileName = "Complete Report " + " — " +courseName + repeaterStatus + " — " + className + " — " + semesterName + ".xlsx";

        File documentsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if (documentsDir != null) {
            File file = new File(documentsDir, fileName);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
                workbook.close();
                return FileProvider.getUriForFile(context, "com.nextgen.hasnatfyp.provider", file);
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

    @NonNull
    private static String formatPercentage(float percentage) {
        return String.format("%.0f", percentage);
    }
}
