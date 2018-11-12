package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.CourseProfessor;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import repository.CourseProfessorModule;

import javax.inject.Inject;
import java.util.Iterator;
import java.util.concurrent.CompletionStage;

public class CourseProfessorController extends Controller {

    private final HttpExecutionContext executionContext;
    private final CourseProfessorModule courseProfessorModule;

    @Inject
    public CourseProfessorController(HttpExecutionContext executionContext, CourseProfessorModule courseProfessorModule) {
        this.executionContext = executionContext;
        this.courseProfessorModule = courseProfessorModule;
    }

    public CompletionStage<Result> saveCourseProfessor(String id) {
        JsonNode json = request().body().asJson();
        Iterator<JsonNode> professorsIdIterator = json.elements();
        return courseProfessorModule.enrollProfessorsToCourse(professorsIdIterator, id).thenApplyAsync(data -> {
            if (data.isPresent() && !data.get().isEmpty()) {
                return status(200, Json.toJson(data.get()));
            } else {
                return status(400, "No course inscriptions created");
            }
        }, executionContext.current());
    }

    public CompletionStage<Result> updateCourseProfessor(String id) {
        JsonNode jsonNode = request().body().asJson();
        String professorId = jsonNode.get("professorId").asText();
        return courseProfessorModule.update(id, professorId).thenApplyAsync(data -> {
            if (data.isPresent()) {
                if (data.get()) {
                    return ok("CourseProfessor updated");
                }
            }
            return status(404, "CourseProfessor to be updated not found");
        }, executionContext.current());
    }

    public CompletionStage<Result> deleteCourseProfessor(String courseId) {

        JsonNode jsonNode = request().body().asJson();
        String professorId = jsonNode.get("professorId").asText();
        return courseProfessorModule.unenrollProfessorFromCourse(professorId, courseId).thenApplyAsync(data -> {
            if (data.isPresent()) {
                if (data.get()) {
                    return ok("Professor unenrolled from course");
                }
            }
            return status(404, "CourseProfessor not found");
        }, executionContext.current());
    }

    public CompletionStage<Result> getCourseProfessor(String id) {
        return courseProfessorModule.get(id).thenApplyAsync(data -> {
            // This is the HTTP rendering thread context
            if(data.isPresent()){
                CourseProfessor courseProfessor = data.get();
                return ok(Json.toJson(courseProfessor));
            }else{
                return status(404, "Resource not found");
            }
        }, executionContext.current());
    }

    public CompletionStage<Result> getAllCourseProfessors() {
        return courseProfessorModule.getAll().thenApplyAsync(data -> {
            // This is the HTTP rendering thread context
            return ok(Json.toJson(data));
        }, executionContext.current());
    }

    //    Devuelve todos los CourseProfessor pertenecientes a Professor
    public CompletionStage<Result> getAllProfessorCourses(String id) {
        return courseProfessorModule.getAllCourseProfessor(id).thenApplyAsync(data -> {
            // This is the HTTP rendering thread context
            return ok(Json.toJson(data));
        }, executionContext.current());
    }

    //    Devuelve todos los CourseProfessor pertenecientes a Course
    public CompletionStage<Result> getAllCourseProfessors(String id) {
        return courseProfessorModule.getAllCourse(id).thenApplyAsync(data -> {
            // This is the HTTP rendering thread context
            return ok(Json.toJson(data));
        }, executionContext.current());
    }
}
