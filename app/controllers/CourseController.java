package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.Course;
import models.DictationHours;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import repository.CourseModule;
import scala.util.Failure;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public class CourseController extends Controller {

    private final HttpExecutionContext executionContext;
    private final CourseModule courseModule;

    @Inject
    public CourseController(HttpExecutionContext executionContext, CourseModule courseModule) {
        this.executionContext = executionContext;
        this.courseModule = courseModule;
    }

    public CompletionStage<Result> saveCourse() {
        JsonNode json = request().body().asJson();
        Course realCourse = Json.fromJson(json, Course.class);

        JsonNode arrays = json.findValue("schedules");
        Iterator<JsonNode> iterator = arrays.iterator();
        List<DictationHours> schedule = new ArrayList<>();
        while (iterator.hasNext()) {
            String dateTime = iterator.next().asText();
            DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");

            DateTime dT1 = formatter.parseDateTime(dateTime);
            if (iterator.hasNext()) {
                dateTime = iterator.next().asText();
                DateTime dT2 = formatter.parseDateTime(dateTime);
                DictationHours dictationHours = new DictationHours(dT1.dayOfWeek().getAsString(), dT1, dT2);
                courseModule.insertDictationHours(dictationHours);
                schedule.add(dictationHours);
            } else {
                return (CompletionStage<Result>) status(409, "Error Schedule");
            }
        }
        realCourse.schedule = schedule;
        return courseModule.insert(realCourse).thenApplyAsync(data -> {
            if (data.isSuccess()) {
                return status(201, data.get());
            } else {
                return status(400, ((Failure) data).exception().getMessage());
            }
        }, executionContext.current());
    }
//    DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
//    DateTime dt = formatter.parseDateTime(string);

    public CompletionStage<Result> updateCourse(String id) {
        JsonNode jsonNode = request().body().asJson();
        Course course = Json.fromJson(jsonNode, Course.class);
        return courseModule.update(id, course).thenApplyAsync(data -> {
            if (data.isPresent()) {
                if (data.get()) {
                    return ok("Course updated");
                }
            }
            return status(404, "Course to be updated not found");
        }, executionContext.current());
    }

    public CompletionStage<Result> deleteCourse(String id) {

        return courseModule.delete(id).thenApplyAsync(data -> {
            // This is the HTTP rendering thread context
            if (data.isPresent()) {
                if (data.get()) {
                    return ok("Course deleted");
                } else {
                    return status(404, "Resource not found");
                }
            } else {
                return status(404, "Resource not found");
            }
        }, executionContext.current());
    }

    public CompletionStage<Result> getCourse(String id) {
        return courseModule.get(id).thenApplyAsync(data -> {
            // This is the HTTP rendering thread context
            if(data.isPresent()){
                Course course = data.get();
                return ok(Json.toJson(course));
            }else{
                return status(404, "Resource not found");
            }
        }, executionContext.current());
    }

    public CompletionStage<Result> getAllCourses() {
        return courseModule.getAll().thenApplyAsync(data -> {
            // This is the HTTP rendering thread context
            return ok(Json.toJson(data));
        }, executionContext.current());
    }

    /**
     * Enrolls students to a course.
     *
     * The request's body must be a Json whose attributes are the ids of the students being enrolled.
     * The names of the Json attributes are irrelevant, although its values must be valid student ids.
     *
     * Json Examples:
     *
     * {
     *     "id": "80eaad59-82f3-4002-a072-ed2f72f21a96"
     * }
     *
     * {
     *     "id1": "80eaad59-82f3-4002-a072-ed2f72f21a96",
     *     "id2": "234bdb14-4df7-4095-b09d-40c5b10717c6",
     *     "id3": "30019b49-4aa8-4123-92dc-5235db9c91d2"
     * }
     *
     * {
     *     "second_id": "30019b49-4aa8-4123-92dc-5235db9c91d2",
     *     "first_id": "234bdb14-4df7-4095-b09d-40c5b10717c6"
     * }
     *
     * @param id The course's id.
     * @return
     *
     * 200 if all the students were enrolled correctly (if a student was already enrolled in the course,
     * his enrollment is considered successful).
     * In this case, all enrollments are completed successfully.
     *
     * 400 if one or more students could not be enrolled in the course (either because the student id
     * was invalid or because no course with the provided id exists).
     * If any enrollment fails, no enrollments are completed.
     */
    public CompletionStage<Result> enrollStudents(String id) {
        JsonNode json = request().body().asJson();
        Iterator<JsonNode> studentIdIterator = json.elements();
        return courseModule.addStudentsToCourse(studentIdIterator, id).thenApplyAsync(data -> {
            if (data.isPresent()) {
                if (data.get() == 1) {
                    return status(200, "Student enrolled successfully");
                }
                if (data.get() == 0) {
                    return status(200, "No students enrolled");
                }
                return status(200, data.get() + " students enrolled successfully");
            } else {
                return status(400, "Resource not found");
            }
        }, executionContext.current());
    }

    /**
     * Removes students from a course.
     *
     * The request's body must be a Json whose attributes are the ids of the students being enrolled.
     * The names of the Json attributes are irrelevant, although its values must be valid student ids.
     *
     * Json Examples:
     *
     * {
     *     "id": "80eaad59-82f3-4002-a072-ed2f72f21a96"
     * }
     *
     * {
     *     "id1": "80eaad59-82f3-4002-a072-ed2f72f21a96",
     *     "id2": "234bdb14-4df7-4095-b09d-40c5b10717c6",
     *     "id3": "30019b49-4aa8-4123-92dc-5235db9c91d2"
     * }
     *
     * {
     *     "second_id": "30019b49-4aa8-4123-92dc-5235db9c91d2",
     *     "first_id": "234bdb14-4df7-4095-b09d-40c5b10717c6"
     * }
     *
     * @param id The course's id.
     * @return
     *
     * 200 if all the students were removed correctly (if a student wasn't enrolled in the course,
     * his removal is considered successful).
     * In this case, all removals are completed successfully.
     *
     * 400 if one or more students could not be removed from the course (either because the student id
     * was invalid or because no course with the provided id exists).
     * If any removal fails, no removals are completed.
     */
    public CompletionStage<Result> removeStudents(String id) {
        JsonNode json = request().body().asJson();
        Iterator<JsonNode> studentIdIterator = json.elements();
        return courseModule.removeStudentsFromCourse(studentIdIterator, id).thenApplyAsync(data -> {
            if (data.isPresent()) {
                if (data.get() == 1) {
                    return status(200, "Student removed successfully");
                }
                if (data.get() == 0) {
                    return status(200, "No students removed");
                }
                return status(200, data.get() + " students removed successfully");
            } else {
                return status(400, "Resource not found");
            }
        }, executionContext.current());
    }

    public CompletionStage<Result> emptyListEnrolled(String id) {
        return courseModule.emptyListEnrolled(id).thenApplyAsync(data -> {
            if (data.isPresent()) {
                if (data.get()) {
                    return ok("Enrolled list updated");
                }
            }
            return status(404, "Course to be updated not found");
        }, executionContext.current());
    }
}
