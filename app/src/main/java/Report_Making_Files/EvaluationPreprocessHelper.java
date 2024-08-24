package Report_Making_Files;

import Display_Course_Students_Evaluation_Activity.CourseStudentEvaluationListModel;
import com.nextgen.hasnatfyp.StudentEvaluationDetailsModel;

import java.util.*;

public class EvaluationPreprocessHelper {

    public static List<CourseStudentEvaluationListModel> preprocessData(List<CourseStudentEvaluationListModel> evaluationRecords) {
        List<CourseStudentEvaluationListModel> preprocessedRecords = new ArrayList<>();

        // Get the order of evaluations from the student with the maximum evaluations
        List<String> evaluationOrder = getMaxEvaluationsOrder(evaluationRecords);

        // Get all unique evaluation names
        Set<String> allEvaluationNames = new HashSet<>(evaluationOrder);

        for (CourseStudentEvaluationListModel record : evaluationRecords) {
            CourseStudentEvaluationListModel preprocessedRecord = new CourseStudentEvaluationListModel();
            preprocessedRecord.setStudentName(record.getStudentName());
            preprocessedRecord.setStudentRollNo(record.getStudentRollNo());

            // Deep copy of the original evaluation list
            List<StudentEvaluationDetailsModel> preprocessedEvalList = new ArrayList<>(record.getStudentEvalList());

            // Add missing evaluations
            addMissingEvaluations(preprocessedEvalList, allEvaluationNames, evaluationRecords);

            // Sort evaluations based on the predefined order
            sortEvaluations(preprocessedEvalList, evaluationOrder);

            preprocessedRecord.setStudentEvalList(preprocessedEvalList);
            preprocessedRecords.add(preprocessedRecord);
        }

        return preprocessedRecords;
    }

    private static List<String> getMaxEvaluationsOrder(List<CourseStudentEvaluationListModel> evaluationRecords) {
        int maxEvaluations = 0;
        List<String> evaluationOrder = new ArrayList<>();

        for (CourseStudentEvaluationListModel record : evaluationRecords) {
            if (record.getStudentEvalList().size() > maxEvaluations) {
                maxEvaluations = record.getStudentEvalList().size();
                evaluationOrder.clear();
                for (StudentEvaluationDetailsModel evaluation : record.getStudentEvalList()) {
                    evaluationOrder.add(evaluation.getEvaluationName());
                }
            }
        }
        return evaluationOrder;
    }

    private static void addMissingEvaluations(List<StudentEvaluationDetailsModel> evalList, Set<String> allEvaluationNames, List<CourseStudentEvaluationListModel> originalRecords) {
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

    private static String findTotalMarksForEvaluation(String evalName, List<CourseStudentEvaluationListModel> originalRecords) {
        for (CourseStudentEvaluationListModel record : originalRecords) {
            for (StudentEvaluationDetailsModel evaluation : record.getStudentEvalList()) {
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
