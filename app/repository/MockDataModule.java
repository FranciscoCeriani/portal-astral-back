package repository;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.Transaction;
import models.*;
import org.joda.time.DateTime;
import play.db.ebean.EbeanConfig;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class MockDataModule {
    private final EbeanServer ebeanServer;
    private final DatabaseExecutionContext executionContext;

    @Inject
    public MockDataModule(EbeanConfig ebeanConfig, DatabaseExecutionContext executionContext) {
        this.ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
        this.executionContext = executionContext;
    }

    public CompletionStage<Optional<Boolean>> createMockData() {
        return supplyAsync(() -> {
            Admin admin = new Admin("Armando", "Paredes", "file", "admin@admin.com", "admin");
            admin.id = UUID.randomUUID().toString();
            Professor professor = new Professor("Pablo", "Clavito", "file", "profe@profe.com", "profe");
            professor.id = UUID.randomUUID().toString();
            Student student = new Student("Esteban", "Quito", "file", "student@student.com", "student", "1998-02-08", "Crack", "Crack de Cracks", "Al fondo a la derecha");
            student.id = UUID.randomUUID().toString();

            Subject subject1 = new Subject("Física I", 1, new ArrayList<>());
            subject1.id = UUID.randomUUID().toString();
            Subject subject2 = new Subject("Análisis I", 1, new ArrayList<>());
            subject2.id = UUID.randomUUID().toString();
            Subject subject3 = new Subject("Programación I", 1, new ArrayList<>());
            subject3.id = UUID.randomUUID().toString();

            Subject subject4 = new Subject("Física II", 2, new ArrayList<>());
            subject4.id = UUID.randomUUID().toString();
            Subject subject5 = new Subject("Análisis II", 1, new ArrayList<>());
            subject5.id = UUID.randomUUID().toString();
            Subject subject6 = new Subject("Programación II", 2, new ArrayList<>());
            subject6.id = UUID.randomUUID().toString();

            subject4.requiredSubjects.add(subject1.id);
            subject4.requiredSubjects.add(subject2.id);

            subject5.requiredSubjects.add(subject2.id);

            subject6.requiredSubjects.add(subject3.id);
            subject6.requiredSubjects.add(subject2.id);
            subject6.requiredSubjects.add(subject1.id);


            Career career = new Career("Ing. Informatica", new ArrayList<>(), new ArrayList<>());
            career.id = UUID.randomUUID().toString();
            career.careerSubjects.add(subject1.id);
            career.careerSubjects.add(subject2.id);
            career.careerSubjects.add(subject3.id);
            career.careerSubjects.add(subject4.id);
            career.careerSubjects.add(subject5.id);
            career.careerSubjects.add(subject6.id);
            career.students.add(student);

            Course course1 = new Course("2019-02-03", "2019-07-04", subject1);
            course1.id = UUID.randomUUID().toString();
            Course course2 = new Course("2019-02-03", "2019-07-04", subject2);
            course2.id = UUID.randomUUID().toString();
            Course course3 = new Course("2019-02-03", "2019-07-04", subject3);
            course3.id = UUID.randomUUID().toString();
            Course course4 = new Course("2018-11-20", "2018-12-20", subject4);
            course4.id = UUID.randomUUID().toString();
            Course course5 = new Course("2018-11-21", "2018-12-21", subject5);
            course5.id = UUID.randomUUID().toString();
            Course course6 = new Course("2018-11-22", "2018-12-22", subject6);
            course6.id = UUID.randomUUID().toString();

            DictationHours dictationHours1 = new DictationHours("Lunes", DateTime.now(), DateTime.now());
            dictationHours1.id = UUID.randomUUID().toString();
            DictationHours dictationHours2 = new DictationHours("Viernes", DateTime.now(), DateTime.now());
            dictationHours2.id = UUID.randomUUID().toString();

//            course1.schedule.add(dictationHours1);
//            course1.schedule.add(dictationHours2);
//            course2.schedule.add(dictationHours1);
//            course2.schedule.add(dictationHours2);
//            course3.schedule.add(dictationHours1);
//            course3.schedule.add(dictationHours2);

            Exam exam1 = new Exam(course1, "2019-05-04");
            exam1.id = UUID.randomUUID().toString();
            Exam exam2 = new Exam(course2, "2019-05-08");
            exam2.id = UUID.randomUUID().toString();
            Exam exam3 = new Exam(course3, "2019-05-16");
            exam3.id = UUID.randomUUID().toString();
            Exam exam4 = new Exam(course1, "2019-06-04");
            exam4.id = UUID.randomUUID().toString();
            Exam exam5 = new Exam(course1, "2019-07-04");
            exam5.id = UUID.randomUUID().toString();

            ExamInscription examInscription1 = new ExamInscription(student, exam1);
            examInscription1.id = UUID.randomUUID().toString();
            examInscription1.result = 9;
            ExamInscription examInscription2 = new ExamInscription(student, exam2);
            examInscription2.id = UUID.randomUUID().toString();
            examInscription2.result = 9;
            ExamInscription examInscription3 = new ExamInscription(student, exam3);
            examInscription3.id = UUID.randomUUID().toString();
            examInscription3.result = 9;
            ExamInscription examInscription4 = new ExamInscription(student, exam4);
            examInscription4.id = UUID.randomUUID().toString();
            examInscription4.result = 8;
            ExamInscription examInscription5 = new ExamInscription(student, exam5);
            examInscription5.id = UUID.randomUUID().toString();
            examInscription5.result = 7;

            Transaction txn = ebeanServer.beginTransaction();
            Optional<Boolean> result;
            try {
                ebeanServer.insert(admin);
                ebeanServer.insert(professor);
                ebeanServer.insert(student);
                ebeanServer.insert(subject1);
                ebeanServer.insert(subject2);
                ebeanServer.insert(subject3);
                ebeanServer.insert(subject4);
                ebeanServer.insert(subject5);
                ebeanServer.insert(subject6);
                ebeanServer.insert(career);
                ebeanServer.insert(dictationHours1);
                ebeanServer.insert(dictationHours2);
                ebeanServer.insert(course1);
                ebeanServer.insert(course2);
                ebeanServer.insert(course3);
                ebeanServer.insert(exam1);
                ebeanServer.insert(exam2);
                ebeanServer.insert(exam3);
                ebeanServer.insert(exam4);
                ebeanServer.insert(exam5);
                ebeanServer.insert(examInscription1);
                ebeanServer.insert(examInscription2);
                ebeanServer.insert(examInscription3);
                ebeanServer.insert(examInscription4);
                ebeanServer.insert(examInscription5);

                result = Optional.of(true);
                txn.commit();
            } finally {
                txn.end();
            }

            return result;
        }, executionContext);
    }
}
