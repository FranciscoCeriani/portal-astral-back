package repository;

import io.ebean.DuplicateKeyException;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.Transaction;
import models.Student;
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

public class StudentModule implements IModule<Student> {

    private final EbeanServer ebeanServer;
    private final DatabaseExecutionContext executionContext;

    @Inject
    public StudentModule(EbeanConfig ebeanConfig, DatabaseExecutionContext executionContext) {
        this.ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
        this.executionContext = executionContext;
    }

    @Override
    public CompletionStage<Try<String>> insert(Student entity) {
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
    public CompletionStage<Optional<Boolean>> update(String id, Student entity) {
        return supplyAsync(() -> {
            Transaction txn = ebeanServer.beginTransaction();
            Optional<Boolean> value = Optional.of(false);
            try {
                Student savedStudent = ebeanServer.find(Student.class).setId(id).findOne();
                if (savedStudent != null) {
                    entity.id = id;
                    BeanUtils.copyProperties(entity, savedStudent);
                    savedStudent.update();
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
                final Optional<Student> student = Optional.ofNullable(ebeanServer.find(Student.class, id));
                if (student.isPresent()){
                    ebeanServer.delete(student.get());
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
    public CompletionStage<Optional<Student>> get(String id) {
        return supplyAsync(() -> {
            Transaction txn = ebeanServer.beginTransaction();
            Optional<Student> value = Optional.empty();
            try {
                Student savedStudent = ebeanServer.find(Student.class).setId(id).findOne();
                if (savedStudent != null) {
                    value = Optional.of(savedStudent);
                }
            } finally {
                txn.end();
            }
            return value;
        }, executionContext);
    }

    public CompletionStage<List<Student>> getAll() {
        return supplyAsync(() -> {
            Transaction txn = ebeanServer.beginTransaction();
            List<Student> value;
            try {

                List<Student> studentList = ebeanServer.find(Student.class).findList();
                value = studentList;
            } finally {
                txn.end();
            }
            return value;
        }, executionContext);
    }
}
