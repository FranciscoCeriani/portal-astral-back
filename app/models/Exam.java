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
    public String date;

    public Exam() {
        course= new Course();
        date = "";
    }

    public Exam(Course course, String date) {
        this.course= course;
        this.date = date;
    }

}
