import com.fasterxml.jackson.core.type.TypeReference;
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


import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.Assert.assertThat;
import static play.test.Helpers.*;

public class RequiredSubjectTest {

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

        Subject subject = new Subject("matematica", 3 , new ArrayList<>());
        Subject requiredSubject = new Subject("algebra", 3 , new ArrayList<>());

        Result result1 = insertSubject(subject);
        Result result2 = insertSubject(requiredSubject);
        String idSubject = contentAsString(result1);
        String idRequiredSubject = contentAsString(result2);

        Result result = insertRequiredSubject(idRequiredSubject,idSubject);
        assertEquals(200, result.status());

        String id = contentAsString(result);
        assertThat(id, is(notNullValue()));
        result = insertRequiredSubject(idRequiredSubject ,idSubject);
        assertEquals(400, result.status());

        result = getSubject(idRequiredSubject);
        requiredSubject.id = idRequiredSubject;
        Subject retrieved = readValue(result, new TypeReference<Subject>(){});
        assertThat(requiredSubject, samePropertyValuesAs(retrieved));

    }

    @Test
    public void deleteTest() throws Exception {

        Subject subject = new Subject("matematica", 3 , new ArrayList<>());
        Subject requiredSubject = new Subject("algebra", 3 , new ArrayList<>());

        Result result1 = insertSubject(subject);
        Result result2 = insertSubject(requiredSubject);
        String idSubject = contentAsString(result1);
        String idRequiredSubject = contentAsString(result2);
        insertRequiredSubject(idRequiredSubject,idSubject);

        Result result = deleteRequiredSubject(idRequiredSubject,idSubject);
        assertEquals(200, result.status());


        result = deleteRequiredSubject("fake-id", "fakeid");
        assertEquals(400, result.status());
    }

    private Result insertSubject(Subject subject) {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(POST)
                .uri("/subject")
                .bodyJson(Json.toJson(subject));

        return route(application, request);
    }

    private Result insertRequiredSubject(String requiredSubjectId, String subjectId) {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(POST)
                .uri("/correlative")
                .bodyJson(Json.newObject()
                        .put("subjectId" , subjectId)
                        .put("requiredSubjectId" , requiredSubjectId)
                );

        return route(application, request);
    }

    private Result getSubject(String id) {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(GET)
                .uri("/subject/" + id);

        return route(application, request);
    }

    private Result deleteRequiredSubject(String requiredSubjectId, String subjectId) {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(DELETE)
                .uri("/correlative")
                .bodyJson(Json.newObject()
                        .put("subjectId" , subjectId)
                        .put("requiredSubjectId" , requiredSubjectId)
                );

        return route(application, request);
    }
    private <T> T readValue(Result result, TypeReference<T> valueTypeRef) throws Exception{
        return Json.mapper().readValue(contentAsString(result), valueTypeRef);
    }
}