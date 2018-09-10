package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.Admin;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import repository.AdminModule;

import javax.inject.Inject;
import java.util.List;
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
        return adminModule.getAll().thenApplyAsync(data -> {
            if (data.isPresent()) {
                List<Admin> admins = data.get();
                return ok(Json.toJson(admins));
            } else {
                return status(404, "No admin found");
            }
        }, executionContext.current());
    }

    public CompletionStage<Result> getAdmin(String id) {
        return adminModule.get(id).thenApplyAsync(data -> {
            if (data.isPresent()) {
                Admin admin = data.get();
                return ok(Json.toJson(admin));
            } else {
                return status(404, "Admin not found");
            }
        }, executionContext.current());
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

}
