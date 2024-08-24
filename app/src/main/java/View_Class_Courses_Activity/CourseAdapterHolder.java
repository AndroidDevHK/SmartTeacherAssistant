package View_Class_Courses_Activity;

public class CourseAdapterHolder {
    private static CourseAdapter courseAdapterInstance;

    public static CourseAdapter getCourseAdapterInstance() {
        return courseAdapterInstance;
    }

    public static void setCourseAdapterInstance(CourseAdapter adapter) {
        courseAdapterInstance = adapter;
    }
}
