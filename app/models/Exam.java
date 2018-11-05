package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.joda.time.DateTime;
import play.data.validation.Constraints;

import javax.persistence.*;
import java.util.List;

@Entity
public class Exam extends BaseModel {

    @ManyToOne
    public Course course;

    @Constraints.Required
    public String date;

    @JsonIgnore
    @OneToMany(cascade=CascadeType.ALL)
    public List<ExamInscription> inscriptions;

    public Exam() {
        course= new Course();
        date = "";
    }

    public Exam(Course course, String date) {
        this.course= course;
        this.date = date;
    }

}
