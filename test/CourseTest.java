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

import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.Assert.assertThat;
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
        assertEquals(409, result.status()); // Fails because subject is not registered

        Result result2 = insertSubject(subject);
        subject.id = contentAsString(result2);
        assertEquals(201, result2.status());

        result = insertCourse(course);
        assertEquals(409, result.status()); // Fails because course ends before it begins
        course.endDate = "2019-06-20";

        result = insertCourse(course);
        assertEquals(409, result.status()); // Fails because start date is before today
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

    private Result getCourse(String id) {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(GET)
                .uri("/course/" + id);

        return route(application, request);
    }

    private <T> T readValue(Result result, TypeReference<T> valueTypeRef) throws Exception{
        return Json.mapper().readValue(contentAsString(result), valueTypeRef);
    }
}
