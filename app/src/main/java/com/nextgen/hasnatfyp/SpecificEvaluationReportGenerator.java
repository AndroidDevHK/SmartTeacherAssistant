package com.nextgen.hasnatfyp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.itextpdf.io.font.constants.StandardFonts;
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
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import Display_Course_Evaluations_List_Activity.CourseEvaluationInfoModel;

public class SpecificEvaluationReportGenerator {
    private static final int LOGO_RESOURCE_ID = R.drawable.logo;
    static String TeacherName;

    public static Uri generatePdf(Context context, boolean areRepeaters, CourseEvaluationInfoModel evaluationInfo) {

        TeacherInstanceModel teacherInstanceModel = TeacherInstanceModel.getInstance(context);
       String courseN = teacherInstanceModel.getCourseName();
      String classN = teacherInstanceModel.getClassName();
       String semesterN = teacherInstanceModel.getSemesterName();

        String repeaterStatus = areRepeaters ? "(Repeaters)" : "";

        String fileName = evaluationInfo.getEvaluationName() + " — " + courseN + repeaterStatus + " — " + classN + " — " + semesterN + ".pdf";

        File pdfFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName);
        Uri pdfUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", pdfFile);
        try {
            PdfDocument pdfDoc = new PdfDocument(new PdfWriter(new FileOutputStream(pdfFile)));
            Document document = new Document(pdfDoc, PageSize.LETTER);

            PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            addLogoToDocument(document, context);
            addHeaderToDocument(document, font, evaluationInfo,context,semesterN);
            addCourseDetailsToDocument(document, font, repeaterStatus,courseN,classN);
            addAttendanceTableToDocument(document, evaluationInfo, font,evaluationInfo.getEvaluationTotalMarks());
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

    private static void addHeaderToDocument(Document document, PdfFont font, CourseEvaluationInfoModel evaluationInfo, Context context, String semesterN) {
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
        Paragraph universityName = new Paragraph(CampusName).setBold()
                .setFont(font)
                .setFontSize(18)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(20);
        document.add(universityName);

        Paragraph resultReport = new Paragraph(evaluationInfo.getEvaluationName() + " Report").setBold()
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

        container.add(new Paragraph("Course : " + courseN + repeaterStatus + " — " + classN)
                .setFont(font)
                .setFontSize(10)
                .setFontColor(ColorConstants.BLACK));

        container.add(new LineSeparator(new SolidLine()));

        container.add(new Paragraph("Teacher Name: " + TeacherName)
                .setFont(font)
                .setFontSize(10)
                .setFontColor(ColorConstants.BLACK));

        document.add(container);
    }

    private static void addAttendanceTableToDocument(Document document, CourseEvaluationInfoModel evaluationInfo, PdfFont font, double evaluationTotalMarks) {
        float maxNameWidth = 160;
        float Obtained = 100;

        Table resultTable = new Table(new float[]{1, maxNameWidth,Obtained, Obtained, Obtained}) // Adjusted column widths
                .setWidth(400) // Increase the width of the table
                .setHorizontalAlignment(HorizontalAlignment.CENTER)
                .setMarginBottom(20);

        Color accent4First = new DeviceRgb(68, 114, 196); // Accent 4 background color
        Color fontColorWhite = ColorConstants.WHITE; // Font color white

        addTableHeader(resultTable, font, accent4First, fontColorWhite, maxNameWidth,evaluationTotalMarks);

        Color accent4oddRecord = new DeviceRgb(217, 226, 243); // Accent 4 background color
        Color accent4EventRecord = new DeviceRgb(255, 255, 255); // Accent 4 background color

        int serialNumber = 1;
        List<CourseEvaluationDetailsModel> evaluationDetailsList = evaluationInfo.getEvaluationInfoList();
        for (CourseEvaluationDetailsModel evaluationDetails : evaluationDetailsList) {
            addTableRow(resultTable, evaluationDetails, font, serialNumber++, accent4oddRecord, accent4EventRecord,evaluationTotalMarks);
        }

        document.add(resultTable);
    }

    private static void addTableHeader(Table table, PdfFont font, Color backgroundColor, Color fontColor, float maxNameWidth, double evaluationTotalMarks) {
        table.addCell(createCell("Sr#", font, 30, backgroundColor, fontColor));
        table.addCell(createCell("Student Name", font, maxNameWidth, backgroundColor, fontColor));
        table.addCell(createCell("Roll No", font, 50, backgroundColor, fontColor));
        table.addCell(createCell("Obtained Marks", font, 100, backgroundColor, fontColor));
        table.addCell(createCell("Total Marks", font, 100, backgroundColor, fontColor));
    }

    @SuppressLint("DefaultLocale")
    private static void addTableRow(Table table, CourseEvaluationDetailsModel evaluation, PdfFont font, int serialNumber, Color oddColor, Color evenColor, double evaluationTotalMarks) {
        Color backgroundColor = (serialNumber % 2 == 0) ? oddColor : evenColor;

        // Format obtained marks to remove decimal part if it's a whole number
        double obtainedMarks = evaluation.getObtainedMarks();
        String formattedObtainedMarks;
        if (obtainedMarks == (int) obtainedMarks) {
            formattedObtainedMarks = String.valueOf((int) obtainedMarks);
        } else {
            formattedObtainedMarks = String.valueOf(obtainedMarks);
        }
        String formattedTotalMarks;
        if (evaluationTotalMarks == (int) evaluationTotalMarks) {
            formattedTotalMarks = String.valueOf((int) evaluationTotalMarks);
        } else {
            formattedTotalMarks = String.valueOf(evaluationTotalMarks);
        }

        table.addCell(createCell(String.valueOf(serialNumber), font, backgroundColor)); // Serial number
        table.addCell(createCell(evaluation.getStudentName(), font, backgroundColor)); // Student Name
        table.addCell(createCell(evaluation.getStudentRollNo(), font, backgroundColor)); // Student Roll No
        table.addCell(createCell(formattedObtainedMarks, font, backgroundColor)); // Obtained Marks
        table.addCell(createCell(formattedTotalMarks, font, backgroundColor)); // Percentage
    }


    private static Cell createCell(String text, PdfFont font, Color backgroundColor) {
        return new Cell().add(new Paragraph(text).setFont(font).setFontSize(10)).setTextAlignment(TextAlignment.CENTER).setBackgroundColor(backgroundColor);
    }

    private static Cell createCell(String text, PdfFont font, float width, Color backgroundColor, Color fontColor) {
        return new Cell().add(new Paragraph(text).setFont(font).setFontSize(10)).setWidth(width).setTextAlignment(TextAlignment.CENTER).setBackgroundColor(backgroundColor).setFontColor(fontColor);
    }
    private static double calculateMarksPercentage(double obtainedMarks, double totalMarks) {
        double percentage = (obtainedMarks / totalMarks) * 100;
        return Math.round(percentage);
    }


}
