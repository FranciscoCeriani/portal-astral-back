package controllers;

import models.Admin;
import models.Professor;
import models.Student;
import models.Subject;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import repository.*;
import scala.util.Success;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.concurrent.CompletionStage;

public class MockDataController extends Controller {
    private final HttpExecutionContext executionContext;
    private final MockDataModule mockDataModule;

    @Inject
    public MockDataController(HttpExecutionContext executionContext, MockDataModule mockDataModule) {
        this.executionContext = executionContext;
        this.mockDataModule = mockDataModule;
    }


    public CompletionStage<Result> initData() {
        return mockDataModule.createMockData().thenApplyAsync(data -> {
            if (data.get()){
                return ok("Start Up");
            } else {
                return status(404, "Error");
            }
        }, executionContext.current());
    }
}
