package controllers;

import models.Admin;
import models.Professor;
import models.Student;
import models.Subject;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import repository.AdminModule;
import repository.ProfessorModule;
import repository.StudentModule;
import repository.SubjectModule;
import scala.util.Success;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.concurrent.CompletionStage;

public class MockDataController extends Controller {
    private final HttpExecutionContext executionContext;
    private final StudentModule studentModule;
    private final ProfessorModule professorModule;
    private final AdminModule adminModule;
    private final SubjectModule subjectModule;

    @Inject

    public MockDataController(HttpExecutionContext executionContext, StudentModule studentModule, ProfessorModule professorModule, AdminModule adminModule, SubjectModule subjectModule) {
        this.executionContext = executionContext;
        this.studentModule = studentModule;
        this.professorModule = professorModule;
        this.adminModule = adminModule;
        this.subjectModule = subjectModule;
    }

    public CompletionStage<Result> initData() {
        Admin admin = new Admin("Armando", "Paredes", "file", "admin@admin.com", "admin");
        Professor professor = new Professor("Pablo", "Clavito", "file", "profe@profe.com", "profe");
        Student student = new Student("Esteban", "Quito", "file", "student@student.com", "student", "08/02/1998", "Crack", "Crack de Cracks", "Al fondo a la derecha");

        Subject subject1 = new Subject("Física I", 1, new ArrayList<>(), new ArrayList<>());
        Subject subject2 = new Subject("Análisis I", 1, new ArrayList<>(), new ArrayList<>());
        Subject subject3 = new Subject("Progeamación I", 2, new ArrayList<>(), new ArrayList<>());

        subjectModule.insert(subject1);
        subjectModule.insert(subject2);
        subjectModule.insert(subject3);

        studentModule.insert(student);
        professorModule.insert(professor);
        return adminModule.insert(admin).thenApplyAsync(data -> {
             return ok();
             }, executionContext.current());
    }
}
