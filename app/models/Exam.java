package models;

import org.joda.time.DateTime;
import play.data.validation.Constraints;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class Exam extends BaseModel {

    @ManyToOne
    public Course course;

    @Constraints.Required
    public DateTime date;

    public Exam() {
        course= new Course();
        date = new DateTime();
    }

    public Exam(Course course, DateTime date) {
        this.course= course;
        this.date = date;
    }

}
