package repository;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
                if (examInscription.isPresent()) {
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

//    Devuelve todos los ExamInscription pertenecientes a Student
    public CompletionStage<List<ExamInscription>> getAllExamStudent(String idStudent) {
        return supplyAsync(() -> {
            Transaction txn = ebeanServer.beginTransaction();
            List<ExamInscription> result = new ArrayList<>();
            try {
                Student student = ebeanServer.find(Student.class).setId(idStudent).findOne();
                if (student != null) {
                    result = getExamsIns(student);
                }
            } finally {
                txn.end();
            }
            return result;
        }, executionContext);
    }

//    Devuelve todos los ExamInscription pertenecientes a Exam
    public CompletionStage<List<ExamInscription>> getAllExam(String idExam) {
        return supplyAsync(() -> {
            Transaction txn = ebeanServer.beginTransaction();
            List<ExamInscription> result = new ArrayList<>();
            try {
                Exam exam = ebeanServer.find(Exam.class).setId(idExam).findOne();
                if (exam != null) {
                    result = getExamsIns(exam);
                }
            } finally {
                txn.end();
            }
            return result;
        }, executionContext);
    }

//    Devuelve todos los ExamInscription pertenecientes a Student y Exam
    public CompletionStage<Optional<ExamInscription>> getExamIns(String idStudent, String idExam) {
        return supplyAsync(() -> {
            Transaction txn = ebeanServer.beginTransaction();
            Optional<ExamInscription> value = Optional.empty();
            try {
                Student student = ebeanServer.find(Student.class).setId(idStudent).findOne();
                Exam exam = ebeanServer.find(Exam.class).setId(idExam).findOne();
                if (student != null && exam != null) {
                    List<ExamInscription> allExams = getExamsIns(exam);
                    if (allExams != null) {
                        for (ExamInscription i : allExams) {
                            if (i.student.equals(student) && i.exam.equals(exam)) {
                                value = Optional.of(i);
                                break;
                            }
                        }
                    }
                }
            } finally {
                txn.end();
            }
            return value;
        }, executionContext);
    }

//    Devuelve todos los ExamInscription pertenecientes a Student
    private List<ExamInscription> getExamsIns(Student student) {
        Transaction txn = ebeanServer.beginTransaction();
        try {
            List<ExamInscription> allExams = ebeanServer.find(ExamInscription.class).findList();
            if (allExams != null) {
                List<ExamInscription> result = new ArrayList<>();
                for (ExamInscription i : allExams) {
                    if (i.student.equals(student)) {
                        result.add(i);
                    }
                }
                return result;
            } else {
                return new ArrayList<>();
            }
        } finally {
            txn.end();
        }
    }

//    Devuelve todos los ExamInscription pertenecientes a Exam
    private List<ExamInscription> getExamsIns(Exam exam) {
        Transaction txn = ebeanServer.beginTransaction();
        try {
            List<ExamInscription> allExams = ebeanServer.find(ExamInscription.class).findList();
            if (allExams != null) {
                List<ExamInscription> result = new ArrayList<>();
                for (ExamInscription i : allExams) {
                    if (i.exam.equals(exam)) {
                        result.add(i);
                    }
                }
                return result;
            } else {
                return new ArrayList<>();
            }
        } finally {
            txn.end();
        }
    }
}
