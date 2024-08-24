package Display_Teacher_Courses_Attendance_Activity;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import android.provider.MediaStore;

import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.colors.ColorConstants;
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
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;
import com.nextgen.hasnatfyp.R;

import java.io.File;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class DisplayTeacherCoursesAttendance extends AppCompatActivity {

    private static final String TAG = "TeacherAttendance";
    private FirebaseFirestore db;
    private List<TeacherCourseAttendanceModel> courseAttendanceList;
    private RecyclerView recyclerView;
    private TeacherCourseAttendanceAdapter adapter;

    String SemesterStartDate;
    String SemesterEndDate;
    private String firstAttendanceDate; // First attendance date
    private String lastAttendanceDate; // Last attendance date

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_teacher_courses_attendance);

        SemesterStartDate = "11/03/24";
        SemesterEndDate = "19/07/24";
        db = FirebaseFirestore.getInstance();
        courseAttendanceList = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerView);
        adapter = new TeacherCourseAttendanceAdapter(courseAttendanceList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Replace the teacherUsername and semesterId with actual values
        retrieveTeacherCoursesAttendance("hasnat", "jZIK19Zi0qTIP1iVoXyh");

        Button generateReportButton = findViewById(R.id.generateReportButton);
        generateReportButton.setOnClickListener(v -> {
            try {
                generatePdf(adapter);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void generatePdf(TeacherCourseAttendanceAdapter adapter) throws FileNotFoundException {
        // Get the list from the adapter
        List<TeacherCourseAttendanceModel> courseAttendanceList = adapter.getCourseAttendanceList();

        // Create a new PDF document
        String outputFile = getOutputFile();
        PdfDocument pdfDoc = new PdfDocument(new PdfWriter(outputFile));

        // Set document size (A3)
        PageSize pageSize = PageSize.A3;
        Document document = new Document(pdfDoc, pageSize);

        try {
            PdfFont fonta = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

            addLogoToDocument(document);

            Paragraph universityName = new Paragraph("Govt. Gordon Graduate College Rawalpindi.")
                    .setFont(fonta)
                    .setFontSize(22) // Increased font size
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(20); // Increased margin top for spacing
            document.add(universityName);

            Paragraph resultReport = new Paragraph("Teacher Attendance Report")
                    .setFont(fonta)
                    .setFontSize(20) // Increased font size
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(10); // Reduced margin bottom for spacing
            document.add(resultReport);

            Paragraph semester = new Paragraph("Fall 2023")
                    .setFont(fonta)
                    .setFontSize(18) // Increased font size
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20); // Reduced margin bottom for spacing
            document.add(semester);

            PdfFont font = PdfFontFactory.createFont(StandardFonts.TIMES_ITALIC);


            Color borderColor = new DeviceRgb(79, 129, 189); // Accent 3 color

            float padding = 10;

            Div container = new Div()
                    .setBackgroundColor(ColorConstants.WHITE)
                    .setBorder(new SolidBorder(borderColor, 2)) // Blue border with increased size
                    .setPadding(padding)
                    .setMarginBottom(20) // Add spacing below the Div
                    .setWidth(500)
                    .setHorizontalAlignment(HorizontalAlignment.CENTER); // Center horizontally


            Paragraph teacherName = new Paragraph("UserID: saleem128")
                    .setFont(font)
                    .setFontSize(14)
                    .setFontColor(ColorConstants.BLACK);
            container.add(teacherName);


            LineSeparator separator = new LineSeparator(new SolidLine()); // Blue solid line separator
            container.add(separator);


            Paragraph userID = new Paragraph("Teacher Name: Prof. Muhammad Saleem Bhatti")
                    .setFont(font)
                    .setFontSize(14)
                    .setFontColor(ColorConstants.BLACK);
            container.add(userID);


            document.add(container);




            com.itextpdf.layout.element.Table resultTable = new com.itextpdf.layout.element.Table(new float[]{1, 2, 2, 1, 1})
                    .setWidth(770) // Adjusted width to fit the larger page
                    .setHorizontalAlignment(HorizontalAlignment.CENTER)
                    .setMarginBottom(20);
            DeviceRgb accentColor = new DeviceRgb(79, 129, 189); // You may adjust these values to match the Accent 5 theme
            Color accentColorLighter = new DeviceRgb(224, 236, 255); // Lighter shade of Accent 5 color

            resultTable.addCell(new Cell().add(new Paragraph("Course").setFont(font).setFontSize(14).setFontColor(ColorConstants.WHITE).setBold()).setBackgroundColor(accentColor));
            resultTable.addCell(new Cell().add(new Paragraph("Class").setFont(font).setFontSize(14).setFontColor(ColorConstants.WHITE).setBold()).setBackgroundColor(accentColor));
            Paragraph classesTakenRequiredHeader = new Paragraph("C.Taken / Req.")
                    .setFont(font)
                    .setFontSize(14)
                    .setFontColor(ColorConstants.WHITE)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER);
            resultTable.addCell(new Cell().add(classesTakenRequiredHeader).setBackgroundColor(accentColor));
            Paragraph percentageHeader = new Paragraph("Percentage")
                    .setFont(font)
                    .setFontSize(14)
                    .setFontColor(ColorConstants.WHITE)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER);
            resultTable.addCell(new Cell().add(percentageHeader).setBackgroundColor(accentColor));

            Paragraph attendanceText = new Paragraph("Attendance Duration")
                    .setFont(font)
                    .setFontSize(14)
                    .setFontColor(ColorConstants.WHITE)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER);
            resultTable.addCell(new Cell().add(attendanceText).setBackgroundColor(accentColor));

            // Iterate through the course attendance list and add data to the table
            for (TeacherCourseAttendanceModel attendanceModel : courseAttendanceList) {
                // Create a new row
                resultTable.addCell(new Cell().add(new Paragraph(attendanceModel.getCourseName()).setFont(font).setFontSize(14).setFontColor(ColorConstants.BLACK)).setBackgroundColor(accentColorLighter));
                resultTable.addCell(new Cell().add(new Paragraph(attendanceModel.getClassName()).setFont(font).setFontSize(14).setFontColor(ColorConstants.BLACK)).setBackgroundColor(accentColorLighter));

                // Center alignment for the "Classes Taken / Required" column
                Paragraph classesTakenRequired = new Paragraph(attendanceModel.getClassesTaken() + " / " + attendanceModel.getExpectedClasses())
                        .setFont(font)
                        .setFontSize(14)
                        .setFontColor(ColorConstants.BLACK)
                        .setTextAlignment(TextAlignment.CENTER);
                resultTable.addCell(new Cell().add(classesTakenRequired).setBackgroundColor(accentColorLighter));


                Paragraph percentageValue = new Paragraph(String.format("%.2f%%", attendanceModel.calculatePercentage()))
                        .setFont(font)
                        .setFontSize(14)
                        .setFontColor(ColorConstants.BLACK)
                        .setTextAlignment(TextAlignment.CENTER);
                resultTable.addCell(new Cell().add(percentageValue).setBackgroundColor(accentColorLighter));

                Paragraph attendanceDateRange = new Paragraph(attendanceModel.getFirstAttendanceDate() + " to " + attendanceModel.getLastAttendanceDate())
                        .setFont(font)
                        .setFontSize(14)
                        .setFontColor(ColorConstants.BLACK)
                        .setTextAlignment(TextAlignment.CENTER);
                resultTable.addCell(new Cell().add(attendanceDateRange).setBackgroundColor(accentColorLighter));
            }


            document.add(resultTable);

            // Close the document
            document.close();

            Toast.makeText(this, "PDF generated successfully", Toast.LENGTH_SHORT).show();

            // Insert the PDF file into MediaStore
            insertPdfIntoMediaStore(outputFile);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to generate PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    private String getOutputFile() {
        // Create a PDF file in the cache directory
        File directory = new File(getExternalCacheDir(), "MyPDFs");
        if (!directory.exists()) {
            boolean isDirectoryCreated = directory.mkdirs();
            if (!isDirectoryCreated) {
                Toast.makeText(this, "Failed to create directory", Toast.LENGTH_SHORT).show();
                return null;
            }
        }
        return directory.getAbsolutePath() + "/teacher_attendance_report.pdf";
    }

    private void addLogoToDocument(Document document) {
        try {
            // Load the image from resources
            @SuppressLint("ResourceType") InputStream inputStream = getResources().openRawResource(R.drawable.logo);
            Bitmap bmp = BitmapFactory.decodeStream(inputStream);

            // Scale the bitmap to reduce the size
            float scale = 0.3f; // Adjust the scale factor as needed
            int newWidth = (int) (bmp.getWidth() * scale);
            int newHeight = (int) (bmp.getHeight() * scale);
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bmp, newWidth, newHeight, true);

            // Convert Bitmap to byte array
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            scaledBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();

            // Create Image element
            Image logo = new Image(ImageDataFactory.create(byteArray));
            logo.setHorizontalAlignment(HorizontalAlignment.CENTER);
            document.add(logo);

            // Close the streams
            inputStream.close();
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void insertPdfIntoMediaStore(String filePath) {
        ContentResolver contentResolver = getContentResolver();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Files.FileColumns.DISPLAY_NAME, "teacher_attendance_report.pdf");
        contentValues.put(MediaStore.Files.FileColumns.MIME_TYPE, "application/pdf");
        contentValues.put(MediaStore.Files.FileColumns.DATA, filePath);
        Uri uri = contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues);
        if (uri != null) {
            Toast.makeText(this, "PDF file inserted into MediaStore", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to insert PDF file into MediaStore", Toast.LENGTH_SHORT).show();
        }
    }

    private void retrieveTeacherCoursesAttendance(String teacherUsername, String semesterId) {
        db.collection("TeacherCourses")
                .whereEqualTo("TeacherUsername", teacherUsername)
                .whereEqualTo("SemesterID", semesterId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            String courseId = document.getString("CourseID");
                            String classId = document.getString("ClassID");

                            // Retrieve course and class information
                            retrieveCourseAndClassInfo(courseId, classId, teacherUsername);
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }

    private void retrieveCourseAndClassInfo(String courseId, String classId, String teacherUsername) {
        db.collection("ClassCourses").document(classId)
                .collection("ClassCoursesSubcollection").document(courseId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot courseDoc = task.getResult();
                        if (courseDoc.exists()) {
                            String courseName = courseDoc.getString("CourseName");
                            String creditHoursStr = courseDoc.getString("CreditHours");
                            int creditHours = Integer.parseInt(creditHoursStr);

                            // Retrieve class name
                            retrieveClassName(classId, teacherUsername, courseId, courseName, creditHours);
                        } else {
                            Log.d(TAG, "Course document does not exist.");
                        }
                    } else {
                        Log.d(TAG, "Error retrieving course document: ", task.getException());
                    }
                });
    }

    private void retrieveClassName(String classId, String teacherUsername, String courseId, String courseName, int creditHours) {
        db.collection("Classes").document(classId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot classDoc = task.getResult();
                        if (classDoc.exists()) {
                            String className = classDoc.getString("ClassName");

                            retrieveAttendanceData(courseId, className, courseName, creditHours);
                        } else {
                            Log.d(TAG, "Class document does not exist.");
                        }
                    } else {
                        Log.d(TAG, "Error retrieving class document: ", task.getException());
                    }
                });
    }

    private void retrieveAttendanceData(String courseId, String className, String courseName, int creditHours) {
        db.collection("CourseAttendance")
                .whereEqualTo("CourseID", courseId)
                .whereEqualTo("IsRepeater", false)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int totalAttendanceToProcess = task.getResult().size(); // Get total attendance records
                        AtomicInteger attendanceProcessedCount = new AtomicInteger(0); // Initialize processed count

                        List<String> allAttendanceDates = new ArrayList<>(); // List to store all attendance dates

                        for (DocumentSnapshot document : task.getResult()) {
                            String attendanceId = document.getString("AttendanceID");
                            readAttendanceData(attendanceId, new AttendanceDateCallback() {
                                @Override
                                public void onAttendanceDateRead(String attendanceDate) {
                                    allAttendanceDates.add(attendanceDate);
                                }

                                @Override
                                public void onAllAttendanceDatesRead() {
                                    attendanceProcessedCount.getAndIncrement();

                                    if (attendanceProcessedCount.get() == totalAttendanceToProcess) {
                                        Collections.sort(allAttendanceDates);

                                        String firstAttendanceDate = allAttendanceDates.get(0);
                                        String lastAttendanceDate = allAttendanceDates.get(allAttendanceDates.size() - 1);

                                        processAttendanceData(allAttendanceDates, courseId, className, courseName, creditHours, firstAttendanceDate, lastAttendanceDate);
                                    }
                                }
                            });
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }

    private void readAttendanceData(String attendanceId, AttendanceDateCallback callback) {
        db.collection("Attendance").document(attendanceId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String attendanceDate = documentSnapshot.getString("AttendanceDate");
                        if (attendanceDate != null) {
                            // Notify callback for each attendance date read
                            callback.onAttendanceDateRead(attendanceDate);
                        }
                    } else {
                        Log.e(TAG, "Document does not exist for AttendanceID: " + attendanceId);
                    }
                })
                .addOnCompleteListener(task -> {
                    // Notify callback when all attendance dates are read
                    callback.onAllAttendanceDatesRead();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error reading document: ", e));
    }

    private void processAttendanceData(List<String> allAttendanceDates, String courseId, String className, String courseName, int creditHours, String firstAttendanceDate, String lastAttendanceDate) {
        // Create teacher class model and add to list
        List<String> teacherAttendance = new ArrayList<>(allAttendanceDates);
        TeacherCourseAttendanceModel attendanceModel = new TeacherCourseAttendanceModel(
                allAttendanceDates.size(), // Number of classes taken
                className,
                courseName,
                creditHours,
                firstAttendanceDate,
                lastAttendanceDate,
                teacherAttendance, // Teacher attendance list
                0, // Initialize expected classes to 0
                0.0 // Initialize percentage to 0.0
        );
        courseAttendanceList.add(attendanceModel);

        // Notify adapter after adding the attendance model
        adapter.notifyDataSetChanged();
    }
    interface AttendanceDateCallback {
        void onAttendanceDateRead(String attendanceDate);
        void onAllAttendanceDatesRead();
    }

}
