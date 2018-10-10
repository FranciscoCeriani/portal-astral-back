package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.Exam;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import repository.ExamModule;
import scala.util.Failure;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class ExamController extends Controller {
    private final HttpExecutionContext executionContext;
    private final ExamModule examModule;

    @Inject
    public ExamController(HttpExecutionContext executionContext, ExamModule examModule) {
        this.executionContext = executionContext;
        this.examModule = examModule;
    }

    public CompletionStage<Result> saveExam() {
        JsonNode jsonNode = request().body().asJson();
        Exam exam = Json.fromJson(jsonNode, Exam.class);
        return examModule.insert(exam).thenApplyAsync(data -> {
            if(data.isSuccess()) {
                return status(201, data.get());
            }
            else {
                return status(409, ((Failure)data).exception().getMessage());
            }
        }, executionContext.current());
    }

    public CompletionStage<Result> deleteExam(String id){
        return examModule.delete(id).thenApplyAsync(data -> {
            if(data.isPresent()){
                if(data.get()){
                    return status(200, "Deleted Correctly");
                }
            }
            return status(404, "Exam not found");
        }, executionContext.current());
    }

    public CompletionStage<Result> updateExam(String id) {
        JsonNode jsonNode = request().body().asJson();
        Exam exam = Json.fromJson(jsonNode, Exam.class);
        return examModule.update(id, exam).thenApplyAsync(data -> {
            if (data.isPresent()){
                if(data.get()){
                    return status(201, "Updated successfully");
                }
            }
            return status(404, "Exam not found");
        }, executionContext.current());
    }
    public CompletionStage<Result> getExam(String id) {
        return examModule.get(id).thenApplyAsync(data -> {
            if (data.isPresent()) {
                Exam exam = data.get();
                return ok(Json.toJson(exam));
            } else {
                return status(404, "Exam not found");
            }
        }, executionContext.current());
    }

    public CompletionStage<Result> getAllExams() {
        return examModule.getAll().thenApplyAsync(data -> {
            return ok(Json.toJson(data));
        }, executionContext.current());
    }


}
