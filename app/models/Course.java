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

    @ManyToMany
    public List<DictationHours> schedule;

    //@JsonIgnore
    @ManyToMany
    public List<Student> enrolled;

    public Course() {
        this.startDate = "";
        this.endDate = "";
        this.subject = new Subject();
        this.schedule = new ArrayList<>();
        this.enrolled = new ArrayList<>();
    }

    public Course(String startTime, String endTime, Subject subject, List<DictationHours> schedule) {
        this.startDate = startTime;
        this.endDate = endTime;
        this.subject = subject;
        this.schedule = schedule;
        this.enrolled = new ArrayList<>();
    }
}
