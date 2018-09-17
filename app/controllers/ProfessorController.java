package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.Professor;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import repository.ProfessorModule;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class ProfessorController extends Controller {
    private final HttpExecutionContext executionContext;
    private final ProfessorModule professorModule;

    @Inject
    public ProfessorController(HttpExecutionContext executionContext, ProfessorModule professorModule) {
        this.executionContext = executionContext;
        this.professorModule = professorModule;
    }

    public CompletionStage<Result> saveProfessor() {
        JsonNode jsonNode = request().body().asJson();
        Professor professor = Json.fromJson(jsonNode, Professor.class);
        return professorModule.insert(professor).thenApplyAsync(data -> {
            return status(201, data);
        }, executionContext.current());
    }

    public CompletionStage<Result> updateProfessor(String id) {
        JsonNode jsonNode = request().body().asJson();
        Professor professor = Json.fromJson(jsonNode, Professor.class);
        return professorModule.update(id, professor).thenApplyAsync(data -> {
            if (data.get()) {
                return ok("Professor update");
            } else {
                return status(404, "Resources not found");
            }
        }, executionContext.current());
    }

    public CompletionStage<Result> getAllProfessors() {
        return professorModule.getAll().thenApplyAsync(data -> {
            // This is the HTTP rendering thread context
                return ok(Json.toJson(data));
        }, executionContext.current());
    }

    public CompletionStage<Result> getProfessor(String id) {
        return professorModule.get(id).thenApplyAsync(data -> {
            // This is the HTTP rendering thread context
            if(data.isPresent()){
                Professor professor = data.get();
                return ok(Json.toJson(professor));
            }else{
                return status(404, "Resource not found");
            }
        }, executionContext.current());
    }

    public CompletionStage<Result> deleteProfessor(String id) {
        return professorModule.delete(id).thenApplyAsync(data -> {
            // This is the HTTP rendering thread context
            if (data.isPresent() && data.get()) {
                return ok();
            } else {
                return status(404, "Resource not found");
            }
        }, executionContext.current());
    }
}
