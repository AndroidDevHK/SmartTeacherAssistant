package com.nextgen.hasnatfyp;

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

import View_Class_Students_Activity.StudentModel;

public class PDFClassStudentListGenerator {
    private static final int LOGO_RESOURCE_ID = R.drawable.logo;

    public static Uri generatePdf(@NonNull List<StudentModel> studentList, Context context) {
        UserInstituteModel userInstituteModel = UserInstituteModel.getInstance(context);
        String classN = userInstituteModel.getClassName();
        String semesterN = userInstituteModel.getSemesterName();

        String fileName = "Class Student List — " + classN + " — " + semesterN + ".pdf";

        File pdfFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName);
        Uri pdfUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", pdfFile);
        try {
            PdfDocument pdfDoc = new PdfDocument(new PdfWriter(new FileOutputStream(pdfFile)));
            Document document = new Document(pdfDoc, PageSize.LETTER);

            String customFontFileName = "calibri-regular.ttf";
            PdfFont font = loadCustomFontFromAssets(context, customFontFileName);

            addLogoToDocument(document, context);
            addHeaderToDocument(document, font, context, classN, semesterN);
            addClassDetailsToDocument(document, font, classN, semesterN);
            addStudentTableToDocument(document, studentList, font);
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

    private static void addHeaderToDocument(Document document, PdfFont font, Context context, String classN, String semesterN) {
        String campusName;
         campusName = UserInstituteModel.getInstance(context).getCampusName();
        Paragraph universityName = new Paragraph(campusName)
                .setFont(font)
                .setFontSize(18)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(20);
        document.add(universityName);

        Paragraph reportTitle = new Paragraph("Class Student List")
                .setFont(font)
                .setFontSize(16)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(2);
        document.add(reportTitle);

        Paragraph semesterName = new Paragraph(semesterN)
                .setFont(font)
                .setFontSize(14)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10);
        document.add(semesterName);
    }

    private static void addClassDetailsToDocument(Document document, PdfFont font, String classN, String semesterN) {
        Color borderColor = new DeviceRgb(79, 129, 189);

        Div container = new Div()
                .setBackgroundColor(ColorConstants.WHITE)
                .setBorder(new SolidBorder(borderColor, 2))
                .setPadding(10)
                .setMarginBottom(20)
                .setWidth(300)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);

        Paragraph classParagraph = new Paragraph()
                .add(new Text("Class: ").setFont(font).setBold())
                .add(new Text(classN).setFont(font))
                .setFontSize(10)
                .setFontColor(ColorConstants.BLACK);

        Paragraph semesterParagraph = new Paragraph()
                .add(new Text("Semester: ").setFont(font).setBold())
                .add(new Text(semesterN).setFont(font))
                .setFontSize(10)
                .setFontColor(ColorConstants.BLACK);

        container.add(classParagraph);
        container.add(new LineSeparator(new SolidLine()));
        container.add(semesterParagraph);

        document.add(container);
    }

    private static void addStudentTableToDocument(Document document, List<StudentModel> studentList, PdfFont font) {
        Table studentTable = new Table(new float[]{1, 4, 2, 4}) // Adjusted column widths
                .setWidth(520) // Set the width of the table
                .setHorizontalAlignment(HorizontalAlignment.CENTER)
                .setMarginBottom(20);

        Color headerColor = new DeviceRgb(68, 114, 196); // Header background color
        Color fontColorWhite = ColorConstants.WHITE; // Font color white

        addTableHeader(studentTable, font, headerColor, fontColorWhite);

        Color oddRowColor = new DeviceRgb(217, 226, 243); // Odd row background color
        Color evenRowColor = new DeviceRgb(255, 255, 255); // Even row background color

        int serialNumber = 1;
        for (StudentModel student : studentList) {
            addTableRow(studentTable, student, font, serialNumber++, oddRowColor, evenRowColor);
        }

        document.add(studentTable);
    }

    private static void addTableHeader(Table table, PdfFont font, Color backgroundColor, Color fontColor) {
        table.addCell(createCell("Sr#", font, 40, backgroundColor, fontColor));
        table.addCell(createCell("Student Name", font, 200, backgroundColor, fontColor));
        table.addCell(createCell("Roll No", font, 100, backgroundColor, fontColor));
        table.addCell(createCell("Student User ID", font, 180, backgroundColor, fontColor));
    }

    private static void addTableRow(Table table, StudentModel student, PdfFont font, int serialNumber, Color oddColor, Color evenColor) {
        Color backgroundColor = (serialNumber % 2 == 0) ? oddColor : evenColor;

        table.addCell(createCell(String.valueOf(serialNumber), font, backgroundColor));
        table.addCell(createCell(student.getStudentName(), font, backgroundColor));
        table.addCell(createCell(student.getRollNo(), font, backgroundColor));
        table.addCell(createCell(student.getStudentUserID(), font, backgroundColor));
    }

    private static Cell createCell(String text, PdfFont font, Color backgroundColor) {
        return new Cell().add(new Paragraph(text).setFont(font).setFontSize(10))
                .setTextAlignment(TextAlignment.CENTER)
                .setBackgroundColor(backgroundColor);
    }

    private static Cell createCell(String text, PdfFont font, float width, Color backgroundColor, Color fontColor) {
        return new Cell().add(new Paragraph(text).setFont(font).setFontSize(10))
                .setWidth(width)
                .setTextAlignment(TextAlignment.CENTER)
                .setBackgroundColor(backgroundColor)
                .setFontColor(fontColor);
    }
}
