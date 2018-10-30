package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Course;
import models.Exam;
import org.joda.time.IllegalFieldValueException;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import repository.CourseModule;
import repository.ExamModule;
import scala.util.Failure;

import javax.inject.Inject;
import java.util.Iterator;
import java.util.concurrent.CompletionStage;

public class ExamController extends Controller {
    private final HttpExecutionContext executionContext;
    private final ExamModule examModule;
    private final CourseModule courseModule;

    @Inject
    public ExamController(HttpExecutionContext executionContext, ExamModule examModule, CourseModule courseModule) {
        this.executionContext = executionContext;
        this.examModule = examModule;
        this.courseModule = courseModule;
    }

    /**
     * Saves a new Exam for a given course at a given time.
     *
     * Receives a Json in the body of the request with two Strings, a courseID and a dateTime.
     *
     * courseID must be a valid course id
     * dateTime must be in format YYYY-MM-ddTHH:mm:ss (ISO 8601)
     *
     * An example of a Json would be:
     *
     * {
     *     "courseID": "8bc0beda-ac6d-4592-91a0-c303a97f42f8",
     *     "dateTime": "2018-11-08T13:30:00"
     * }
     *
     * Note that the method will still work if the dateTime is incomplete.
     * For example, both "2018-11-08" and "2018-11-08T13:30" would work as expected.
     *
     * @return
     * 409 if there are any errors.
     * 201 if the exam was created successfully.
     */
    public CompletionStage<Result> saveExam() {
        JsonNode jsonNode = request().body().asJson();
        String courseID = jsonNode.get("courseID").textValue();
        String dateTime = jsonNode.get("dateTime").textValue();

        Course course = new Course();
        course.id = courseID;

        Exam exam = new Exam(course, dateTime);
        return examModule.insert(exam).thenApplyAsync(data -> {
            if (data.isSuccess()) {
                return status(201, data.get());
            } else {
                return status(409, ((Failure) data).exception().getMessage());
            }
        }, executionContext.current());
    }

    public CompletionStage<Result> deleteExam(String id){
        return examModule.delete(id).thenApplyAsync(data -> {
            if(data.isPresent()){
                if(data.get()){
                    return status(200, "Deleted Correctly");
                }
            }
            return status(404, "Exam not found");
        }, executionContext.current());
    }

    public CompletionStage<Result> updateExam(String id) {
        JsonNode jsonNode = request().body().asJson();
        Iterator<JsonNode> iterator = jsonNode.elements();
        String courseID = iterator.next().get("id").toString().replace("\"","");
        String date;
        try{
            date = iterator.next().textValue();
        }
        catch (IllegalFieldValueException e){
            date = null;
        }
        Course course;
        if(courseModule.get(courseID).toCompletableFuture().join().isPresent()){
            course = courseModule.get(courseID).toCompletableFuture().join().get();
        }
        else {
            course = null;
        }
        Exam exam = new Exam(course, date);
        return examModule.update(id, exam).thenApplyAsync(data -> {
            if(exam.course == null){
                return status(404, "Course not found");
            }
            if(exam.date ==null){
                return status(400, "Invalid Date");
            }
            if (data.isPresent()){
                if(data.get()){
                    return status(201, "Updated successfully");
                }
            }
            return status(404, "Exam not found");
        }, executionContext.current());
    }
    public CompletionStage<Result> getExam(String id) {
        return examModule.get(id).thenApplyAsync(data -> {
            if (data.isPresent()) {
                Exam exam = data.get();
                return ok(Json.toJson(exam));
            } else {
                return status(404, "Exam not found");
            }
        }, executionContext.current());
    }

    public CompletionStage<Result> getAllExams() {
        return examModule.getAll().thenApplyAsync(data -> {
            return ok(Json.toJson(data));
        }, executionContext.current());
    }


}
