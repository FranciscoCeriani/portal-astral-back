package repository;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.Transaction;
import models.Student;
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

public class SubjectModule implements IModule<Subject> {

    private final EbeanServer ebeanServer;
    private final DatabaseExecutionContext executionContext;

    @Inject
    public SubjectModule(EbeanConfig ebeanConfig, DatabaseExecutionContext executionContext) {
        this.ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
        this.executionContext = executionContext;
    }

    @Override
    public CompletionStage<Optional<Boolean>> update(String id, Subject entity) {
        return supplyAsync(() -> {
            Transaction txn = ebeanServer.beginTransaction();
            Optional<Boolean> value = Optional.of(false);
            try {
                Subject savedSubject = ebeanServer.find(Subject.class).setId(id).findOne();
                if (savedSubject != null) {
                    entity.id = id;
                    BeanUtils.copyProperties(entity, savedSubject);
                    savedSubject.update();
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
                final Optional<Subject> subject = Optional.ofNullable(ebeanServer.find(Subject.class, id));
                if (subject.isPresent()){
                    ebeanServer.delete(subject.get());
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
    public CompletionStage<Try<String>> insert(Subject entity) {
        return supplyAsync(() -> {
            Subject subjectInDatabase = ebeanServer.find(Subject.class)
                    .where().eq("subjectName", entity.subjectName).eq("careerYear", entity.careerYear)
                    .findOne();
            if (subjectInDatabase == null) {
                if (checkRequiredSubjects(entity)) {
                    entity.id = UUID.randomUUID().toString();
                    ebeanServer.insert(entity);
                    return new Success(entity.id);
                } else {
                    return new Failure(new Exception("Required subject does not exist"));
                }
            } else {
                return new Failure(new Exception("Subject exists"));
            }
        }, executionContext);
    }

    private boolean checkRequiredSubjects(Subject subject) {
        Subject subjectToTest;
        List<String> subjects = subject.requiredSubjects;
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
    public CompletionStage<Optional<Subject>> get(String id) {
        return supplyAsync(() -> {
            Transaction txn = ebeanServer.beginTransaction();
            Optional<Subject> value = Optional.empty();
            try {
                Subject savedSubject = ebeanServer.find(Subject.class).setId(id).findOne();
                if (savedSubject != null) {
                    value = Optional.of(savedSubject);
                }
            } finally {
                txn.end();
            }
            return value;
        }, executionContext);
    }

    public CompletionStage<List<Subject>> getAll() {
        return supplyAsync(() -> {
            Transaction txn = ebeanServer.beginTransaction();
            try {
                List<Subject> allSubjects = ebeanServer.find(Subject.class).findList();
                if (allSubjects != null) {
                    return allSubjects;
                } else {
                    return new ArrayList<Subject>();
                }
            } finally {
                txn.end();
            }
        }, executionContext);
    }

    public CompletionStage<Optional<Subject>> addStudentToSubject(Student student, String subjectID) {
        return supplyAsync(() -> {
            Transaction txn = ebeanServer.beginTransaction();
            Optional<Subject> value = Optional.empty();
            try {
                Subject subject = ebeanServer.find(Subject.class).setId(subjectID).findOne();
                if (subject != null) {
                    subject.students.add(student);
                    update(subjectID, subject);
                    value = Optional.of(subject);
                }
            } finally {
                txn.end();
            }
            return value;
        }, executionContext);
    }

    public CompletionStage<Optional<Subject>> addRequiredSubject(String subjectID, String requiredSubjectID) {
        return supplyAsync(() -> {
            Transaction txn = ebeanServer.beginTransaction();
            Optional<Subject> value = Optional.empty();
            try {
                Subject requiredSubject = ebeanServer.find(Subject.class).setId(requiredSubjectID).findOne();
                Subject subject = ebeanServer.find(Subject.class).setId(subjectID).findOne();
                if (subject != null && requiredSubject != null) {
                    subject.addRequiredSubject(requiredSubjectID);
                    subject.update();
                    txn.commit();
                    value = Optional.of(subject);
                }
            } finally {
                txn.end();
            }
            return value;
        }, executionContext);
    }

    public CompletionStage<Optional<Boolean>> deleteRequiredSubject(String subjectID, String requiredSubjectID) {
        return supplyAsync(() -> {
            Transaction txn = ebeanServer.beginTransaction();
            Optional<Boolean> value = Optional.of(false);
            try {
                Subject requiredSubject = ebeanServer.find(Subject.class).setId(requiredSubjectID).findOne();
                Subject subject = ebeanServer.find(Subject.class).setId(subjectID).findOne();
                if (subject != null && requiredSubject != null) {
                    boolean result = subject.deleteRequiredSubject(requiredSubjectID);
                    subject.update();
                    txn.commit();
                    value = Optional.of(result);
                }
            } finally {
                txn.end();
            }
            return value;
        }, executionContext);
    }
}
