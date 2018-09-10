package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.Admin;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import repository.AdminModule;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class AdminController extends Controller {
    private final HttpExecutionContext executionContext;
    private final AdminModule adminModule;

    @Inject
    public AdminController(HttpExecutionContext executionContext, AdminModule adminModule) {
        this.executionContext = executionContext;
        this.adminModule = adminModule;
    }

    public CompletionStage<Result> saveAdmin() {
        JsonNode jsonNode = request().body().asJson();
        Admin admin = Json.fromJson(jsonNode, Admin.class);
        return adminModule.insert(admin).thenApplyAsync(data -> {
            return status(201, data);
        }, executionContext.current());
    }

    public CompletionStage<Result> getAllAdmins() {
        final CompletableFuture<Result> result = new CompletableFuture<>();
        result.complete(status(501, "Method not implemented"));
        return result;
    }

    public CompletionStage<Result> getAdmin(String id) {
        final CompletableFuture<Result> result = new CompletableFuture<>();
        result.complete(status(501, "Method not implemented"));
        return result;
    }

    public CompletionStage<Result> deleteAdmin(String id){
        return adminModule.delete(id).thenApplyAsync(data -> {
            if(data.isPresent()){
                if(data.get()){
                    return status(200, "Deleted Correctly");
                }
            }
            return status(404, "Admin not found");
        }, executionContext.current());

    }

    public CompletionStage<Result> updateAdmin(String id) {
        JsonNode jsonNode = request().body().asJson();
        Admin admin = Json.fromJson(jsonNode, Admin.class);
        return adminModule.update(id, admin).thenApplyAsync(data -> {
            if (data.isPresent()){
                if(data.get()){
                    return status(201, "Updated successfully");
                }
            }
            return status(404, "Admin not found");
        }, executionContext.current());
    }

}
