import akka.stream.impl.JsonObjectParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.google.gson.JsonObject;
import io.ebeaninternal.server.transaction.JdbcTransaction;
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
import scala.util.parsing.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
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
    public void insertExam() throws Exception {
        Subject subject = new Subject("Algebra", 1, new ArrayList<>());
        Course course = new Course("2019-04-21", "2020-03-22", subject, new ArrayList<>());
        Exam exam = new Exam(course, "date");

        Result result3 = insertSubject(subject);
        subject.id = contentAsString(result3);
        assertEquals(201, result3.status());

        Result result2 = insertCourse(course);
        course.id = contentAsString(result2);
        assertEquals(201, result2.status());

        Result result = insertExam(course.id, exam.date);
        assertEquals(201, result.status());

        String examID = contentAsString(result);
        result = getExam(examID);
        exam.id = examID;
        Exam retrieved = readValue(result, new TypeReference<Exam>() {});
        assertThat(exam, samePropertyValuesAs(retrieved));

    }

    private Result insertExam(String courseID, String dateTime) throws IOException {

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

    private <T> T readValue(Result result, TypeReference<T> valueTypeRef) throws Exception {
        return Json.mapper().readValue(contentAsString(result), valueTypeRef);
    }
}
