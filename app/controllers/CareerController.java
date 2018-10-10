package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.Career;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import repository.CareerModule;
import session.SessionManager;
import scala.util.Failure;

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
public class CareerController extends Controller {

    private final HttpExecutionContext executionContext;
    private final CareerModule careerModule;

    @Inject
    public CareerController(HttpExecutionContext executionContext, CareerModule careerModule) {
        this.executionContext = executionContext;
        this.careerModule = careerModule;
    }

    public CompletionStage<Result> saveCareer() {
        JsonNode json = request().body().asJson();
        Career career = Json.fromJson(json, Career.class);
        return careerModule.insert(career).thenApplyAsync(data -> {
            // This is the HTTP rendering thread context
            if(data.isSuccess()) {
                return status(201, data.get());
            }
            else {
                return status(409, ((Failure)data).exception().getMessage());
            }
        }, executionContext.current());
    }

    public CompletionStage<Result> deleteCareer(String id) {
        return careerModule.delete(id).thenApplyAsync(data -> {
            // This is the HTTP rendering thread context
            if (data.isPresent() && data.get()) {
                return status(200, id);
            } else {
                return status(404, "Career not found");
            }
        }, executionContext.current());
    }

    public CompletionStage<Result> updateCareer(String id) {
        JsonNode jsonNode = request().body().asJson();
        Career career = Json.fromJson(jsonNode, Career.class);
        return careerModule.update(id, career).thenApplyAsync(data -> {
            if (data.get()) {
                return status(200, "Career updated");
            } else {
                return status(400, "Career not found");
            }
        }, executionContext.current());
    }

    public CompletionStage<Result> getCareer(String id) {
        return careerModule.get(id).thenApplyAsync(data -> {
            // This is the HTTP rendering thread context
            if (data.isPresent()) {
                Career career = data.get();
                return ok(Json.toJson(career));
            } else {
                return status(404, "Career not found");
            }
        }, executionContext.current());
    }

    public CompletionStage<Result> getAllCareers() {
        return careerModule.getAll().thenApplyAsync(data -> {
            // This is the HTTP rendering thread context
            if(!data.isEmpty()){
                List<Career> careers = data;
                return ok(Json.toJson(careers));
            } else {
                List<Career> careers = new ArrayList<>();
                return ok(Json.toJson(careers));
            }
        }, executionContext.current());
    }
}