package repository;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.Model;
import io.ebean.Transaction;
import models.Admin;
import play.db.ebean.EbeanConfig;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.inject.Inject;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class AdminModule implements IModule<Admin>{

    private final EbeanServer ebeanServer;
    private final DatabaseExecutionContext executionContext;

    @Inject
    public AdminModule(EbeanConfig ebeanConfig, DatabaseExecutionContext executionContext) {
        this.ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
        this.executionContext = executionContext;
    }

    @Override
    public CompletionStage<Optional<Boolean>> update(String id, Admin entity) {
        throw new NotImplementedException();
    }

    @Override
    public CompletionStage<Optional<Boolean>> delete(String id) {
          return supplyAsync(() -> {
            try {
                final Optional<Admin> computerOptional = Optional.ofNullable(ebeanServer.find(Admin.class).setId(id).findOne());
                computerOptional.ifPresent(Model::delete);
                return Optional.of(true);
            } catch (Exception e) {
                return Optional.of(false);
            }
        }, executionContext);
    }

    @Override
    public CompletionStage<String> insert(Admin entity) {
        return supplyAsync(() -> {
            entity.id = UUID.randomUUID().toString();
            ebeanServer.insert(entity);
            return entity.id;
        }, executionContext);
    }

    @Override
    public CompletionStage<Optional<Admin>> get(String id) {
        throw new NotImplementedException();
    }
}
