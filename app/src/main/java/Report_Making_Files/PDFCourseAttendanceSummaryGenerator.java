package Report_Making_Files;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;
import com.nextgen.hasnatfyp.R;
import com.nextgen.hasnatfyp.TeacherInstanceModel;
import com.nextgen.hasnatfyp.UserInstituteModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import Display_Course_Attendance_Activity.StudentAttendanceModel;
import Display_Course_Attendance_Activity.StudentAttendanceRecordModel;

public class PDFCourseAttendanceSummaryGenerator {
    private static final int LOGO_RESOURCE_ID = R.drawable.logo;

    static String TeacherName;

    public static Uri generatePdf(@NonNull List<StudentAttendanceRecordModel> attendanceRecords, Context context, boolean areRepeaters) {
        TeacherInstanceModel teacherInstanceModel = TeacherInstanceModel.getInstance(context);
        String courseN = teacherInstanceModel.getCourseName();
        String classN = teacherInstanceModel.getClassName();
        String semesterN = teacherInstanceModel.getSemesterName();

        String repeaterStatus = areRepeaters ? "(Repeaters)" : "";

        String fileName = "Attendance Report — " + courseN + repeaterStatus + " — " + classN + " — " + semesterN +".pdf";

        File pdfFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName);
        Uri pdfUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", pdfFile);
        try {
            PdfDocument pdfDoc = new PdfDocument(new PdfWriter(new FileOutputStream(pdfFile)));
            Document document = new Document(pdfDoc, PageSize.LETTER);

                String customFontFileName = "calibri-regular.ttf";
                PdfFont font = loadCustomFontFromAssets(context, customFontFileName);


            addLogoToDocument(document, context);
            addHeaderToDocument(document, font,context,semesterN);
            addCourseDetailsToDocument(document, font,repeaterStatus,courseN,classN);
            addAttendanceTableToDocument(document, attendanceRecords, font);
            document.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(context, "Failed to generate PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return pdfUri;
    }
    private static void addLogoToDocument(Document document, Context context) {
        try (@SuppressLint("ResourceType") InputStream inputStream = context.getResources().openRawResource(LOGO_RESOURCE_ID);
             ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            Bitmap bmp = BitmapFactory.decodeStream(inputStream);
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bmp, (int) (bmp.getWidth() * 0.2f), (int) (bmp.getHeight() * 0.2f), true);
            scaledBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

            Image logo = new Image(ImageDataFactory.create(stream.toByteArray()));
            logo.setHorizontalAlignment(HorizontalAlignment.CENTER);
            document.add(logo);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static PdfFont loadCustomFontFromAssets(Context context, String fontFileName) throws IOException {
        return PdfFontFactory.createFont("assets/" + fontFileName, true);
    }
    private static void addHeaderToDocument(Document document, PdfFont font, Context context, String semesterN) {
        String CampusName;

        if(UserInstituteModel.getInstance(context).isSoloUser())
        {
            CampusName = UserInstituteModel.getInstance(context).getCampusName();
            TeacherName = UserInstituteModel.getInstance(context).getAdminName();


        }
        else
        {
            CampusName = TeacherInstanceModel.getInstance(context).getInstituteName();
            TeacherName = TeacherInstanceModel.getInstance(context).getTeacherName();

        }
        Paragraph universityName = new Paragraph(CampusName)
                .setFont(font)
                .setFontSize(18)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(20);
        document.add(universityName);

        Paragraph resultReport = new Paragraph("Course Attendance Report")
                .setFont(font)
                .setFontSize(16)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(2);
        document.add(resultReport);

        Paragraph semesterName = new Paragraph(semesterN)
                .setFont(font)
                .setFontSize(14)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10);
        document.add(semesterName);
    }



    private static void addCourseDetailsToDocument(Document document, PdfFont font, String repeaterStatus, String courseN, String classN) {
        Color borderColor = new DeviceRgb(79, 129, 189);

        Div container = new Div()
                .setBackgroundColor(ColorConstants.WHITE)
                .setBorder(new SolidBorder(borderColor, 2))
                .setPadding(10)
                .setMarginBottom(20)
                .setWidth(300)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);

        Paragraph courseParagraph = new Paragraph()
                .add(new Text("Course : ").setFont(font).setBold())
                .add(new Text(courseN + repeaterStatus + " — " + classN).setFont(font))
                .setFontSize(10)
                .setFontColor(ColorConstants.BLACK);

        Paragraph teacherParagraph = new Paragraph()
                .add(new Text("Teacher Name: ").setFont(font).setBold())
                .add(new Text(TeacherName).setFont(font))
                .setFontSize(10)
                .setFontColor(ColorConstants.BLACK);

        container.add(courseParagraph);
        container.add(new LineSeparator(new SolidLine()));
        container.add(teacherParagraph);

        document.add(container);
    }


