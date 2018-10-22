package session;
import com.typesafe.config.ConfigFactory;
import models.Token;
import play.mvc.*;

import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class SessionManager extends Action.Simple{

    private final TokenModule tokenModule;

    private final int tokenLifespan;

    @Inject
    private SessionManager() {
        this.tokenLifespan = ConfigFactory.load().getInt("token-duration");
        this.tokenModule = new TokenModule();
    }

    /**
     * Returns a Token id for the User id provided.
     * Removes any previous tokens for that user if any were present.
     * As this is the only way of creating tokens, previous tokens for a user can at most be one.
     * @param id The User's id.
     * @return The id of the Token created.
     */
    public CompletionStage<String> getToken(String id) {
        Token token = new Token(id, this.tokenLifespan);
        return tokenModule.insert(token);
    }

    /**
     * Deletes a any tokens for the User id provided.
     * @param id The User's id
     * @return true if a token was deleted successfully.
     * Returns false if the token could not be deleted or no token existed.
     */
    public CompletionStage<Boolean> deleteToken(String id) {
        return tokenModule.delete(id);
    }

    /**
     * Verifies if a valid token exists.
     * The token will be taken from a cookie and looked for in the database.
     *
     * Any method annotated @With(SessionManager.class) will first call this method to verify if a valid token exists
     * In case all methods in a class require this verification, the annotation can be used at the class level.
     *
     * @return If a valid token exists, the call is delegated to the method that was originally called.
     * In case no token exists or the token provided is invalid, an unauthorized result is returned.
     *
     * If the a valid token is found, its timer will be reset.
     */
    @Override
    public CompletionStage<Result> call(Http.Context ctx) {
        Optional<String> tokenId = getTokenFromCookie(ctx);
        Result unauthorized = Results.unauthorized("unauthorized");
        CompletionStage<Result> response = CompletableFuture.completedFuture(unauthorized);
        if (tokenId.isPresent()) {
            return tokenModule.get(tokenId.get()).thenCompose(data -> {
                if (data.isPresent()) {
                    return delegate.call(ctx);
                }
                return response;
            });
        }
        return response;
    }

    /**
     * Retrieves the authentication token from the cookie.
     *
     * The cookie must be called "Token".
     *
     * @param ctx The Http.Context of the call
     * @return An Optional<String> containing the tokenId if it was present in the cookie.
     */
    private Optional<String> getTokenFromCookie(Http.Context ctx) {
        Optional<String> value = Optional.empty();
        Http.Cookie token = ctx.request().cookies().get("Token");
        if (token != null) {
            value = Optional.of(token.value());
        }
        return  value;
    }
}
