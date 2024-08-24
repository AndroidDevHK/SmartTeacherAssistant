package Report_Making_Files;

import com.nextgen.hasnatfyp.StudentEvaluationDetailsModel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import Display_Complete_Course_Att_Eval_data_Activity.CourseStudentDetailsModel;

public class EvaluationPreprocessHelper2 {

    public static List<CourseStudentDetailsModel> preprocessData(List<CourseStudentDetailsModel> evaluationRecords) {
        List<CourseStudentDetailsModel> preprocessedRecords = new ArrayList<>();

        // Get the order of evaluations from the student with the maximum evaluations
        List<String> evaluationOrder = getMaxEvaluationsOrder(evaluationRecords);

        // Get all unique evaluation names
        Set<String> allEvaluationNames = new HashSet<>(evaluationOrder);

        for (CourseStudentDetailsModel record : evaluationRecords) {
            CourseStudentDetailsModel preprocessedRecord = new CourseStudentDetailsModel();
            preprocessedRecord.setStudentName(record.getStudentName());
            preprocessedRecord.setStudentRollNo(record.getStudentRollNo());

            // Preserve attendance data
            preprocessedRecord.setPresents(record.getPresents());
            preprocessedRecord.setAbsents(record.getAbsents());
            preprocessedRecord.setLeaves(record.getLeaves());
            preprocessedRecord.setTotalCount(record.getTotalCount());
            preprocessedRecord.setPresentPercentage(record.getPresentPercentage());

            // Deep copy of the original evaluation list
            List<StudentEvaluationDetailsModel> preprocessedEvalList = new ArrayList<>(record.getEvaluationDetailsList());

            // Add missing evaluations
            addMissingEvaluations(preprocessedEvalList, allEvaluationNames, evaluationRecords);

            // Sort evaluations based on the predefined order
            sortEvaluations(preprocessedEvalList, evaluationOrder);

            preprocessedRecord.setEvaluationDetailsList(preprocessedEvalList);
            preprocessedRecords.add(preprocessedRecord);
        }

        return preprocessedRecords;
    }

    private static List<String> getMaxEvaluationsOrder(List<CourseStudentDetailsModel> evaluationRecords) {
        int maxEvaluations = 0;
        List<String> evaluationOrder = new ArrayList<>();

        for (CourseStudentDetailsModel record : evaluationRecords) {
            if (record.getEvaluationDetailsList().size() > maxEvaluations) {
                maxEvaluations = record.getEvaluationDetailsList().size();
                evaluationOrder.clear();
                for (StudentEvaluationDetailsModel evaluation : record.getEvaluationDetailsList()) {
                    evaluationOrder.add(evaluation.getEvaluationName());
                }
            }
        }
        return evaluationOrder;
    }

    private static void addMissingEvaluations(List<StudentEvaluationDetailsModel> evalList, Set<String> allEvaluationNames, List<CourseStudentDetailsModel> originalRecords) {
        for (String evalName : allEvaluationNames) {
            boolean found = false;
            for (StudentEvaluationDetailsModel evaluation : evalList) {
                if (evaluation.getEvaluationName().equals(evalName)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                StudentEvaluationDetailsModel missingEval = new StudentEvaluationDetailsModel();
                missingEval.setEvaluationName(evalName);
                missingEval.setObtainedMarks("N/A");
                String totalMarks = findTotalMarksForEvaluation(evalName, originalRecords);
                missingEval.setTotalMarks(totalMarks);
                evalList.add(missingEval);
            }
        }
    }

    private static String findTotalMarksForEvaluation(String evalName, List<CourseStudentDetailsModel> originalRecords) {
        for (CourseStudentDetailsModel record : originalRecords) {
            for (StudentEvaluationDetailsModel evaluation : record.getEvaluationDetailsList()) {
                if (evaluation.getEvaluationName().equals(evalName)) {
                    return evaluation.getTotalMarks();
                }
            }
        }
        return "0";
    }

    private static void sortEvaluations(List<StudentEvaluationDetailsModel> evalList, List<String> evaluationOrder) {
        Map<String, Integer> orderMap = new HashMap<>();
        for (int i = 0; i < evaluationOrder.size(); i++) {
            orderMap.put(evaluationOrder.get(i), i);
        }
        evalList.sort(Comparator.comparingInt(evaluation -> orderMap.getOrDefault(evaluation.getEvaluationName(), Integer.MAX_VALUE)));
    }
}
