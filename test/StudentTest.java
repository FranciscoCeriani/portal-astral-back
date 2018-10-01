import com.fasterxml.jackson.core.type.TypeReference;
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
import java.util.Optional;

import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static play.test.Helpers.*;

public class StudentTest {

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
    public void updateTest() throws Exception {
        Optional<String> opt = Optional.of("asdada");
        Student student = new Student("name", "lastName", "file", "email", "password", "idType", "id", opt);
        Result result = insertStudent(student);
        String id = contentAsString(result);
        Student newStudent = new Student("name2", "lastName2", "file", "email", "password");
        result = updateStudent(id, newStudent);
        assertEquals(201, result.status());
        result = getStudent(id);
        Student retrieved = readValue(result, new TypeReference<Student>(){});
        assertEquals(retrieved.name, "name2");
        assertEquals(retrieved.lastName, "lastName2");
    }

    private Result insertStudent(Student student) {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(POST)
                .uri("/student")
                .bodyJson(Json.toJson(student));

        return route(application, request);
    }


    private Result deleteStudent(String id) {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(DELETE)
                .uri("/student/" + id);

        return route(application, request);
    }

    private Result updateStudent(String id) {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(PUT)
                .uri("/administrator/" + id)
                .bodyJson(Json.toJson(student));

        return route(application, request);
    }


    private Result getStudent(String id) {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(GET)
                .uri("/student/" + id);

        return route(application, request);
    }

    private Result getAllStudents() {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(GET)
                .uri("/student");

        return route(application, request);
    }

    private <T> T readValue(Result result, TypeReference<T> valueTypeRef) throws Exception{
        return Json.mapper().readValue(contentAsString(result), valueTypeRef);
    }
}
