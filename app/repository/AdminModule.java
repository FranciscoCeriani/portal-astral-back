package repository;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.Model;
import io.ebean.Transaction;
import models.Admin;
import org.springframework.beans.BeanUtils;
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
        return supplyAsync(() -> {
            Transaction txn = ebeanServer.beginTransaction();
            Optional<Boolean> value = Optional.of(false);
            try {
                Admin savedAdmin = ebeanServer.find(Admin.class).setId(id).findOne();
                if (savedAdmin != null) {
                    BeanUtils.copyProperties(entity, savedAdmin);
                    savedAdmin.update();
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
                final Optional<Admin> computerOptional = Optional.ofNullable(ebeanServer.find(Admin.class).setId(id).findOne());
                if(computerOptional.isPresent()){
                    computerOptional.get().delete();
                    return Optional.of(true);
                }else{
                    return Optional.of(false);
                }
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
        return supplyAsync(() -> {
            Transaction txn = ebeanServer.beginTransaction();
            Optional<Admin> value = Optional.empty();
            try {
                Admin savedAdmin = ebeanServer.find(Admin.class).setId(id).findOne();
                if (savedAdmin != null) {
                    value = Optional.of(savedAdmin);
                }
            } finally {
                txn.end();
            }
            return value;
        }, executionContext);
    }
}
