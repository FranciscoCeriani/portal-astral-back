package repository;

import com.fasterxml.jackson.databind.JsonNode;
import io.ebean.DuplicateKeyException;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.Transaction;
import models.Course;
import models.Course;
import models.CourseProfessor;
import models.Professor;
import org.springframework.beans.BeanUtils;
import play.db.ebean.EbeanConfig;
import scala.util.Failure;
import scala.util.Success;
import scala.util.Try;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class CourseProfessorModule {

    private final EbeanServer ebeanServer;
    private final DatabaseExecutionContext executionContext;

    @Inject
    public CourseProfessorModule(EbeanConfig ebeanConfig, DatabaseExecutionContext executionContext) {
        this.ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
        this.executionContext = executionContext;
    }

    public CompletionStage<Optional<Boolean>> update(String id, String professorId) {
        return supplyAsync(() -> {
            Transaction txn = ebeanServer.beginTransaction();
            Optional<Boolean> value = Optional.of(false);
            try {
                CourseProfessor savedCourseI = ebeanServer.find(CourseProfessor.class).where().eq("course_id", id).findOne();
                Professor professor = ebeanServer.find(Professor.class).where().eq("id", professorId).findOne();
                if (savedCourseI != null) {
                    savedCourseI.professor = professor;
                    savedCourseI.update();
                    txn.commit();
                    value = Optional.of(true);
                }
            } finally {
                txn.end();
            }
            return value;
        }, executionContext);
    }

    public CompletionStage<Optional<Boolean>> delete(String id) {
        return supplyAsync(() -> {
            try {
                final Optional<CourseProfessor> courseProfessor = Optional.ofNullable(ebeanServer.find(CourseProfessor.class, id));
                if (courseProfessor.isPresent()) {
                    ebeanServer.delete(courseProfessor.get());
                    return Optional.of(true);
                } else {
                    return Optional.of(false);
                }
            } catch (Exception e) {
                return Optional.of(false);
            }
        }, executionContext);
    }


    public CompletionStage<Try<String>> insert(CourseProfessor entity) {
        return supplyAsync(() -> {
            try {
                entity.id = UUID.randomUUID().toString();
                ebeanServer.insert(entity);
                return new Success(entity.id);
            } catch (DuplicateKeyException e) {
                return new Failure(new Exception("CourseProfessor already exists"));
            }
        }, executionContext);
    }

    public CompletionStage<Optional<CourseProfessor>> get(String id) {
        return supplyAsync(() -> {
            Transaction txn = ebeanServer.beginTransaction();
            Optional<CourseProfessor> value = Optional.empty();
            try {
                CourseProfessor savedCourseI = ebeanServer.find(CourseProfessor.class).setId(id).findOne();
                if (savedCourseI != null) {
                    value = Optional.of(savedCourseI);
                }
            } finally {
                txn.end();
            }
            return value;
        }, executionContext);
    }

    public CompletionStage<List<CourseProfessor>> getAll() {
        return supplyAsync(() -> {
            Transaction txn = ebeanServer.beginTransaction();
            try {
                List<CourseProfessor> allCourses = ebeanServer.find(CourseProfessor.class).findList();
                if (allCourses != null) {
                    return allCourses;
                } else {
                    return new ArrayList<>();
                }
            } finally {
                txn.end();
            }
        }, executionContext);
    }

    //    Devuelve todos los CourseProfessor pertenecientes a Professor
    public CompletionStage<List<Course>> getAllCourseProfessor(String idProfessor) {
        return supplyAsync(() -> {
            Transaction txn = ebeanServer.beginTransaction();
            List<Course> result = new ArrayList<>();
            try {
                Professor professor = ebeanServer.find(Professor.class).setId(idProfessor).findOne();
                if (professor != null) {
                    result = getCoursesIns(professor);
                }
            } finally {
                txn.end();
            }
            return result;
        }, executionContext);
    }

    //    Devuelve todos los CourseProfessor pertenecientes a Course
    public CompletionStage<List<Professor>> getAllCourse(String idCourse) {
        return supplyAsync(() -> {
            Transaction txn = ebeanServer.beginTransaction();
            List<Professor> result = new ArrayList<>();
            try {
                Course course = ebeanServer.find(Course.class).setId(idCourse).findOne();
                if (course != null) {
                    result = getCoursesIns(course);
                }
            } finally {
                txn.end();
            }
            return result;
        }, executionContext);
    }

    //    Devuelve todos los CourseProfessor pertenecientes a Professor
    private List<Course> getCoursesIns(Professor professor) {
        Transaction txn = ebeanServer.beginTransaction();
        try {
            List<CourseProfessor> allCourses = ebeanServer.find(CourseProfessor.class).findList();
            if (allCourses != null) {
                List<Course> result = new ArrayList<>();
                for (CourseProfessor i : allCourses) {
                    if (i.professor.equals(professor)) {
                        result.add(i.course);
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

    //    Devuelve todos los CourseProfessor pertenecientes a Course
    private List<Professor> getCoursesIns(Course course) {
        Transaction txn = ebeanServer.beginTransaction();
        try {
            List<CourseProfessor> allCourses = ebeanServer.find(CourseProfessor.class).findList();
            if (allCourses != null) {
                List<Professor> result = new ArrayList<>();
                for (CourseProfessor i : allCourses) {
                    if (i.course.equals(course)) {
                        result.add(i.professor);
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

    /**
     * Enrolls professors into an course by creating CourseProfessors.
     *
     * @param professorIdsIterator An iterator with the ids of all the professors to be enrolled into the course.
     * @param courseId The course's id.
     * @return An optional with a list of Strings (ids from all created CourseInscrptions. If the CourseProfessor for a
     * professor for this course already existed, the id for that CourseProfessor is also added to the list).
     * An empty optional if every enrollment failed (if this happens, no CourseProfessors are created).
     */
    public CompletionStage<Optional<List<String>>> enrollProfessorsToCourse(Iterator<JsonNode> professorIdsIterator, String courseId) {
        return supplyAsync(() -> {
            Optional<List<String>> result = Optional.empty();
            List<String> inscriptionIds = new ArrayList<>();
            Course course = ebeanServer.find(Course.class).setId(courseId).findOne();
            Professor professor;
            CourseProfessor courseProfessor;
            if (course != null) {
                while (professorIdsIterator.hasNext()) {
                    professor = ebeanServer.find(Professor.class).setId(professorIdsIterator.next().textValue()).findOne();
                    if (professor != null) {
                        if (!checkIfInscriptionAlreadyExists(professor, course)){
                            courseProfessor = new CourseProfessor(professor, course);
                            courseProfessor.id = UUID.randomUUID().toString();
                            ebeanServer.insert(courseProfessor);
                            inscriptionIds.add(courseProfessor.id);
                        } else {
                            courseProfessor = ebeanServer.find(CourseProfessor.class)
                                    .where().eq("professor", professor).eq("course", course)
                                    .findOne();
                            inscriptionIds.add(courseProfessor.id);
                        }
                    }
                }
            } else {
                return result;
            }
            result = Optional.of(inscriptionIds);
            return result;
        }, executionContext);
    }

    /**
     * Unenrolls a professor from an course by deleting the CourseProfessor.
     *
     * @param professorId Id from the professor to unenroll.
     * @param courseId The course's id.
     * @return An optional with a Boolean (True if the unenrollment was successful, False if not).
     */
    public CompletionStage<Optional<Boolean>> unenrollProfessorFromCourse(String professorId, String courseId) {
        return supplyAsync(() -> {
            Professor professor = ebeanServer.find(Professor.class).setId(professorId).findOne();
            Course course = ebeanServer.find(Course.class).setId(courseId).findOne();
            if (professor != null && course != null) {
                CourseProfessor courseProfessor = ebeanServer.find(CourseProfessor.class)
                        .where().eq("professor", professor).eq("course", course)
                        .findOne();
                if (courseProfessor != null) {
                    ebeanServer.delete(courseProfessor);
                    return Optional.of(true);
                }
            }
            return Optional.of(false);
        }, executionContext);
    }

    /**
     * Checks if the professor is already enrolled into the course.
     *
     * @param professor Professor to check.
     * @param course Course to check.
     * @return A boolean. (True if the professor is already enrolled to that course, False if not).
     */
    private boolean checkIfInscriptionAlreadyExists(Professor professor, Course course) {
        CourseProfessor courseProfessor = ebeanServer.find(CourseProfessor.class)
                .where().eq("professor", professor).eq("course", course)
                .findOne();
        return courseProfessor != null;
    }
}
