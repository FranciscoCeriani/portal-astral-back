package controllers;


import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import session.SessionManager;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class LogoutController extends Controller {
    private final HttpExecutionContext executionContext;
    private final SessionManager sessionManager;

    @Inject
    public LogoutController(HttpExecutionContext executionContext, SessionManager sessionManager) {
        this.executionContext = executionContext;
        this.sessionManager = sessionManager;
    }

    @With(SessionManager.class)
    public CompletionStage<Result> logout(String id) {
        return sessionManager.deleteToken(id).thenApplyAsync(data -> {
            if(data) {
                response().discardCookie("Token");
                return status(201, "Logged out succesfully");
            }
            else {
                return status(409, "Error logging out");
            }
        }, executionContext.current());

    }

}
