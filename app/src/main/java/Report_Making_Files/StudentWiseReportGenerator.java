package Report_Making_Files;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.property.BorderRadius;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import com.itextpdf.layout.renderer.DocumentRenderer;
import com.nextgen.hasnatfyp.R;
import com.nextgen.hasnatfyp.StudentEvaluationModel;
import com.nextgen.hasnatfyp.UserInstituteModel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;

import DisplayStudentCompleteAttendanceEvaluation_Activity.StudentCourseAttendanceEvaluationModel;

public class StudentWiseReportGenerator {

    private Context context;

    public StudentWiseReportGenerator(Context context) {
        this.context = context;
    }

    public Uri generateReport(String semesterName, String studentName, String RollNo, List<StudentCourseAttendanceEvaluationModel> courseList, String ClassName) {
        try {
            // Create PDF file
            File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), studentName + "("+ RollNo +") " +   semesterName +" — Complete Report.pdf");
            PdfWriter writer = new PdfWriter(new FileOutputStream(file));
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.LETTER);
            document.setMargins(20, 20, 20, 60); // Increased bottom margin to accommodate footer



            String customFontFileName = "calibri-regular.ttf";
            PdfFont font = loadCustomFontFromAssets(context, customFontFileName);

            // Add content to the PDF
            addUniversityLogo(document);
            addUniversityName(document, font);
            addReportTitle(document, font);
            addSemesterName(document, semesterName, font);
            addStudentInfoCard(document, studentName, RollNo, ClassName, font);

            // Add content for each course
            int coursesPerPage = 2;
            boolean isFirstPage = true;

            for (int i = 0; i < courseList.size(); i++) {
                if (isFirstPage) {
                    // First page: Only one course
                    addCourseReport(document, courseList.get(i), font);
                    isFirstPage = false; // Set isFirstPage to false after adding the first course
                } else {
                    // Subsequent pages: Two courses per page with a space between them
                    if ((i - 1) % coursesPerPage == 0) {
                        document.add(new AreaBreak());
                        addDivider(document); // Add a divider between courses
                    } else {
                        addSpace(document); // Add a little space between courses
                    }
                    addCourseReport(document, courseList.get(i), font);
                }
            }

            document.close();

            // Return URI using FileProvider
            return FileProvider.getUriForFile(context, "com.nextgen.hasnatfyp.provider", file);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Failed to generate PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
    }
    private void addSpace(Document document) {
        document.add(new Paragraph("\n")); // Add a newline paragraph for spacing
    }





    private void addUniversityLogo(Document document) throws IOException {
        @SuppressLint("ResourceType") InputStream inputStream = context.getResources().openRawResource(R.drawable.logo);
        Bitmap bmp = BitmapFactory.decodeStream(inputStream);
        float scale = 0.3f;
        int newWidth = (int) (bmp.getWidth() * scale);
        int newHeight = (int) (bmp.getHeight() * scale);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bmp, newWidth, newHeight, true);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scaledBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        Image logo = new Image(ImageDataFactory.create(byteArray));
        logo.setHorizontalAlignment(HorizontalAlignment.CENTER);
        document.add(logo);
        inputStream.close();
        stream.close();
    }

    private void addUniversityName(Document document, PdfFont font) {
        Paragraph universityName = new Paragraph(UserInstituteModel.getInstance(context).getCampusName())
                .setFontColor(ColorConstants.BLACK)
                .setFontSize(22)
                .setFont(font)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(5);
        document.add(universityName);
    }

    private void addReportTitle(Document document, PdfFont font) {
        Paragraph reportTitle = new Paragraph("Student Complete Report")
                .setFontColor(ColorConstants.BLACK)
                .setFont(font)
                .setFontSize(18)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5);
        document.add(reportTitle);
    }

    private void addSemesterName(Document document, String semesterName, PdfFont font) {
        Paragraph semester = new Paragraph(semesterName)
                .setFontColor(ColorConstants.BLACK)
                .setFontSize(16)
                .setFont(font)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5);
        document.add(semester);
    }

    private void addStudentInfoCard(Document document, String studentName, String RollNo, String className, PdfFont font) {
        Div studentInfoCard = new Div()
                .setBackgroundColor(ColorConstants.WHITE)
                .setBorder(new SolidBorder(ColorConstants.RED, 2))
                .setPadding(10)
                .setFont(font)
                .setMarginBottom(20)
                .setWidth(UnitValue.createPointValue(500))
                .setHorizontalAlignment(HorizontalAlignment.CENTER)
                .setBorderRadius(new BorderRadius(5));

        studentInfoCard.add(new Paragraph().add(new Text("Student ").setBold().setFont(font))
                .add(new Text(studentName + " (" + RollNo + ")").setFont(font)));
        studentInfoCard.add(new LineSeparator(new SolidLine()).setMarginBottom(5));
        studentInfoCard.add(new Paragraph().add(new Text("Class : ").setBold().setFont(font))
                .add(new Text(className).setFont(font)));

        document.add(studentInfoCard);
    }

    private static PdfFont loadCustomFontFromAssets(Context context, String fontFileName) throws IOException {
        return PdfFontFactory.createFont("assets/" + fontFileName, true);
    }

    @SuppressLint("DefaultLocale")
    private void addCourseReport(Document document, StudentCourseAttendanceEvaluationModel course, PdfFont font) {
        float[] columnWidths = {250F, 200F, 100F}; // Adjusted widths for course details
        // Create table with adjusted widths
        Table table = new Table(UnitValue.createPercentArray(columnWidths))
                .setWidth(UnitValue.createPercentValue(80)) // Adjusted width percentage
                .setHorizontalAlignment(HorizontalAlignment.CENTER); // Center align the table

        Cell courseNameCell = new Cell(1, 3)
                .add(new Paragraph("Course: " + course.getCourseName()))
                .setBackgroundColor(ColorConstants.YELLOW)
                .setBold()
                .setFont(font)
                .setFontSize(14) // Adjusted font size
                .setTextAlignment(TextAlignment.CENTER);
        table.addCell(courseNameCell);

        Cell evaluationsHeader = new Cell(1, 3)
                .add(new Paragraph("Evaluations"))
                .setBackgroundColor(ColorConstants.YELLOW)
                .setBold()
                .setFont(font)
                .setFontSize(12) // Adjusted font size
                .setTextAlignment(TextAlignment.CENTER);
        table.addCell(evaluationsHeader);

        if (course.getStudentEvalList().isEmpty()) {
            // If no evaluations exist, add a message row
            addNoEvaluationRow(table, font);
        } else {
            // Add evaluation headers and rows
            table.addCell(new Cell().add(new Paragraph("Evaluation Name")).setBackgroundColor(ColorConstants.YELLOW).setBold().setFont(font).setFontSize(10)); // Adjusted font size
            table.addCell(new Cell().add(new Paragraph("Obtained Marks")).setBackgroundColor(ColorConstants.YELLOW).setBold().setFont(font).setFontSize(10)); // Adjusted font size
            table.addCell(new Cell().add(new Paragraph("Total Marks")).setBackgroundColor(ColorConstants.YELLOW).setBold().setFont(font).setFontSize(10)); // Adjusted font size

            // Add evaluation rows
            for (int i = 0; i < course.getStudentEvalList().size(); i++) {
                StudentEvaluationModel evaluation = course.getStudentEvalList().get(i);
                addEvaluationRow(table, (i + 1) + ". " + evaluation.getEvalName(), evaluation.getEvalObtMarks(), evaluation.getEvalTMarks(), font);
            }

            int totalObtained = Integer.parseInt(course.getAllEvaluationObtainedMarks());
            int totalMarks = Integer.parseInt(course.getAllEvaluationTotal());

            table.addCell(new Cell().add(new Paragraph("Total")).setBold().setFont(font).setFontSize(12)); // Adjusted font size
            table.addCell(new Cell().add(new Paragraph(String.valueOf(totalObtained)).setFont(font).setFontSize(10))); // Adjusted font size
            table.addCell(new Cell().add(new Paragraph(String.valueOf(totalMarks)).setFont(font).setFontSize(10))); // Adjusted font size

            Cell percentageCell = new Cell(1, 3).add(new Paragraph("Percentage : " + course.getPercentage())).setFont(font).setFontSize(12); // Adjusted font size
            table.addCell(percentageCell);
        }



        Cell attendanceHeader = new Cell(1, 3)
                .add(new Paragraph("Attendance"))
                .setBackgroundColor(ColorConstants.YELLOW)
                .setBold()
                .setFont(font)
                .setFontSize(12) // Adjusted font size
                .setTextAlignment(TextAlignment.CENTER);
        table.addCell(attendanceHeader);

        table.addCell(new Cell().add(new Paragraph("Total Classes")).setBold().setFont(font).setFontSize(12)); // Adjusted font size
        table.addCell(new Cell().add(new Paragraph("Presents")).setBold().setFont(font).setFontSize(12)); // Adjusted font size
        table.addCell(new Cell().add(new Paragraph("Percentage")).setBold().setFont(font).setFontSize(12)); // Adjusted font size

        table.addCell(new Cell().add(new Paragraph(String.valueOf(course.getTotalCount())).setFont(font).setFontSize(10))); // Adjusted font size
        table.addCell(new Cell().add(new Paragraph(String.valueOf(course.getPresents())).setFont(font).setFontSize(10))); // Adjusted font size
        table.addCell(new Cell().add(new Paragraph(String.format("%.2f%%", course.getPresentPercentage())).setFont(font).setFontSize(10))); // Adjusted font size

        document.add(table);
    }

    private void addNoEvaluationRow(Table table, PdfFont font) {
        table.addCell(new Cell(1, 3)
                .add(new Paragraph("No Evaluation have been added yet"))
                .setFont(font)
                .setFontSize(10) // Adjusted font size
                .setTextAlignment(TextAlignment.CENTER));
    }

    private void addEvaluationRow(Table table, String evaluationName, String obtainedMarks, String totalMarks, PdfFont font) {
        table.addCell(new Cell().add(new Paragraph(evaluationName)).setFont(font));
        table.addCell(new Cell().add(new Paragraph(obtainedMarks)).setFont(font));
        table.addCell(new Cell().add(new Paragraph(totalMarks)).setFont(font));
    }


    private void addDivider(Document document) {
        document.add(new Paragraph("\n"));
    }

}

