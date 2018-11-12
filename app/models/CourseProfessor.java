package models;

import play.data.validation.Constraints;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class CourseProfessor extends BaseModel {

    @ManyToOne
    public Professor professor;

    @ManyToOne
    public Course course;

    public CourseProfessor() {
        professor = new Professor();
        course = new Course();
    }

    public CourseProfessor(Professor professor, Course course) {
        this.professor = professor;
        this.course = course;
    }
}
