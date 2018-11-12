import com.fasterxml.jackson.core.type.TypeReference;
import models.Admin;
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

public class AdminTest {

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

        Admin admin = new Admin("name", "lastName", "file", "email", "password");

        Result result = insertAdmin(admin);
        assertEquals(201, result.status());

        String id = contentAsString(result);
        assertThat(id, is(notNullValue()));
        result = insertAdmin(admin);
        assertEquals(409, result.status());

        result = getAdmin(id);
        admin.id = id;
        Admin retrieved = readValue(result, new TypeReference<Admin>(){});
        assertThat(admin, samePropertyValuesAs(retrieved));

        admin.email = "newMail";
        insertAdmin(admin);
        result = getAllAdmins();
        List<Admin> admins = readValue(result, new TypeReference<List<Admin>>(){});
        assertEquals(2, admins.size());
    }

    @Test
    public void deleteTest() throws Exception {

        Admin admin = new Admin("name", "lastName", "file", "email", "password");
        Result result = insertAdmin(admin);
        String id = contentAsString(result);
        result = deleteAdmin(id);
        assertEquals(403, result.status());

        Admin admin2 = new Admin("name", "lastName", "file", "email@as.com", "password");
        Result result2 = insertAdmin(admin2);
        String id2 = contentAsString(result2);

        result = deleteAdmin("fake-id");
        assertEquals(404, result.status());

        result2 = deleteAdmin(id2);
        assertEquals(200, result2.status());

        result = getAllAdmins();
        List<Admin> admins = readValue(result, new TypeReference<List<Admin>>(){});
        assertEquals(1, admins.size());
    }

    @Test
    public void updateTest() throws Exception {
        Admin admin = new Admin("name", "lastName", "file", "email", "password");
        Result result = insertAdmin(admin);
        String id = contentAsString(result);
        Admin newAdmin = new Admin("name2", "lastName2", "file", "email", "password");
        result = updateAdmin(id, newAdmin);
        assertEquals(201, result.status());
        result = getAdmin(id);
        Admin retrieved = readValue(result, new TypeReference<Admin>(){});
        assertEquals(retrieved.name, "name2");
        assertEquals(retrieved.lastName, "lastName2");
    }


    private Result insertAdmin(Admin admin) {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(POST)
                .uri("/administrator")
                .bodyJson(Json.toJson(admin));

        return route(application, request);
    }


    private Result deleteAdmin(String id) {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(DELETE)
                .uri("/administrator/" + id);

        return route(application, request);
    }

    private Result updateAdmin(String id, Admin admin) {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(PUT)
                .uri("/administrator/" + id)
                .bodyJson(Json.toJson(admin));

        return route(application, request);
    }

    private Result getAdmin(String id) {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(GET)
                .uri("/administrator/" + id);

        return route(application, request);
    }

    private Result getAllAdmins() {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(GET)
                .uri("/administrator");

        return route(application, request);
    }

    private <T> T readValue(Result result, TypeReference<T> valueTypeRef) throws Exception{
        return Json.mapper().readValue(contentAsString(result), valueTypeRef);
    }
}
