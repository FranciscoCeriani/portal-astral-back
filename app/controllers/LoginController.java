package controllers;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Token;
import models.User;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Http;
import play.mvc.Result;
import repository.Authenticator;
import session.SessionManager;


import javax.inject.Inject;
import java.util.concurrent.CompletionStage;
import static play.mvc.Controller.request;
import static play.mvc.Controller.response;
import static play.mvc.Results.status;


public class LoginController {
    private final HttpExecutionContext executionContext;
    private final Authenticator authenticator;
    private final SessionManager sessionManager;

    @Inject
    public LoginController(HttpExecutionContext executionContext, Authenticator authenticator, SessionManager sessionManager) {
        this.executionContext = executionContext;
        this.authenticator = authenticator;
        this.sessionManager = sessionManager;
    }

    public CompletionStage<Result> validateUser(){
        JsonNode jsonNode = request().body().asJson();
        String email = jsonNode.get("email").asText();
        String password = jsonNode.get("password").asText();
        return authenticator.authenticateUser(email,password).thenApplyAsync(data -> {
            if (data.isPresent()) {
                User user = data.get();
                ObjectNode result = Json.newObject();
                result.put("Type" , user.getClass().toString().substring(13));
                result.put("User", Json.toJson(user));
                String tokenID = sessionManager.getToken(user.id).toCompletableFuture().join();
                response().setCookie(
                        Http.Cookie.builder("Token", tokenID)
                                .withHttpOnly(true)
                                .withSameSite(Http.Cookie.SameSite.STRICT)
                                .withSecure(true)
                                .build()
                );
                response().discardCookie("cookie");
                return status(200,  result);
            } else {
                return status(401, "Invalid email/password");
            }
        }, executionContext.current());


    }


}
