import com.fasterxml.jackson.core.type.TypeReference;
import models.*;
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
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static play.test.Helpers.*;

public class CourseTest {

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
    public void insertCourse() throws Exception {
        Subject subject = new Subject("Algebra", 1, new ArrayList<>());
        Course course = new Course("2018-04-21", "2018-03-22", subject, new ArrayList<>());

        Result result = insertCourse(course);
        assertEquals(400, result.status()); // Fails because subject is not registered

        Result result2 = insertSubject(subject);
        subject.id = contentAsString(result2);
        assertEquals(201, result2.status());

        result = insertCourse(course);
        assertEquals(400, result.status()); // Fails because course ends before it begins
        course.endDate = "2019-06-20";

        result = insertCourse(course);
        assertEquals(400, result.status()); // Fails because start date is before today
        course.startDate = "2019-01-20";

        result = insertCourse(course);
        assertEquals(201, result.status()); // Succeeds as all the information is now correct.

        // Finally we check if the course was actually saved correctly
        String courseId = contentAsString(result);
        result = getCourse(courseId);
        course.id = courseId;
        Course retrieved = readValue(result, new TypeReference<Course>(){});
        assertThat(course, samePropertyValuesAs(retrieved));
    }

    @Test
    public void deleteTest() throws Exception {
        Subject subject = new Subject("Algebra", 1, new ArrayList<>());
        Course course = new Course("2018-04-21", "2018-03-22", subject, new ArrayList<>());

        insertSubject(subject);
        Result result = insertCourse(course);
        String id = contentAsString(result);

        result = deleteCourse(id);
        assertEquals(200, result.status());

        result = getAllCourses();
        List<Course> courses = readValue(result, new TypeReference<List<Course>>(){});
        assertEquals(0, courses.size());

        result = deleteCourse("fake-id");
        assertEquals(404, result.status());


    }

    @Test
    public void updateTest() throws Exception {
        Subject subject = new Subject("Algebra", 1, new ArrayList<>());
        Course course = new Course("2018-04-21", "2018-03-22", subject, new ArrayList<>());

        insertSubject(subject);
        Result result = insertCourse(course);

        String id = contentAsString(result);
        course.startDate = "2019-04-21";
        result = updateCourse(course , id);
        assertEquals(200, result.status());

        result = getCourse(id);
        Course retrieved = readValue(result, new TypeReference<Course>(){});

        assertEquals("2019-04-21", retrieved.startDate);

        result = updateCourse(course, "fake-id");
        assertEquals(404, result.status());
    }

    @Test
    public void enrollStudentTest() throws Exception {
        Subject subject = new Subject("Physics", 1, new ArrayList<>());
        Course course = new Course("2019-01-20", "2019-06-20", subject, new ArrayList<>());
        Result result = insertSubject(subject);
        assertEquals(201, result.status());
        subject.id = contentAsString(result);
        result = insertCourse(course);
        course.id = contentAsString(result);

        Student student = new Student();
        student.id = "fake-id";
        result = enrollStudent(student, course.id);
        assertEquals(400, result.status()); // Student is not in the database.

        result = insertStudent(student);
        student.id = contentAsString(result);

        result = enrollStudent(student, course.id);
        assertEquals(200, result.status());

        result = getCourses(student.id);
        List<Course> courses = readValue(result, new TypeReference<List<Course>>(){});
        assertTrue(courses.contains(course));
    }

    @Test
    public void removeStudentTest() throws Exception {
        Subject subject = new Subject("Programming", 1, new ArrayList<>());
        Course course = new Course("2019-01-20", "2019-06-20", subject, new ArrayList<>());

        Result result = insertSubject(subject);
        subject.id = contentAsString(result);

        result = insertCourse(course);
        course.id = contentAsString(result);

        Student student = new Student();
        student.id = "fake-id";
        result = enrollStudent(student, course.id);
        assertEquals(400, result.status()); // Student is not in the database.

        result = insertStudent(student);
        student.id = contentAsString(result);

        enrollStudent(student, course.id);

        result = removeStudent(student, course.id);
        assertEquals(200, result.status());

        result = getCourses(student.id);
        List<Course> courses = readValue(result, new TypeReference<List<Course>>(){});
        assertTrue(!courses.contains(course));
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

    private Result insertStudent(Student student) {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(POST)
                .uri("/student")
                .bodyJson(Json.toJson(student));

        return route(application, request);
    }

    private Result getCourse(String id) {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(GET)
                .uri("/course/" + id);

        return route(application, request);
    }

    private Result enrollStudent(Student student, String id) {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(POST)
                .uri("/course/"+ id + "/enroll")
                .bodyJson(Json.parse("{\"id\":\""+ student.id + "\"}"));

        return route(application, request);
    }

    private Result removeStudent(Student student, String id) {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(DELETE)
                .uri("/course/"+ id + "/remove")
                .bodyJson(Json.parse("{\"id\":\""+ student.id + "\"}"));

        return route(application, request);
    }

    private Result getCourses(String id) {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(GET)
                .uri("/student/"+ id +"/courses");

        return route(application, request);
    }

    private Result deleteCourse(String id) {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(DELETE)
                .uri("/course/" + id);

        return route(application, request);
    }

    private Result updateCourse(Course course, String id) {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(PUT)
                .uri("/course/" + id)
                .bodyJson(Json.toJson(course));

        return route(application, request);
    }

    private Result getAllCourses() {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(GET)
                .uri("/course");

        return route(application, request);
    }

    private <T> T readValue(Result result, TypeReference<T> valueTypeRef) throws Exception{
        return Json.mapper().readValue(contentAsString(result), valueTypeRef);
    }
}