package repository;

import io.ebean.DuplicateKeyException;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.Transaction;
import models.Exam;
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

public class ExamModule implements IModule<Exam> {

    private final EbeanServer ebeanServer;
    private final DatabaseExecutionContext executionContext;

    @Inject
    public ExamModule(EbeanConfig ebeanConfig, DatabaseExecutionContext executionContext) {
        this.ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
        this.executionContext = executionContext;
    }
    @Override
    public CompletionStage<Optional<Boolean>> update(String id, Exam exam) {
        return supplyAsync(() -> {
            Transaction txn = ebeanServer.beginTransaction();
            Optional<Boolean> value = Optional.of(false);
            try {
                Exam savedExam = ebeanServer.find(Exam.class).setId(id).findOne();
                if (savedExam != null) {
                    exam.id = id;
                    BeanUtils.copyProperties(exam, savedExam);
                    savedExam.update();
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
                final Optional<Exam> computerOptional = Optional.ofNullable(ebeanServer.find(Exam.class).setId(id).findOne());
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
    public CompletionStage<Try<String>> insert(Exam exam) {
        return supplyAsync(() -> {
            try {
                exam.id = UUID.randomUUID().toString();
                ebeanServer.insert(exam);
                return new Success(exam.id);
            }catch (DuplicateKeyException e){
                return new Failure(new Exception("Email already exists"));
            }
        }, executionContext);
    }

    @Override
    public CompletionStage<Optional<Exam>> get(String id) {
        return supplyAsync(() -> {
            Transaction txn = ebeanServer.beginTransaction();
            Optional<Exam> value = Optional.empty();
            try {
                Exam savedExam = ebeanServer.find(Exam.class).setId(id).findOne();
                if (savedExam != null) {
                    value = Optional.of(savedExam);
                }
            } finally {
                txn.end();
            }
            return value;
        }, executionContext);
    }

    @Override
    public CompletionStage<List<Exam>> getAll() {
        return supplyAsync(() -> {
            Transaction txn = ebeanServer.beginTransaction();
            List<Exam> value;
            try {
                List<Exam> savedExams = ebeanServer.find(Exam.class).findList();
                value = savedExams;
            } finally {
                txn.end();
            }
            return value;
        }, executionContext);
    }
}
