package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.joda.time.DateTime;
import play.data.validation.Constraints;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Course extends BaseModel {

    @Constraints.Required
    public String startTime;

    @Constraints.Required
    public String endTime;

    @ManyToOne
    public Subject subject;

    @ManyToMany
    public List<DictationHours> schedule;

    //@JsonIgnore
    @ManyToMany
    public List<Student> enrolled;

    public Course() {
        this.startTime = "";
        this.endTime = "";
        this.subject = new Subject();
        this.schedule = new ArrayList<>();
        this.enrolled = new ArrayList<>();
    }

    public Course(String startTime, String endTime, Subject subject, List<DictationHours> schedule) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.subject = subject;
        this.schedule = schedule;
        this.enrolled = new ArrayList<>();
    }
}
