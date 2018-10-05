package models;

import org.joda.time.DateTime;
import play.data.validation.Constraints;

import javax.persistence.Entity;
import javax.validation.Constraint;

@Entity
public class DictationHours extends BaseModel {

    @Constraints.Required
    public String day;

    @Constraints.Required
    public DateTime startTime;

    @Constraints.Required
    public DateTime endTime;

    public DictationHours() {
        day = "";
        startTime = new DateTime();
        endTime = new DateTime();
    }

    public DictationHours(String day, DateTime startTime, DateTime endTime) {
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
