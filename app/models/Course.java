package models;

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
    public DateTime startTime;

    @Constraints.Required
    public DateTime endTime;

    @ManyToOne
    public Subject subject;

    @ManyToMany
    public List<DictationHours> schedule;

    public Course() {
        this.startTime = new DateTime();
        this.endTime = new DateTime();
        this.subject = new Subject();
        this.schedule = new ArrayList<>();
    }

    public Course(DateTime startTime, DateTime endTime, Subject subject, List<DictationHours> schedule) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.subject = subject;
        this.schedule = schedule;
    }
}
