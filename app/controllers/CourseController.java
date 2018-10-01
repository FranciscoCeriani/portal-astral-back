package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.Course;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import repository.CourseModule;
import scala.util.Failure;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class CourseController extends Controller {

    private final HttpExecutionContext executionContext;
    private final CourseModule courseModule;

    @Inject
    public CourseController(HttpExecutionContext executionContext, CourseModule courseModule) {
        this.executionContext = executionContext;
        this.courseModule = courseModule;
    }

    public CompletionStage<Result> saveCourse() {
        JsonNode json = request().body().asJson();
        Course realCourse = Json.fromJson(json, Course.class);
        return courseModule.insert(realCourse).thenApplyAsync(data -> {
            if (data.isSuccess()) {
                return status(201, data.get());
            } else {
                return status(409, ((Failure) data).exception().getMessage());
            }
        }, executionContext.current());
    }

    public CompletionStage<Result> updateCourse(String id) {
        JsonNode jsonNode = request().body().asJson();
        Course course = Json.fromJson(jsonNode, Course.class);
        return courseModule.update(id, course).thenApplyAsync(data -> {
            if (data.isPresent()) {
                if (data.get()) {
                    return ok("Course updated");
                }
            }
            return status(404, "Course to be updated not found");
        }, executionContext.current());
    }

    public CompletionStage<Result> deleteCourse(String id) {

        return courseModule.delete(id).thenApplyAsync(data -> {
            // This is the HTTP rendering thread context
            if (data.isPresent()) {
                if (data.get()) {
                    return ok("Course deleted");
                } else {
                    return status(404, "Resource not found");
                }
            } else {
                return status(404, "Resource not found");
            }
        }, executionContext.current());
    }

    public CompletionStage<Result> getCourse(String id) {
        return courseModule.get(id).thenApplyAsync(data -> {
            // This is the HTTP rendering thread context
            if(data.isPresent()){
                Course course = data.get();
                return ok(Json.toJson(course));
            }else{
                return status(404, "Resource not found");
            }
        }, executionContext.current());
    }

    public CompletionStage<Result> getAllCourses() {
        return courseModule.getAll().thenApplyAsync(data -> {
            // This is the HTTP rendering thread context
            return ok(Json.toJson(data));
        }, executionContext.current());
    }
}
