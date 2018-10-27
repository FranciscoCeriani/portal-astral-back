package repository;

import com.fasterxml.jackson.databind.JsonNode;
import io.ebean.DuplicateKeyException;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import models.Exam;
import models.Student;
import play.db.ebean.EbeanConfig;
import io.ebean.Transaction;
import models.ExamInscription;
import org.springframework.beans.BeanUtils;
import scala.util.Failure;
import scala.util.Success;
import scala.util.Try;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class ExamInscriptionModule implements IModule<ExamInscription> {

    private final EbeanServer ebeanServer;
    private final DatabaseExecutionContext executionContext;

    @Inject
    public ExamInscriptionModule(EbeanConfig ebeanConfig, DatabaseExecutionContext executionContext) {
        this.ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
        this.executionContext = executionContext;
    }

    @Override
    public CompletionStage<Optional<Boolean>> update(String id, ExamInscription entity) {
        return supplyAsync(() -> {
            Transaction txn = ebeanServer.beginTransaction();
            Optional<Boolean> value = Optional.of(false);
            try {
                ExamInscription savedExamI = ebeanServer.find(ExamInscription.class).setId(id).findOne();
                if (savedExamI != null) {
                    entity.id = id;
                    BeanUtils.copyProperties(entity, savedExamI);
                    savedExamI.update();
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
                final Optional<ExamInscription> examInscription = Optional.ofNullable(ebeanServer.find(ExamInscription.class, id));
                if (examInscription.isPresent()){
                    ebeanServer.delete(examInscription.get());
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
    public CompletionStage<Try<String>> insert(ExamInscription entity) {
        return supplyAsync(() -> {
            try {
                entity.id = UUID.randomUUID().toString();
                ebeanServer.insert(entity);
                return new Success(entity.id);
            } catch (DuplicateKeyException e) {
                return new Failure(new Exception("ExamInscription already exists"));
            }
        }, executionContext);
    }

    @Override
    public CompletionStage<Optional<ExamInscription>> get(String id) {
        return supplyAsync(() -> {
            Transaction txn = ebeanServer.beginTransaction();
            Optional<ExamInscription> value = Optional.empty();
            try {
                ExamInscription savedExamI = ebeanServer.find(ExamInscription.class).setId(id).findOne();
                if (savedExamI != null) {
                    value = Optional.of(savedExamI);
                }
            } finally {
                txn.end();
            }
            return value;
        }, executionContext);
    }

    @Override
    public CompletionStage<List<ExamInscription>> getAll() {
        return supplyAsync(() -> {
            Transaction txn = ebeanServer.beginTransaction();
            try {
                List<ExamInscription> allExams = ebeanServer.find(ExamInscription.class).findList();
                if (allExams != null) {
                    return allExams;
                } else {
                    return new ArrayList<>();
                }
            } finally {
                txn.end();
            }
        }, executionContext);
    }

    public CompletionStage<Optional<Integer>> enrollStudentsToExam(Iterator<JsonNode> studentIdsIterator, String examId) {
        return supplyAsync(() -> {
            Optional<Integer> result = Optional.empty();
            int successfulEnrollments = 0;
            Exam exam = ebeanServer.find(Exam.class).setId(examId).findOne();
            Student student;
            if (exam != null) {
                while (studentIdsIterator.hasNext()) {
                    student = ebeanServer.find(Student.class).setId(studentIdsIterator.next().textValue()).findOne();
                    if (student != null && !checkIfInscriptionExists(student, exam)) {
                        insert(new ExamInscription(student, exam));
                        successfulEnrollments++;
                    }
                }
            }
            result = Optional.of(successfulEnrollments);
            return result;
        }, executionContext);
    }

    private boolean checkIfInscriptionExists(Student student, Exam exam) {
        ExamInscription examInscription = ebeanServer.find(ExamInscription.class)
                .where().eq("student", student).eq("exam", exam)
                .findOne();
        return examInscription != null;
    }
}
