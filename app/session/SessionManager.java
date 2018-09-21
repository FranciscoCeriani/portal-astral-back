package session;
import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.ConfigFactory;
import models.Admin;
import models.Token;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.*;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.inject.Inject;
import java.io.StreamCorruptedException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import static java.util.concurrent.CompletableFuture.supplyAsync;

public class SessionManager extends Action.Simple{

    private final TokenModule tokenModule;
    //private HttpExecutionContext executionContext;

    private static SessionManager singleInstance = null;
    private final int tokenLifespan;

    public static SessionManager getInstance() {
        if (singleInstance == null) {
            singleInstance = new SessionManager();
        }
        return singleInstance;
    }

    @Inject
    private SessionManager() {
        this.tokenLifespan = ConfigFactory.load().getInt("token-duration");
        this.tokenModule = new TokenModule();
    }

    /*
    @Inject
    public SessionManager(HttpExecutionContext executionContext) {
        this.tokenLifespan = ConfigFactory.load().getInt("token-duration");
        this.tokenModule = new TokenModule();
        this.executionContext = executionContext;
    }
    */

    /**
     * Returns a Token id for the User id provided.
     * Removes any previous tokens for that user if any were present.
     * As this is the only way of creating tokens, previous tokens for a user can at most be one.
     * @param id The User's id.
     * @return The id of the Token created.
     */
    //how can we manage the insertion and deletion?
    //insertion should wait for deletion to finish because otherwise what is being inserted could be deleted.
    //could create a new method in TokenModule that deletes and then inserts to solve this.
    public CompletionStage<String> getToken(String id) {
        Timestamp validUntil = new Timestamp(System.currentTimeMillis() + this.tokenLifespan * 60000); //There are 60000 milliseconds in a minute
        Token token = new Token(id, validUntil);
        return tokenModule.insert(token); //we save the new token that was requested by the user.
    }

    /**
     * Deletes a any tokens for the User id provided.
     * @param id The User's id
     * @return true if a token was deleted successfully.
     * Returns false if the token could not be deleted or no token existed.
     */
    //maybe change it so that it returns a CompletionStage<Boolean> instead
    //should change the return type of method delete in TokenModule
    public CompletionStage<Optional<Boolean>> deleteToken(String id) {
        return tokenModule.delete(id);
    }



    //JsonNode jsonNode = request().cookies();

    /*
    public Optional<Token> verifyToken(Http.Context ctx) {
        try {
            Optional<Token> optionalToken = tokenModule.get(token.id).toCompletableFuture().get();
            if (optionalToken.isPresent()) {
                if (optionalToken.get().isValid(new Timestamp(System.currentTimeMillis()))) {
                    return Optional.of(getToken(token.getId()));
                } else {
                    deleteToken(token.getId());
                }
            }
        } catch (Exception e) {
            return Optional.empty();
        }
        return Optional.empty();
    }
    */

    /**
     * Verifies if a valid token exists.
     * The token will be taken from a cookie and looked for in the database.
     *
     * Any method annotated @With(SessionManager.class) will first call this method to verify if a valid token exists
     * In case all methods in a class require this verification, the annotation can be used at the class level.
     *
     * @return If a valid token exists, the call is delegated to the method that was originally called.
     * In case no token exists or the token provided is invalid, an unauthorized result is returned.
     */
    @Override
    public CompletionStage<Result> call(Http.Context ctx) {
        String token = getTokenFromCookie(ctx);
        return tokenModule.get(token).thenCompose(data -> {
            data = Optional.of(new Token("dude", new Timestamp(System.currentTimeMillis() - 100000000))); //DELETE
            if (data.isPresent()) {
                if (data.get().isValid(new Timestamp(System.currentTimeMillis()))) {
                    return delegate.call(ctx);
                } else {
                    deleteToken(token);
                }
            }
            return CompletableFuture.completedFuture(Results.unauthorized("unauthorized"));
        });
    }

    /*
    CompletionStage<Result> ret = jsonResponse.thenCompose(jsonNode -> {
        if (jsonNode.get("success").equals("true")) {
            return delegate.call(ctx);
        } else {
            return CompletableFuture.completedFuture(unauthorized);
        }
    });
    */

    private String getTokenFromCookie(Http.Context ctx) {
        //Check how to read the cookie data.
        //return ctx.request().cookies().get("Auth-Token").value(); //The token stored in the cookie
        return "This is a token";
    }
}
