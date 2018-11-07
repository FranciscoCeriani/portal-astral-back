import com.fasterxml.jackson.core.type.TypeReference;
import junit.framework.Assert;
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
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static play.test.Helpers.*;

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
        ArrayList<String> requiredSubjects = new ArrayList<>();
        ArrayList<Student> students = new ArrayList<>();
        Subject subject = new Subject("lab2", 3, requiredSubjects);

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

        subject.subjectName = "lab3";
        subject.careerYear = 4;
        result = insertSubject(subject);
        assertEquals(201, result.status());

        subject.requiredSubjects.add("fake-subject");
        result = insertSubject(subject);
        assertEquals(409, result.status());

        requiredSubjects.remove(0);
        requiredSubjects.add(id);
        Subject subject2 = new Subject("analisis", 1, requiredSubjects);
        result = insertSubject(subject2);
        assertEquals(201, result.status());

        result = getAllSubjects();
        List<Subject> subjects = readValue(result, new TypeReference<List<Subject>>(){});
        assertEquals(3, subjects.size());
        assertEquals(subjects.get(0).subjectName, "lab2");
        assertEquals(subjects.get(1).subjectName, "lab3");
        assertEquals(subjects.get(2).subjectName, "analisis");
    }

    @Test
    public void deleteTest() throws Exception {
        ArrayList<String> requiredSubjects = new ArrayList<>();
        ArrayList<Student> students = new ArrayList<>();
        Subject subject = new Subject("lab2", 3, requiredSubjects);

        Result result = insertSubject(subject);
        String id = contentAsString(result);
        subject.subjectName = "lab3";
        subject.careerYear = 4;
        result = insertSubject(subject);
        String id2 = contentAsString(result);

        result = getAllSubjects();
        List<Subject> subjects = readValue(result, new TypeReference<List<Subject>>(){});
        assertEquals(2, subjects.size());

        result = deleteSubject(id);
        assertEquals(200, result.status());

        result = deleteSubject(id);
        assertEquals(404, result.status());

        result = getAllSubjects();
        subjects = readValue(result, new TypeReference<List<Subject>>(){});
        assertEquals(1, subjects.size());

        result = deleteSubject(id2);
        assertEquals(200, result.status());

        result = getAllSubjects();
        subjects = readValue(result, new TypeReference<List<Subject>>(){});
        assertEquals(0, subjects.size());
    }

    @Test
    public void getAllSubjectsTest() throws Exception{

        ArrayList<String> requiredSubjects = new ArrayList<>();
        ArrayList<Student> students = new ArrayList<>();
        Subject subject = new Subject("lab2", 3, requiredSubjects);
        Subject subject2 = new Subject("lab3", 4, requiredSubjects);

        Result result = getAllSubjects();
        assertEquals(200, result.status());
        List<Subject> subjects = readValue(result, new TypeReference<List<Subject>>(){});
        assertTrue(subjects.isEmpty());

        result = insertSubject(subject);
        String subject1Id = contentAsString(result);

        result = getAllSubjects();
        assertEquals(200, result.status());
        subjects = readValue(result, new TypeReference<List<Subject>>(){});
        assertEquals(subjects.size(), 1);

        result = insertSubject(subject2);
        String subject2Id = contentAsString(result);

        requiredSubjects.add(subject1Id);
        Subject subject3 = new Subject("lab4", 5, requiredSubjects);

        result = insertSubject(subject3);
        String subject3Id = contentAsString(result);

        result = getAllSubjects();
        assertEquals(200, result.status());
        subjects = readValue(result, new TypeReference<List<Subject>>(){});
        assertEquals(subjects.size(), 3);

        assertEquals(subjects.get(0).id, subject1Id);
        assertEquals(subjects.get(1).id, subject2Id);
        assertEquals(subjects.get(2).id, subject3Id);
        assertEquals(subjects.get(2).requiredSubjects.get(0), subject1Id);
        assertEquals(subjects.get(2).requiredSubjects.size(), 1);
    }


    private Result insertSubject(Subject subject) {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(POST)
                .uri("/subject")
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
