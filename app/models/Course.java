package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.joda.time.DateTime;
import play.data.validation.Constraints;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.xml.bind.DatatypeConverter;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Course extends BaseModel {

    @Constraints.Required
    public String startDate;

    @Constraints.Required
    public String endDate;

    @ManyToOne
    public Subject subject;

    @JsonIgnore
    @ManyToMany
    public List<Student> enrolled;

    public Course() {
        this.startDate = "";
        this.endDate = "";
        this.subject = new Subject();
        this.enrolled = new ArrayList<>();
    }

    public Course(String startTime, String endTime, Subject subject) {
        this.startDate = startTime;
        this.endDate = endTime;
        this.subject = subject;
        this.enrolled = new ArrayList<>();
    }
}
