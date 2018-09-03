package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.Professor;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import repository.ProfessorModule;

import javax.inject.Inject;
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
}
