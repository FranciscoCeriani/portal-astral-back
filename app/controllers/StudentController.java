package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.Student;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import repository.StudentModule;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class StudentController extends Controller {

    private final HttpExecutionContext executionContext;
    private final StudentModule studentModule;

    @Inject
    public StudentController (HttpExecutionContext executionContext, StudentModule studentModule) {
        this.executionContext = executionContext;
        this.studentModule = studentModule;
    }


    public CompletionStage<Result> saveStudent() {
        JsonNode json = request().body().asJson();
        Student realStudent = Json.fromJson(json, Student.class);
        return studentModule.insert(realStudent).thenApplyAsync(data -> {
            // This is the HTTP rendering thread context
            return status(201, data);
        }, executionContext.current());
    }

    public CompletionStage<Result> updateStudent(String id) {
        JsonNode jsonNode = request().body().asJson();
        Student student = Json.fromJson(jsonNode, Student.class);
        return studentModule.update(id, student).thenApplyAsync(data -> {
            if (data.get()) {
                return ok("Student updated");
            } else {
                return status(404, "Student to be updated not found");
            }
        }, executionContext.current());
    }

    public CompletionStage<Result> getStudent(String id) {

        return studentModule.get(id).thenApplyAsync(data -> {
            // This is the HTTP rendering thread context
            if(data.isPresent()){
                Student student = data.get();
                return ok(Json.toJson(student));
            }else{
                return status(404, "Resource not found");
            }
        }, executionContext.current());
    }

    public CompletionStage<Result> getAllStudents() {
        return studentModule.getAll().thenApplyAsync(data -> {
            // This is the HTTP rendering thread context
            if(data.isPresent()){
                List<Student> studentList = data.get();
                return ok(Json.toJson(studentList));
            }else{
                return status(404, "Resource not found");
            }
        }, executionContext.current());
    }

    public CompletionStage<Result> deleteStudent(String id){

         return studentModule.delete(id).thenApplyAsync(data -> {
            // This is the HTTP rendering thread context
            if(data.isPresent()){
                if (data.get()) {
                    return ok("Student deleted");
                } else {
                    return status(404, "Resource not found");
                }
            }else{
                return status(404, "Resource not found");
            }
        }, executionContext.current());
    }

}