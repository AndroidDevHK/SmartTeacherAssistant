package com.nextgen.hasnatfyp;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import View_Class_Students_Activity.StudentModel;

public class StudentSortHelper {

    public static void sortByRollNumber(List<StudentModel> studentList) {
        Collections.sort(studentList, Comparator.comparing(StudentModel::getRollNo));
    }
}
