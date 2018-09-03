package repository;

import io.ebean.Transaction;
import models.Professor;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import play.db.ebean.EbeanConfig;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.inject.Inject;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class ProfessorModule implements IModule<Professor>{

    private final EbeanServer ebeanServer;
    private final DatabaseExecutionContext executionContext;

    @Inject
    public ProfessorModule(EbeanConfig ebeanConfig, DatabaseExecutionContext executionContext) {
        this.ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
        this.executionContext = executionContext;
    }

    @Override
    public CompletionStage<Optional<Boolean>> update(String id, Professor entity) {
        return supplyAsync(() -> {
            Transaction txn = ebeanServer.beginTransaction();
            Optional<Boolean> value = Optional.of(false);
            try {
                Professor professor = ebeanServer.find(Professor.class).setId(id).findOne();
                if (professor != null) {
                    professor.name = entity.name;
                    professor.lastName = entity.lastName;
                    professor.file = entity.file;
                    professor.email = entity.email;
                    professor.password = entity.password;
                    professor.update();
                    txn.commit();
                    value = Optional.of(true);
                }
            } finally {
                txn.end();
            }
            return value;
        }, executionContext);
    }

    @Override
    public CompletionStage<Optional<Boolean>> delete(String id) {
        throw new NotImplementedException();
    }

    @Override
    public CompletionStage<String> insert(Professor entity) {
        return supplyAsync(() -> {
            entity.id = UUID.randomUUID().toString();
            ebeanServer.insert(entity);
            return entity.id;
        }, executionContext);
    }

    @Override
    public CompletionStage<Optional<Professor>> get(String id) {
        throw new NotImplementedException();
    }

}
