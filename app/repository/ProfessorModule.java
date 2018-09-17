package repository;

import io.ebean.Transaction;
import models.Professor;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import org.springframework.beans.BeanUtils;
import play.db.ebean.EbeanConfig;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.inject.Inject;
import java.util.List;
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
                    entity.id = id;
                    BeanUtils.copyProperties(entity, professor);
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
        return supplyAsync(() -> {
            try {
                final Optional<Professor> professorOptional = Optional.ofNullable(ebeanServer.find(Professor.class).setId(id).findOne());
                if (professorOptional.isPresent()) {
                    professorOptional.get().delete();
                    return Optional.of(true);
                }
                else return Optional.of(false);
            } catch (Exception e) {
                return Optional.of(false);
            }
        }, executionContext);
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
        return supplyAsync(() -> {
            Transaction txn = ebeanServer.beginTransaction();
            Optional<Professor> value = Optional.empty();
            try {
                Professor professor = ebeanServer.find(Professor.class).setId(id).findOne();
                if (professor != null) {
                    value = Optional.of(professor);
                }
            } finally {
                txn.end();
            }
            return value;
        }, executionContext);
    }

    public CompletionStage<Optional<List<Professor>>> getAll() {
        return supplyAsync(() -> {
            Transaction txn = ebeanServer.beginTransaction();
            Optional<List<Professor>> value = Optional.empty();
            try {

                List<Professor> professorList = ebeanServer.find(Professor.class).findList();
                if(!professorList.isEmpty()){
                    value = Optional.of(professorList);

                }
            } finally {
                txn.end();
            }
            return value;
        }, executionContext);
    }

}
