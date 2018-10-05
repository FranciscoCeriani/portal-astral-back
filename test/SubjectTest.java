import com.fasterxml.jackson.core.type.TypeReference;
import models.Student;
import models.Subject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.Application;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.Assert.assertThat;
import static play.test.Helpers.*;
import static play.test.Helpers.GET;
import static play.test.Helpers.route;

public class SubjectTest {

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

        Subject subject = new Subject("name", 0, new ArrayList<>(), new ArrayList<>());

        Result result = insertSubject(subject);
        assertEquals(201, result.status());

        String id = contentAsString(result);
        assertThat(id, is(notNullValue()));

        result = insertSubject(subject);
        assertEquals(409, result.status());

        result = getSubject(id);
        subject.id = id;

        Subject retrieved = readValue(result, new TypeReference<Subject>(){});

        assertThat(subject, samePropertyValuesAs(retrieved));

        subject.subjectName = "newName";
        insertSubject(subject);

        result = getAllSubjects();

        List<Subject> subjects = readValue(result, new TypeReference<List<Subject>>(){});
        assertEquals(2, subjects.size());
    }

    @Test
    public void deleteTest() throws Exception {

        Subject subject = new Subject("name", 0, new ArrayList<>(), new ArrayList<>());
        Result result = insertSubject(subject);

        String id = contentAsString(result);

        result = deleteSubject(id);
        assertEquals(200, result.status());

        result = getAllSubjects();
        List<Subject> subjects = readValue(result, new TypeReference<List<Subject>>(){});
        assertEquals(0, subjects.size());

        result = deleteSubject("fake-id");
        assertEquals(404, result.status());
    }

    @Test
    public void updateTest() throws Exception {
        Subject subject = new Subject("name", 0, new ArrayList<>(), new ArrayList<>());
        Result result = insertSubject(subject);

        String id = contentAsString(result);

        subject.subjectName = "newName";
        result = updateSubject(subject, id);
        assertEquals(200, result.status());

        result = getSubject(id);
        Subject retrieved = readValue(result, new TypeReference<Subject>(){});

        assertEquals("newName", retrieved.subjectName);

        result = updateSubject(subject, "fake-id");
        assertEquals(400, result.status());
    }

    @Test
    public void insertStudentTest() throws Exception {
        Subject subject = new Subject("name", 0, new ArrayList<>(), new ArrayList<>());
        Result result = insertSubject(subject);

        String subjectId = contentAsString(result);

        Student student = new Student();

        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(POST)
                .uri("/student")
                .bodyJson(Json.toJson(student));

        result = route(application, request);
        assertEquals(201, result.status());

        student.id = contentAsString(result);

        request = Helpers.fakeRequest()
                .method(POST)
                .uri("/subject/" + subjectId)
                .bodyJson(Json.toJson(student));

        result = route(application, request);
        assertEquals(200, result.status());

        Subject retrieved = readValue(result, new TypeReference<Subject>(){});

        assertThat(student, samePropertyValuesAs(retrieved.students.get(0)));
    }

    private Result insertSubject(Subject subject) {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(POST)
                .uri("/subject")
                .bodyJson(Json.toJson(subject));

        return route(application, request);
    }

    private Result updateSubject(Subject subject, String id) {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(PUT)
                .uri("/subject/" + id)
                .bodyJson(Json.toJson(subject));

        return route(application, request);
    }

    private Result deleteSubject(String id) {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(DELETE)
                .uri("/subject/" + id);

        return route(application, request);
    }

    private Result getSubject(String id) {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(GET)
                .uri("/subject/" + id);

        return route(application, request);
    }

    private Result getAllSubjects() {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(GET)
                .uri("/subject");

        return route(application, request);
    }

    private <T> T readValue(Result result, TypeReference<T> valueTypeRef) throws Exception{
        return Json.mapper().readValue(contentAsString(result), valueTypeRef);
    }
}
