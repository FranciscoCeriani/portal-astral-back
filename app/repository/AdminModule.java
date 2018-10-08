package repository;

import io.ebean.DuplicateKeyException;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.Transaction;
import models.Admin;
import org.springframework.beans.BeanUtils;
import play.db.ebean.EbeanConfig;
import scala.util.Failure;
import scala.util.Success;
import scala.util.Try;

import javax.inject.Inject;
import java.util.List;
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
                    entity.id = id;
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
    public CompletionStage<Try<String>> insert(Admin entity) {
        return supplyAsync(() -> {
            try {
                entity.id = UUID.randomUUID().toString();
                ebeanServer.insert(entity);
                return new Success(entity.id);
            }catch (DuplicateKeyException e){
                return new Failure(new Exception("Email already exists"));
            }
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

    public CompletionStage<List<Admin>> getAll() {
        return supplyAsync(() -> {
            Transaction txn = ebeanServer.beginTransaction();
            List<Admin> value;
            try {
                List<Admin> savedAdmins = ebeanServer.find(Admin.class).findList();
                value = savedAdmins;
            } finally {
                txn.end();
            }
            return value;
        }, executionContext);
    }
}

