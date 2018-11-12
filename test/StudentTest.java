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
    public void insertTest() throws Exception {
        Student student = new Student("name", "lastName", "file", "email", "password","1999-05-11", "identificationType", "identification", "address");

        Result result = insertStudent(student);
        assertEquals(201, result.status());
        String id = contentAsString(result);
        assertThat(id, is(notNullValue()));

        result = insertStudent(student);
        assertEquals(409, result.status());
        result = getStudent(id);
        student.id = id;

        Student retrieved = readValue(result, new TypeReference<Student>(){});
        assertThat(student, samePropertyValuesAs(retrieved));

        student.email = "newEmail";
        insertStudent(student);
        result = getAllStudents();
        List<Student> students = readValue(result, new TypeReference<List<Student>>() {});
        assertEquals(2, students.size());
    }

    @Test
    public void deleteTest() throws Exception {
        Student student = new Student("name", "lastName", "file", "email", "password", "1999-05-11", "identificationType", "identification", "address");

        Result result = insertStudent(student);
        String id = contentAsString(result);

        result = deleteStudent(id);
        assertEquals(200, result.status());

        result = getAllStudents();
        List<Student> studentList = readValue(result, new TypeReference<List<Student>>() {});
        assertEquals(0, studentList.size());

        result = deleteStudent("fake-id");
        assertEquals(404, result.status());
    }

    @Test
    public void updateTest() throws Exception {
        Optional<String> optionalAddress = Optional.empty();
        Student student = new Student("name", "lastName", "file", "email", "password", "1999-05-11", "idType", "id", "address");
        Result result = insertStudent(student);
        String id = contentAsString(result);
        Student newStudent = new Student("name2", "lastName2", "file", "email", "password", "birthday", "idType", "id", "address");
        result = updateStudent(id, newStudent);
        assertEquals(200, result.status());
        result = getStudent(id);
        Student retrieved = readValue(result, new TypeReference<Student>(){});
        assertEquals(retrieved.name, "name2");
        assertEquals(retrieved.lastName, "lastName2");
    }


    private Result insertStudent(Student student) {
        Http.RequestBuilder requestBuilder = Helpers.fakeRequest().method(POST).uri("/student").bodyJson(Json.toJson(student));
        return route(application, requestBuilder);
    }

    private Result updateStudent(String id, Student student) {
        Http.RequestBuilder request = Helpers.fakeRequest().method(PUT).uri("/student/" + id).bodyJson(Json.toJson(student));
        return route(application, request);
    }


    private Result getStudent(String id) {
        Http.RequestBuilder requestBuilder = Helpers.fakeRequest().method(GET).uri("/student/" + id);
        return route(application, requestBuilder);
    }

    private Result getAllStudents() {
        Http.RequestBuilder requestBuilder = Helpers.fakeRequest().method(GET).uri("/student");
        return route(application, requestBuilder);
    }

    private Result deleteStudent(String id) {
        Http.RequestBuilder requestBuilder = Helpers.fakeRequest().method(DELETE).uri("/student/" + id);
        return route(application, requestBuilder);
    }

    private <T> T readValue(Result result, TypeReference<T> valueTypeRef) throws Exception{
        return Json.mapper().readValue(contentAsString(result), valueTypeRef);
    }

}
