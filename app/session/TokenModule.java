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

    //@Inject
    public TokenModule() {
        //this.ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
        this.ebeanServer = Ebean.getDefaultServer();
    }

    public CompletionStage<String> insert(Token token) {
        return supplyAsync(() -> {

            final Optional<Token> tokenOptional = Optional.ofNullable(ebeanServer.find(Token.class).where().eq("userId", token.getId()).findOne());
            tokenOptional.ifPresent(ebeanServer::delete);

            token.id = UUID.randomUUID().toString();
            ebeanServer.insert(token);
            return token.id;
        });
    }

    //should the id provided for the method be the user id or the token id?
    public CompletionStage<Optional<Boolean>> delete(String userId) {
        return supplyAsync(() -> {
            try {
                //final Optional<Token> tokenOptional = Optional.ofNullable(ebeanServer.find(Token.class).findOne());
                //final Optional<Token> tokenOptional = Optional.ofNullable(ebeanServer.createQuery(Token.class).setParameter("userId", userId).findOne());
                final Optional<Token> tokenOptional = Optional.ofNullable(ebeanServer.find(Token.class).where().eq("userId", userId).findOne());
                if(tokenOptional.isPresent()){
                    ebeanServer.delete(tokenOptional.get());
                    return Optional.of(true);
                }else{
                    return Optional.of(false);
                }
            } catch (Exception e) {
                return Optional.of(false);
            }
        });
    }

    //token id
    public CompletionStage<Optional<Token>> get(String id) {
        return supplyAsync(() -> {
            Transaction txn = ebeanServer.beginTransaction();
            Optional<Token> value = Optional.empty();
            try {
                Token savedToken = ebeanServer.find(Token.class).setId(id).findOne();
                if (savedToken != null) {
                    value = Optional.of(savedToken);
                }
            } finally {
                txn.end();
            }
            return value;
        });
    }
}
