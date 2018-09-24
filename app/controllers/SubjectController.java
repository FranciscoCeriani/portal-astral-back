package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.Student;
import models.Subject;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import repository.StudentModule;
import repository.SubjectModule;
import session.SessionManager;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class SubjectController extends Controller {

    private final HttpExecutionContext executionContext;
    private final SubjectModule subjectModule;
    private final StudentModule studentModule;

    @Inject
    public SubjectController(HttpExecutionContext executionContext, SubjectModule subjectModule, StudentModule studentModule) {
        this.executionContext = executionContext;
        this.subjectModule = subjectModule;
        this.studentModule = studentModule;
    }


    public CompletionStage<Result> saveSubject() {
        JsonNode json = request().body().asJson();
        Subject subject = Json.fromJson(json, Subject.class);
        return subjectModule.insert(subject).thenApplyAsync(data -> {
            // This is the HTTP rendering thread context
            if (data.equals("Subject exists")){
                return status(403, "Subject already exists");
            } else if (data.equals("Required subject does not exist")){
                return status(400, "Required subject does not exist");
            }
            return status(201, data.get());
        }, executionContext.current());
    }

    public CompletionStage<Result> getSubject(String id) {

        return subjectModule.get(id).thenApplyAsync(data -> {
            // This is the HTTP rendering thread context
            if (data.isPresent()) {
                Subject subject = data.get();
                return ok(Json.toJson(subject));
            } else {
                return status(404, "Resource not found");
            }
        }, executionContext.current());
    }

    @With(SessionManager.class)
    public CompletionStage<Result> getAllSubjects() {
        return subjectModule.getAll().thenApplyAsync(data -> {
            // This is the HTTP rendering thread context
            if(!data.isEmpty()){
                List<Subject> subjects = data;
                return ok(Json.toJson(subjects));
            } else {
                List<Subject> subjects = new ArrayList<>();
                return ok(Json.toJson(subjects));
            }
        }, executionContext.current());
    }

    public CompletionStage<Result> saveStudentToSubject(String subjectId) {
        JsonNode json = request().body().asJson();
        Student student = Json.fromJson(json, Student.class);

        return subjectModule.addStudentToSubject(student, subjectId).thenApplyAsync(data -> {
            // This is the HTTP rendering thread context
            if (data.isPresent()) {
                return ok(Json.toJson(data));
            } else {
                return status(404, "Resource not found");
            }
        }, executionContext.current());
    }

    public CompletionStage<Result> saveRequiredSubject() {
        JsonNode jsonNode = request().body().asJson();
        Iterator<JsonNode> ids = jsonNode.elements();
        String subjectID = ids.next().textValue();
        String requiredSubjectID = ids.next().textValue();
        return subjectModule.addRequiredSubject(subjectID, requiredSubjectID).thenApplyAsync(data -> {
            if (data.isPresent()) {
                return status(200, "Subject added");
            } else {
                return status(400, "Resource not found");
            }
        }, executionContext.current());
    }

    public CompletionStage<Result> deleteRequiredSubject(){
        JsonNode jsonNode = request().body().asJson();
        Iterator<JsonNode> ids = jsonNode.elements();
        String subjectID = ids.next().textValue();
        String requiredSubjectID = ids.next().textValue();

        return subjectModule.deleteRequiredSubject(subjectID, requiredSubjectID).thenApplyAsync(data -> {
            if (data.get()) {
                return status(200, "Subject deleted");
            } else {
                return status(400, "Resource not found");
            }
        }, executionContext.current());
    }

    public CompletionStage<Result> updateSubject(String id) {
        JsonNode jsonNode = request().body().asJson();
        Subject subject = Json.fromJson(jsonNode, Subject.class);
        return subjectModule.update(id, subject).thenApplyAsync(data -> {
            if (data.get()) {
                return status(200, "Professor update");
            } else {
                return status(400, "Resources not found");
            }
        }, executionContext.current());
    }

    public CompletionStage<Result> deleteSubject(String id) {
        return subjectModule.delete(id).thenApplyAsync(data -> {
            // This is the HTTP rendering thread context
            if (data.isPresent() && data.get()) {
                return status(200, id);
            } else {
                return status(404, "Subject not found");
            }
        }, executionContext.current());
    }
}