    private static void addAttendanceTableToDocument(Document document, List<StudentAttendanceRecordModel> attendanceRecords, PdfFont font) {
        float maxNameWidth = 160;

        Table resultTable = new Table(new float[]{1, maxNameWidth, 2, 1, 1, 1, 1, 3}) // Adjusted column widths
                .setWidth(550) // Increase the width of the table
                .setHorizontalAlignment(HorizontalAlignment.CENTER)
                .setMarginBottom(20);

        Color accent4First = new DeviceRgb(68, 114, 196); // Accent 4 background color
        Color fontColorWhite = ColorConstants.WHITE; // Font color white

        addTableHeader(resultTable, font, accent4First, fontColorWhite, maxNameWidth);

        Color accent4oddRecord = new DeviceRgb(217, 226, 243); // Accent 4 background color
        Color accent4EventRecord = new DeviceRgb(255, 255, 255); // Accent 4 background color

        int serialNumber = 1;
        for (StudentAttendanceRecordModel record : attendanceRecords) {
            addTableRow(resultTable, record, font, serialNumber++, accent4oddRecord, accent4EventRecord);
        }

        document.add(resultTable);
    }

    private static void addTableHeader(Table table, PdfFont font, Color backgroundColor, Color fontColor, float maxNameWidth) {
        table.addCell(createCell("Sr#", font, 30, backgroundColor, fontColor));
        table.addCell(createCell("Student Name", font, maxNameWidth, backgroundColor, fontColor));
        table.addCell(createCell("Roll No", font, 80, backgroundColor, fontColor));
        table.addCell(createCell("Total", font, 40, backgroundColor, fontColor));
        table.addCell(createCell("Presents", font, 40, backgroundColor, fontColor));
        table.addCell(createCell("Absents", font, 40, backgroundColor, fontColor));
        table.addCell(createCell("Leaves", font, 40, backgroundColor, fontColor));
        table.addCell(createCell("Percentage", font, 120, backgroundColor, fontColor));
    }

    @SuppressLint("DefaultLocale")
    private static void addTableRow(Table table, StudentAttendanceRecordModel record, PdfFont font, int serialNumber, Color oddColor, Color evenColor) {
        String name = record.getName().replaceAll("(?i)muhammad", "M.").replaceAll("(?i)mohammad", "M.");
        int presents = countStatusOccurrences(record.getAttendanceList(), "P");
        int absents = countStatusOccurrences(record.getAttendanceList(), "A");
        int leaves = countStatusOccurrences(record.getAttendanceList(), "L");
        double percentage = calculatePercentage(presents, absents, leaves);
        int total = presents + absents + leaves;

        Color backgroundColor = (serialNumber % 2 == 0) ? oddColor : evenColor;

        table.addCell(createCell(String.valueOf(serialNumber), font, backgroundColor));
        table.addCell(createCell(name, font, backgroundColor));
        table.addCell(createCell(record.getStudentRollNo(), font, backgroundColor));
        table.addCell(createCell(String.valueOf(total), font, backgroundColor));
        table.addCell(createCell(String.valueOf(presents), font, backgroundColor));
        table.addCell(createCell(String.valueOf(absents), font, backgroundColor));
        table.addCell(createCell(String.valueOf(leaves), font, backgroundColor));
        table.addCell(createCell(String.format("%.2f", percentage) + "%", font, backgroundColor));
    }

    private static Cell createCell(String text, PdfFont font, Color backgroundColor) {
        return new Cell().add(new Paragraph(text).setFont(font).setFontSize(8)).setTextAlignment(TextAlignment.CENTER).setBackgroundColor(backgroundColor);
    }

    private static Cell createCell(String text, PdfFont font, float width, Color backgroundColor, Color fontColor) {
        return new Cell().add(new Paragraph(text).setFont(font).setFontSize(8)).setWidth(width).setTextAlignment(TextAlignment.CENTER).setBackgroundColor(backgroundColor).setFontColor(fontColor);
    }

    private static int countStatusOccurrences(List<StudentAttendanceModel> attendanceList, String status) {
        int count = 0;
        for (StudentAttendanceModel attendance : attendanceList) {
            if (attendance.getAttendanceStatus().equals(status)) {
                count++;
            }
        }
        return count;
    }

    private static double calculatePercentage(int presents, int absents, int leaves) {
        int total = presents + absents + leaves;
        return total == 0 ? 0 : (presents * 100.0) / total;
    }
}
