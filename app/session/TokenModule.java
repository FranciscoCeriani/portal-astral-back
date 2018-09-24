package session;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.Transaction;
import models.Token;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class TokenModule {

    private final EbeanServer ebeanServer;

    TokenModule() {
        this.ebeanServer = Ebean.getDefaultServer();
    }

    /**
     * Inserts a token into the database.
     * This method deletes any previous stored tokens for the same user.
     * @param token The token to be saved.
     * @return The id of the token that was inserted.
     */
    public CompletionStage<String> insert(Token token) {
        return supplyAsync(() -> {

            final Optional<Token> tokenOptional = Optional.ofNullable(ebeanServer.find(Token.class)
                    .where()
                    .eq("userId", token.getUserId())
                    .findOne());
            tokenOptional.ifPresent(ebeanServer::delete);

            token.id = UUID.randomUUID().toString();
            ebeanServer.insert(token);
            return token.id;
        });
    }

    /**
     * Deletes the tokens that exist for a given user
     * @param userId The id of the User.
     * @return true if the delete was successful. If there was an error or no tokens were found, false is returned.
     */
    public CompletionStage<Boolean> delete(String userId) {
        return supplyAsync(() -> {
            try {
                final Optional<Token> tokenOptional = Optional.ofNullable(ebeanServer.find(Token.class)
                        .where()
                        .eq("userId", userId)
                        .findOne());
                if(tokenOptional.isPresent()){
                    ebeanServer.delete(tokenOptional.get());
                    return true;
                }else{
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        });
    }

    /**
     * Verifies a token.
     *
     * Returns a the token if certain conditions are met.
     * There must be a token in the database with the provided id.
     * That token must be valid.
     *
     * If a token is found but it is invalid, it will be deleted.
     *
     * When the conditions are met, the token's timer will be reset.
     *
     * @param id The id of the token to be verified.
     * @return An Optional<Token> in case it was created. If the token was not found or it was invalid, this
     * method returns an Optional.empty();
     */
    public CompletionStage<Optional<Token>> get(String id) {
        return supplyAsync(() -> {
            Transaction txn = ebeanServer.beginTransaction();
            Optional<Token> value = Optional.empty();
            try {
                Token savedToken = ebeanServer.find(Token.class).setId(id).findOne();
                if (savedToken != null) {
                    if (savedToken.isValid()) {
                        savedToken.reset();
                        savedToken.update();
                        txn.commit();
                        value = Optional.of(savedToken);
                    } else {
                        delete(savedToken.getUserId());
                    }
                }
            } finally {
                txn.end();
            }
            return value;
        });
    }
}
