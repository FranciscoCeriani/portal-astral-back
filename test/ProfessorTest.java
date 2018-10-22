import com.fasterxml.jackson.core.type.TypeReference;
import models.Professor;
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
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static play.test.Helpers.*;

public class ProfessorTest {

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

        Professor professor = new Professor("name", "lastName", "file", "email", "password");

        Result result = insertProfessor(professor);
        assertEquals(201, result.status());

        String id = contentAsString(result);
        assertThat(id, is(notNullValue()));

        result = insertProfessor(professor);
        assertEquals(409, result.status());

        result = getProfessor(id);
        professor.id = id;

        Professor retrieved = readValue(result, new TypeReference<Professor>(){});

        assertThat(professor, samePropertyValuesAs(retrieved));

        professor.email = "newEmail";
        insertProfessor(professor);

        result = getAllProfessors();

        List<Professor> professors = readValue(result, new TypeReference<List<Professor>>(){});
        assertEquals(2, professors.size());
    }

    @Test
    public void deleteTest() throws Exception {

        Professor professor = new Professor("name", "lastName", "file", "email", "password");
        Result result = insertProfessor(professor);

        String id = contentAsString(result);

        result = deleteProfessor(id);
        assertEquals(200, result.status());

        result = getAllProfessors();
        List<Professor> professors = readValue(result, new TypeReference<List<Professor>>(){});
        assertEquals(0, professors.size());

        result = deleteProfessor("fake-id");
        assertEquals(404, result.status());
    }

    @Test
    public void updateTest() throws Exception {
        Professor professor = new Professor("name", "lastName", "file", "email", "password");
        Result result = insertProfessor(professor);

        String id = contentAsString(result);

        professor.name = "newName";
        result = updateProfessor(professor, id);
        assertEquals(200, result.status());

        result = getProfessor(id);
        Professor retrieved = readValue(result, new TypeReference<Professor>(){});

        assertEquals("newName", retrieved.name);

        result = updateProfessor(professor, "fake-id");
        assertEquals(404, result.status());
    }

    private Result insertProfessor(Professor professor) {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(POST)
                .uri("/professor")
                .bodyJson(Json.toJson(professor));

        return route(application, request);
    }

    private Result updateProfessor(Professor professor, String id) {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(PUT)
                .uri("/professor/" + id)
                .bodyJson(Json.toJson(professor));

        return route(application, request);
    }

    private Result deleteProfessor(String id) {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(DELETE)
                .uri("/professor/" + id);

        return route(application, request);
    }

    private Result getProfessor(String id) {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(GET)
                .uri("/professor/" + id);

        return route(application, request);
    }

    private Result getAllProfessors() {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(GET)
                .uri("/professor");

        return route(application, request);
    }

    private <T> T readValue(Result result, TypeReference<T> valueTypeRef) throws Exception{
        return Json.mapper().readValue(contentAsString(result), valueTypeRef);
    }
}
