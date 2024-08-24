package Display_Course_Repeaters_Activity;

import java.util.List;

import View_Class_Students_Activity.StudentModel;

public class RepeaterClassStudentsModel {
    private String classId;
    private String className;
    private List<StudentModel> students;

    public RepeaterClassStudentsModel(String classId, String className, List<StudentModel> students) {
        this.classId = classId;
        this.className = className;
        this.students = students;
    }

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<StudentModel> getStudents() {
        return students;
    }

    public void setStudents(List<StudentModel> students) {
        this.students = students;
    }
}

