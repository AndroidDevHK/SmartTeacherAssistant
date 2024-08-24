package Report_Making_Files;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
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
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.property.BorderRadius;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import com.nextgen.hasnatfyp.R;
import com.nextgen.hasnatfyp.UserInstituteModel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;

import Display_Semester_Teacher_All_Courses_Attendance.SemesterTeachersAttendanceModel;
import Display_Teacher_Courses_Attendance_Activity.TeacherCourseAttendanceModel;

public class SemesterTeachersAttendanceReportGenerator {



    private static final DeviceRgb ACCENT_COLOR = new DeviceRgb(0, 176, 240);
    private static final DeviceRgb TABLE_HEADER_COLOR = new DeviceRgb(255, 255, 255);
    private static final DeviceRgb TABLE_ROW_COLOR = new DeviceRgb(242, 242, 242);

    private final Context context;

    public SemesterTeachersAttendanceReportGenerator(Context context) {
        this.context = context;
    }

    public Uri generatePdf(List<SemesterTeachersAttendanceModel> teachersAttendanceList) {
        String FILE_NAME = "Teacher Attendance Report — " + UserInstituteModel.getInstance(context).getSemesterName() + ".pdf";
        String outputFile = getOutputFilePath(FILE_NAME);

        try {
            PdfDocument pdfDoc = new PdfDocument(new PdfWriter(outputFile));
            Document document = new Document(pdfDoc, PageSize.A3);

            PdfFont fonta = PdfFontFactory.createFont();
            addLogoToDocument(document);
            String CampusName;
            if(!UserInstituteModel.getInstance(context).isSoloUser())
            {
                CampusName = UserInstituteModel.getInstance(context).getCampusName();
            }
            else
            {
                CampusName = "Smart Teacher Assistant";

            }
            Paragraph universityName = new Paragraph(CampusName)
                    .setFont(fonta)
                    .setFontSize(22)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(20);
            document.add(universityName);

            Paragraph resultReport = new Paragraph("Teacher Attendance Report")
                    .setFont(fonta)
                    .setFontSize(20)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(10);
            document.add(resultReport);

            Paragraph semester = new Paragraph(UserInstituteModel.getInstance(context).getSemesterName())
                    .setFont(fonta)
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(semester);

            pdfDoc.addEventHandler(PdfDocumentEvent.END_PAGE, event -> {
                PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
                PdfDocument pdfDocument = docEvent.getDocument();
                PdfPage page = docEvent.getPage();

                PdfCanvas canvas = new PdfCanvas(page.newContentStreamBefore(), page.getResources(), pdfDocument);

                canvas.setFontAndSize(fonta, 10);
                canvas.beginText().moveText((PageSize.A3.getWidth() - 120), 20)
                        .showText("Printed Date: " + LocalDate.now()).endText();
                canvas.release();
            });

            for (int i = 0; i < teachersAttendanceList.size(); i++) {
                SemesterTeachersAttendanceModel teacherAttendance = teachersAttendanceList.get(i);

                if (i > 0) {
                    document.add(createLineSeparator());
                }

                document.add(createTeacherInfoCard(teacherAttendance));

                document.add(createResultTable(teacherAttendance, fonta));
            }

            document.close();
            return getUriForFile(outputFile);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Failed to generate PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        return null;
    }

    private String getOutputFilePath(String FILE_NAME) {
        File directory = new File(context.getExternalFilesDir(null), "Documents");
        if (!directory.exists()) {
            boolean isDirectoryCreated = directory.mkdirs();
            if (!isDirectoryCreated) {
                Toast.makeText(context, "Failed to create directory", Toast.LENGTH_SHORT).show();
                return null;
            }
        }
        return new File(directory, FILE_NAME).getAbsolutePath();
    }

    private void addLogoToDocument(Document document) throws IOException {
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

    private LineSeparator createLineSeparator() {
        SolidLine line = new SolidLine(1f);
        line.setColor(ColorConstants.RED);
        return new LineSeparator(line).setMarginBottom(15);
    }

    private Div createTeacherInfoCard(SemesterTeachersAttendanceModel teacherAttendance) throws IOException {
        Div teacherInfoCard = new Div()
                .setBackgroundColor(ColorConstants.WHITE)
                .setBorder(new SolidBorder(ColorConstants.RED, 2))
                .setPadding(10)
                .setMarginBottom(20)
                .setWidth(UnitValue.createPointValue(500))
                .setHorizontalAlignment(HorizontalAlignment.CENTER)
                .setBorderRadius(new BorderRadius(5));

        teacherInfoCard.add(new Paragraph().add(new Text("User: ").setBold())
                .add(new Text(teacherAttendance.getTeacherUserName())));
        teacherInfoCard.add(new LineSeparator(new SolidLine()).setMarginBottom(5));
        teacherInfoCard.add(new Paragraph().add(new Text("Teacher Name: ").setBold())
                .add(new Text(teacherAttendance.getTeacherName())));

        return teacherInfoCard;
    }

    private com.itextpdf.layout.element.Table createResultTable(SemesterTeachersAttendanceModel teacherAttendance, PdfFont fonta) {
        com.itextpdf.layout.element.Table resultTable = new com.itextpdf.layout.element.Table(new float[]{1, 2, 2, 1})
                .setWidth(UnitValue.createPointValue(770))
                .setHorizontalAlignment(HorizontalAlignment.CENTER)
                .setMarginBottom(20);

        resultTable.addCell(new Cell().add(new Paragraph("Course").setFont(fonta).setFontSize(14).setFontColor(TABLE_HEADER_COLOR)).setBackgroundColor(ACCENT_COLOR));
        resultTable.addCell(new Cell().add(new Paragraph("Class").setFont(fonta).setFontSize(14).setFontColor(TABLE_HEADER_COLOR)).setBackgroundColor(ACCENT_COLOR));
        resultTable.addCell(new Cell().add(new Paragraph("Classes Taken").setFont(fonta).setFontSize(14).setFontColor(TABLE_HEADER_COLOR)).setBackgroundColor(ACCENT_COLOR).setTextAlignment(TextAlignment.CENTER));
        resultTable.addCell(new Cell().add(new Paragraph("Attendance Dates").setFont(fonta).setFontSize(14).setFontColor(TABLE_HEADER_COLOR)).setBackgroundColor(ACCENT_COLOR).setTextAlignment(TextAlignment.CENTER));

        for (TeacherCourseAttendanceModel courseAttendance : teacherAttendance.getTeacherAttendance()) {
            resultTable.addCell(new Cell().add(new Paragraph(courseAttendance.getCourseName())).setBackgroundColor(TABLE_ROW_COLOR));
            resultTable.addCell(new Cell().add(new Paragraph(courseAttendance.getClassName())).setBackgroundColor(TABLE_ROW_COLOR));
            resultTable.addCell(new Cell().add(new Paragraph(String.valueOf(courseAttendance.getClassesTaken())).setBackgroundColor(TABLE_ROW_COLOR).setTextAlignment(TextAlignment.CENTER)));
            resultTable.addCell(new Cell().add(new Paragraph(courseAttendance.getFirstAttendanceDate() + " - " + courseAttendance.getLastAttendanceDate())).setBackgroundColor(TABLE_ROW_COLOR).setTextAlignment(TextAlignment.CENTER));
        }

        return resultTable;
    }

    private Uri getUriForFile(String filePath) {
        File file = new File(filePath);
        return FileProvider.getUriForFile(context, "com.nextgen.hasnatfyp.provider", file);
    }
}
