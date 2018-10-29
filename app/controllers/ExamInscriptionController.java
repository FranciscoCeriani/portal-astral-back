package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.ExamInscription;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import repository.ExamInscriptionModule;
import scala.util.Failure;

import javax.inject.Inject;
import java.util.Iterator;
import java.util.concurrent.CompletionStage;

public class ExamInscriptionController extends Controller {

    private final HttpExecutionContext executionContext;
    private final ExamInscriptionModule examInscriptionModule;

    @Inject
    public ExamInscriptionController(HttpExecutionContext executionContext, ExamInscriptionModule examInscriptionModule) {
        this.executionContext = executionContext;
        this.examInscriptionModule = examInscriptionModule;
    }

    public CompletionStage<Result> saveExamInscription(String id) {
        JsonNode json = request().body().asJson();
        Iterator<JsonNode> studentsIdIterator = json.elements();
        return examInscriptionModule.enrollStudentsToExam(studentsIdIterator, id).thenApplyAsync(data -> {
            if (data.isPresent()) {
                return status(200, Json.toJson(data.get()));
            } else {
                return status(400, "No exam inscriptions created");
            }
        }, executionContext.current());
    }

    public CompletionStage<Result> updateExamInscription(String id) {
        JsonNode jsonNode = request().body().asJson();
        ExamInscription examInscription = Json.fromJson(jsonNode, ExamInscription.class);
        return examInscriptionModule.update(id, examInscription).thenApplyAsync(data -> {
            if (data.isPresent()) {
                if (data.get()) {
                    return ok("ExamInscription updated");
                }
            }
            return status(404, "ExamInscription to be updated not found");
        }, executionContext.current());
    }

    public CompletionStage<Result> deleteExamInscription(String id) {

        return examInscriptionModule.delete(id).thenApplyAsync(data -> {
            // This is the HTTP rendering thread context
            if (data.isPresent()) {
                if (data.get()) {
                    return ok("ExamInscription deleted");
                } else {
                    return status(404, "Resource not found");
                }
            } else {
                return status(404, "Resource not found");
            }
        }, executionContext.current());
    }

    public CompletionStage<Result> getExamInscription(String id) {
        return examInscriptionModule.get(id).thenApplyAsync(data -> {
            // This is the HTTP rendering thread context
            if(data.isPresent()){
                ExamInscription examInscription = data.get();
                return ok(Json.toJson(examInscription));
            }else{
                return status(404, "Resource not found");
            }
        }, executionContext.current());
    }

    public CompletionStage<Result> getAllExamInscriptions() {
        return examInscriptionModule.getAll().thenApplyAsync(data -> {
            // This is the HTTP rendering thread context
            return ok(Json.toJson(data));
        }, executionContext.current());
    }

    //    Devuelve todos los ExamInscription pertenecientes a Student
    public CompletionStage<Result> getAllExamStudent(String id) {
        return examInscriptionModule.getAllExamStudent(id).thenApplyAsync(data -> {
            // This is the HTTP rendering thread context
            return ok(Json.toJson(data));
        }, executionContext.current());
    }

    //    Devuelve todos los ExamInscription pertenecientes a Exam
    public CompletionStage<Result> getAllExam(String id) {
        return examInscriptionModule.getAllExam(id).thenApplyAsync(data -> {
            // This is the HTTP rendering thread context
            return ok(Json.toJson(data));
        }, executionContext.current());
    }

//    Add result
    public CompletionStage<Result> addResult(String id) {
        JsonNode jsonNode = request().body().asJson();
        ExamInscription examInscription = Json.fromJson(jsonNode, ExamInscription.class);
        return examInscriptionModule.addResult(id, examInscription).thenApplyAsync(data -> {
            if (data.isPresent()) {
                if (data.get()) {
                    return ok("Result added");
                }
            }
            return status(404, "ExamInscription to be updated not found");
        }, executionContext.current());
    }
}
