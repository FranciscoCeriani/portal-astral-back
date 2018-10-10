package models;

import org.joda.time.DateTime;
import play.data.validation.Constraints;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class Exam extends BaseModel {

    @ManyToOne
    public String subjectId;

    @Constraints.Required
    public DateTime date;

    public Exam() {
        subjectId= "";
        date = new DateTime();
    }

    public Exam(String subjectId, DateTime date) {
        this.subjectId= subjectId;
        this.date = date;
    }

}
