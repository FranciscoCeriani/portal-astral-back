package repository;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.Transaction;
import models.Career;
import models.Subject;
import org.springframework.beans.BeanUtils;
import play.db.ebean.EbeanConfig;
import scala.util.Failure;
import scala.util.Success;
import scala.util.Try;
//import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static play.mvc.Results.ok;
import static play.mvc.Results.status;

public class CareerModule implements IModule<Career> {

    private final EbeanServer ebeanServer;
    private final DatabaseExecutionContext executionContext;

    @Inject
    public CareerModule(EbeanConfig ebeanConfig, DatabaseExecutionContext executionContext) {
        this.ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
        this.executionContext = executionContext;
    }

    @Override
    public CompletionStage<Optional<Boolean>> update(String id, Career entity) {
        return supplyAsync(() -> {
            Transaction txn = ebeanServer.beginTransaction();
            Optional<Boolean> value = Optional.of(false);
            try {
                Career savedCareer = ebeanServer.find(Career.class).setId(id).findOne();
                if (savedCareer != null) {
                    entity.id = id;
                    BeanUtils.copyProperties(entity, savedCareer);
                    savedCareer.update();
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
                final Optional<Career> career = Optional.ofNullable(ebeanServer.find(Career.class, id));
                if (career.isPresent()){
                    ebeanServer.delete(career.get());
                    return Optional.of(true);
                } else {
                    return Optional.of(false);
                }
            } catch (Exception e) {
                return Optional.of(false);
            }
        }, executionContext);
    }

    @Override
    public CompletionStage<Try<String>> insert(Career entity) {
        return supplyAsync(() -> {
            Career careerInDatabase = ebeanServer.find(Career.class)
                    .where().eq("careerName", entity.careerName)
                    .findOne();
            if (careerInDatabase == null) {
                if (checkCareerSubjects(entity)) {
                    entity.id = UUID.randomUUID().toString();
                    ebeanServer.insert(entity);
                    return new Success(entity.id);
                } else {
                    return new Failure(new Exception("Subject does not exist"));
                }
            } else {
                return new Failure(new Exception("Career already exists"));
            }
        }, executionContext);
    }

    //Returns true if the career subjects exist.
    private boolean checkCareerSubjects(Career career) {
        Subject subjectToTest;
        List<String> subjects = career.getCareerSubjects();
        for (String id : subjects) {
            subjectToTest = ebeanServer.find(Subject.class)
                    .where().eq("id", id)
                    .findOne();
            if (subjectToTest == null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public CompletionStage<Optional<Career>> get(String id) {
        return supplyAsync(() -> {
            Transaction txn = ebeanServer.beginTransaction();
            Optional<Career> value = Optional.empty();
            try {
                Career savedCareer = ebeanServer.find(Career.class).setId(id).findOne();
                if (savedCareer != null) {
                    value = Optional.of(savedCareer);
                }
            } finally {
                txn.end();
            }
            return value;
        }, executionContext);
    }

    public CompletionStage<List<Career>> getAll() {
        return supplyAsync(() -> {
            Transaction txn = ebeanServer.beginTransaction();
            try {
                List<Career> allCareers = ebeanServer.find(Career.class).findList();
                if (allCareers != null) {
                    return allCareers;
                } else {
                    return new ArrayList<Career>();
                }
            } finally {
                txn.end();
            }
        }, executionContext);
    }
}
