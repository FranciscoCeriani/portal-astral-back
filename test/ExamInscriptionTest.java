import com.fasterxml.jackson.core.type.TypeReference;
import models.Exam;
import models.ExamInscription;
import models.Student;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.Application;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;

import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static play.test.Helpers.*;

public class ExamInscriptionTest {

    private Application application = fakeApplication(inMemoryDatabase());

    @Before
    public void startApp() {
        Helpers.start(application);
    }

    @After
    public void stopApp() {
        Helpers.stop(application);
    }

    @Test
    public void createExamInscription() throws Exception {

        startDB();

        Student student = new Student("name", "lastName", "file", "email", "password", "2017-09-18", "identificationType", "identification", "address");
        Result result;
        result = insertStudent(student);
        String studentId = contentAsString(result);

        result = getAllExams();
        List<Exam> exams = readValue(result, new TypeReference<List<Exam>>() {});
        String examId = exams.get(0).id;

        result = getAllExamInscriptions();
        List<ExamInscription> examInscriptions = readValue(result, new TypeReference<List<ExamInscription>>() {});
        int prevExamInscriptionsSize = examInscriptions.size();

        //Test if ExamInscription is successfully created.
        result = createExamInscription(studentId, examId);
        assertEquals(200, result.status());
        List<String> examInscriptionIds = readValue(result, new TypeReference<List<String>>() {});
        String examInscriptionId = examInscriptionIds.get(0);

        //Check if ExamInscription was added.
        result = getAllExamInscriptions();
        examInscriptions = readValue(result, new TypeReference<List<ExamInscription>>() {});
        int examInscriptionsSize = examInscriptions.size();
        assertEquals(prevExamInscriptionsSize + 1, examInscriptionsSize);
        prevExamInscriptionsSize = examInscriptionsSize;

        //Already created ExamInscription returning its id.
        result = createExamInscription(studentId, examId);
        assertEquals(200, result.status());
        examInscriptionIds = readValue(result, new TypeReference<List<String>>() {});
        String examInscriptionId2 = examInscriptionIds.get(0);
        assertEquals(examInscriptionId, examInscriptionId2); //Checking the ExamInscription already created returned its id and wasn't created again.

        //Check if existing ExamInscription wasn't added again.
        result = getAllExamInscriptions();
        examInscriptions = readValue(result, new TypeReference<List<ExamInscription>>() {});
        examInscriptionsSize = examInscriptions.size();
        assertEquals(prevExamInscriptionsSize, examInscriptionsSize);

        //Checking with non-existent student.
        result = createExamInscription("fakeId", examId);
        assertEquals(400, result.status());

        //Checking with non-existent exam.
        result = createExamInscription(studentId, "fakeId");
        assertEquals(400, result.status());
    }

    @Test
    public void deleteTest() throws Exception {

        startDB();

        Result result;
        result = getAllStudents();
        List<Student> students = readValue(result, new TypeReference<List<Student>>() {});
        String studentId = students.get(0).id;

        result = getAllExams();
        List<Exam> exams = readValue(result, new TypeReference<List<Exam>>() {});
        String examId = exams.get(0).id;

        result = getAllExamInscriptions();
        List<ExamInscription> examInscriptions = readValue(result, new TypeReference<List<ExamInscription>>() {});
        int prevExamInscriptionsSize = examInscriptions.size();

        //Test if ExamInscription is successfully deleted.
        result = deleteExamInscription(studentId, examId);
        assertEquals(200, result.status());

        //Check if ExamInscriptions decreased by one.
        result = getAllExamInscriptions();
        examInscriptions = readValue(result, new TypeReference<List<ExamInscription>>() {});
        int examInscriptionsSize = examInscriptions.size();
        assertEquals(prevExamInscriptionsSize - 1, examInscriptionsSize);

        //Test deleting recently deleted ExamInscription.
        result = deleteExamInscription(studentId, examId);
        assertEquals(404, result.status());

        //Test that ExamInscriptions didn't decrease.
        prevExamInscriptionsSize = examInscriptionsSize;
        result = getAllExamInscriptions();
        examInscriptions = readValue(result, new TypeReference<List<ExamInscription>>() {});
        examInscriptionsSize = examInscriptions.size();
        assertEquals(prevExamInscriptionsSize, examInscriptionsSize);

        //Test non existing student.
        result = deleteExamInscription("fakeId", examId);
        assertEquals(404, result.status());

        //Test non existing student.
        result = deleteExamInscription(studentId, "fakeId");
        assertEquals(404, result.status());

    }

    private Result startDB() {
        Http.RequestBuilder requestBuilder = Helpers.fakeRequest().method(GET).uri("/init");
        return route(application, requestBuilder);
    }

    private Result createExamInscription(String studentId, String examId) {
        Http.RequestBuilder requestBuilder = Helpers.fakeRequest().method(POST).uri("/exam/enroll/" + examId).bodyJson(Json.parse("{\"studentId\":\""+ studentId + "\"}"));
        return route(application, requestBuilder);
    }

    private Result insertStudent(Student student) {
        Http.RequestBuilder requestBuilder = Helpers.fakeRequest().method(POST).uri("/student").bodyJson(Json.toJson(student));
        return route(application, requestBuilder);
    }

    private Result getAllStudents() {
        Http.RequestBuilder requestBuilder = Helpers.fakeRequest().method(GET).uri("/student");
        return route(application, requestBuilder);
    }

    private Result getAllExamInscriptions() {
        Http.RequestBuilder requestBuilder = Helpers.fakeRequest().method(GET).uri("/examInscription");
        return route(application, requestBuilder);
    }

    private Result getAllExams() {
        Http.RequestBuilder requestBuilder = Helpers.fakeRequest().method(GET).uri("/exam");
        return route(application, requestBuilder);
    }

    private Result deleteExamInscription(String studentId, String examId) {
        Http.RequestBuilder requestBuilder = Helpers.fakeRequest().method(DELETE).uri("/exam/unenroll/" + examId).bodyJson(Json.parse("{\"studentId\":\""+ studentId + "\"}"));
        return route(application, requestBuilder);
    }

    private <T> T readValue(Result result, TypeReference<T> valueTypeRef) throws Exception{
        return Json.mapper().readValue(contentAsString(result), valueTypeRef);
    }

}
