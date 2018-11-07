import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.Course;
import models.Exam;
import models.Subject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.Application;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.Assert.assertThat;
import static play.test.Helpers.*;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.route;

public class ExamTest {

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
    public void insertTest() throws Exception {
        Subject subject = new Subject("Algebra", 1, new ArrayList<>());
        Course course = new Course("2019-04-21", "2020-03-22", subject, new ArrayList<>());
        Exam exam = new Exam(course, "date");

        Result result3 = insertSubject(subject);
        subject.id = contentAsString(result3);
        assertEquals(201, result3.status());

        Result result2 = insertCourse(course);
        course.id = contentAsString(result2);
        assertEquals(201, result2.status());

        Result result = insertTest(course.id, exam.date);
        assertEquals(201, result.status());

        String examID = contentAsString(result);
        result = getExam(examID);
        exam.id = examID;
        Exam retrieved = readValue(result, new TypeReference<Exam>() {});
        assertThat(exam, samePropertyValuesAs(retrieved));

    }

    @Test
    public void deleteTest() throws Exception {
        Subject subject = new Subject("Algebra", 1, new ArrayList<>());
        Course course = new Course("2019-04-21", "2020-03-22", subject, new ArrayList<>());
        Exam exam = new Exam(course, "date");

        Result result3 = insertSubject(subject);
        subject.id = contentAsString(result3);
        assertEquals(201, result3.status());

        Result result2 = insertCourse(course);
        course.id = contentAsString(result2);
        assertEquals(201, result2.status());

        Result result = insertTest(course.id, exam.date);
        String id = contentAsString(result);

        result = deleteExam(id);
        assertEquals(200, result.status());

        result = getAllExams();
        List<Exam> studentList = readValue(result, new TypeReference<List<Exam>>() {});
        assertEquals(0, studentList.size());

        result = deleteExam("fake-id");
        assertEquals(404, result.status());
    }

    private Result insertTest(String courseID, String dateTime) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree("{\"courseID\":\"" + courseID + "\",\"dateTime\":\"" + dateTime + "\"}");

        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(POST)
                .uri("/exam")
                .bodyJson(jsonNode);
        return route(application, request);
    }

    private Result insertCourse(Course course) {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(POST)
                .uri("/course")
                .bodyJson(Json.toJson(course));

        return route(application, request);
    }

    private Result insertSubject(Subject subject) {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(POST)
                .uri("/subject")
                .bodyJson(Json.toJson(subject));

        return route(application, request);
    }

    private Result getExam(String id) {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(GET)
                .uri("/exam/" + id);

        return route(application, request);
    }

    private Result deleteExam(String id) {
        Http.RequestBuilder requestBuilder = Helpers.fakeRequest().method(DELETE).uri("/exam/" + id);
        return route(application, requestBuilder);
    }

    private <T> T readValue(Result result, TypeReference<T> valueTypeRef) throws Exception {
        return Json.mapper().readValue(contentAsString(result), valueTypeRef);
    }

    private Result getAllExams() {
        Http.RequestBuilder requestBuilder = Helpers.fakeRequest().method(GET).uri("/exam");
        return route(application, requestBuilder);
    }
}
