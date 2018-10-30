package repository;

import com.fasterxml.jackson.databind.JsonNode;
import io.ebean.DuplicateKeyException;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.Transaction;
import models.Course;
import models.Student;
import models.Subject;
import org.springframework.beans.BeanUtils;
import play.db.ebean.EbeanConfig;
import scala.util.Failure;
import scala.util.Success;
import scala.util.Try;

import javax.inject.Inject;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class CourseModule implements IModule<Course> {
    private final EbeanServer ebeanServer;
    private final DatabaseExecutionContext executionContext;

    @Inject
    public CourseModule(EbeanConfig ebeanConfig, DatabaseExecutionContext executionContext) {
        this.ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
        this.executionContext = executionContext;
    }

    @Override
    public CompletionStage<Try<String>> insert(Course entity) {
        return supplyAsync(() -> {
            try {
                if (entity.subject != null && entity.subject.id != null) {
                    Subject subject = ebeanServer.find(Subject.class).setId(entity.subject.id).findOne();
                    if (subject == null) {
                        return new Failure(new Exception("The subject is not registered"));
                    }
                } else return new Failure(new Exception("The subject is not registered"));
                if (entity.startDate.compareTo(entity.endDate) > 0) {
                    return new Failure(new Exception("Course must end after it began"));
                }
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                String today = df.format(new Date());
                if (entity.startDate.compareTo(today) < 0) {
                    return new Failure(new Exception("Start date has already passed"));
                }
                entity.id = UUID.randomUUID().toString();
                ebeanServer.insert(entity);
                return new Success(entity.id);
            } catch (DuplicateKeyException e) {
                return new Failure(new Exception("Email already exists"));
            }
        }, executionContext);
    }

    @Override
    public CompletionStage<Optional<Boolean>> update(String id, Course entity) {
        return supplyAsync(() -> {
            Transaction txn = ebeanServer.beginTransaction();
            Optional<Boolean> value = Optional.of(false);
            try {
                Course savedCourse = ebeanServer.find(Course.class).setId(id).findOne();
                if (savedCourse != null) {
                    entity.id = id;
                    BeanUtils.copyProperties(entity, savedCourse);
                    savedCourse.update();
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
                final Optional<Course> course = Optional.ofNullable(ebeanServer.find(Course.class, id));
                if (course.isPresent()){
                    ebeanServer.delete(course.get());
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
    public CompletionStage<Optional<Course>> get(String id) {
        return supplyAsync(() -> {
            Transaction txn = ebeanServer.beginTransaction();
            Optional<Course> value = Optional.empty();
            try {
                Course savedCourse = ebeanServer.find(Course.class).setId(id).findOne();
                if (savedCourse != null) {
                    value = Optional.of(savedCourse);
                }
            } finally {
                txn.end();
            }
            return value;
        }, executionContext);
    }

    @Override
    public CompletionStage<List<Course>> getAll() {
        return supplyAsync(() -> {
            Transaction txn = ebeanServer.beginTransaction();
            try {
                List<Course> allCourses = ebeanServer.find(Course.class).findList();
                if (allCourses != null) {
                    return allCourses;
                } else {
                    return new ArrayList<Course>();
                }
            } finally {
                txn.end();
            }
        }, executionContext);
    }

    /**
     * Updates the enrolled students in a course.
     *
     * @param studentIDIterator An iterator with the ids of all the students to be enrolled in the course.
     * @param courseID The course's id.
     * @return An optional with an integer (the amount of students enrolled) if all enrollments were successful.
     * An empty optional if any enrollment failed (if this happens, no enrollments are registered).
     */
    public CompletionStage<Optional<Integer>> addStudentsToCourse(Iterator<JsonNode> studentIDIterator, String courseID) {
        return supplyAsync(() -> {
            Transaction txn = ebeanServer.beginTransaction();
            Optional<Integer> value = Optional.empty();
            int enrollments = 0;
            try {
                Course course = ebeanServer.find(Course.class).setId(courseID).findOne();
                if (course != null) {
                    while (studentIDIterator.hasNext()) {
                        Student student = ebeanServer.find(Student.class)
                                .setId(studentIDIterator.next().textValue())
                                .findOne();
                        if (student != null) {
                            if (!course.enrolled.contains(student)){
                                course.enrolled.add(student);
                                enrollments++;
                            }
                        } else return value;
                    }
                    course.update();
                    txn.commit();
                    value = Optional.of(enrollments);
                }
            } finally {
                txn.end();
            }
            return value;
        }, executionContext);
    }

    /**
     * Updates the enrolled students in a course.
     *
     * @param studentIDIterator An iterator with the ids of all the students to be removed from the course.
     * @param courseID The course's id.
     * @return An optional with an integer (the amount of students removed) if all removals were successful.
     * An empty optional if any removal failed (if this happens, no removals are registered).
     */
    public CompletionStage<Optional<Integer>> removeStudentsFromCourse(Iterator<JsonNode> studentIDIterator, String courseID) {
        return supplyAsync(() -> {
            Transaction txn = ebeanServer.beginTransaction();
            Optional<Integer> value = Optional.empty();
            int removals = 0;
            try {
                Course course = ebeanServer.find(Course.class).setId(courseID).findOne();
                if (course != null) {
                    while (studentIDIterator.hasNext()) {
                        Student student = ebeanServer.find(Student.class)
                                .setId(studentIDIterator.next().textValue())
                                .findOne();
                        if (student != null) {
                            if (course.enrolled.contains(student)){
                                course.enrolled.remove(student);
                                removals++;
                            }
                        } else return value;
                    }
                    course.update();
                    txn.commit();
                    value = Optional.of(removals);
                }
            } finally {
                txn.end();
            }
            return value;
        }, executionContext);
    }

    //    empty the list of students
    public CompletionStage<Optional<Boolean>> emptyListEnrolled(String id) {
        return supplyAsync(() -> {
            Transaction txn = ebeanServer.beginTransaction();
            Optional<Boolean> value = Optional.of(false);
            try {
                Course savedCourse = ebeanServer.find(Course.class).setId(id).findOne();
                if (savedCourse != null) {
                    savedCourse.enrolled = new ArrayList<>();
                    savedCourse.update();
                    txn.commit();
                    value = Optional.of(true);
                }
            } finally {
                txn.end();
            }
            return value;
        }, executionContext);
    }
}
