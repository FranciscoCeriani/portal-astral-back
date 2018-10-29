package models;

import play.data.validation.Constraints;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.Optional;

@Entity
public class ExamInscription extends BaseModel {

    @ManyToOne
    public Student student;

    @ManyToOne
    public Exam exam;

    @Constraints.Required
    public Optional<Integer> result;

    public ExamInscription() {
        student = new Student();
        exam = new Exam();
        result = Optional.empty();
    }

    public ExamInscription(Student student, Exam exam) {
        this.student = student;
        this.exam = exam;
        this.result = Optional.empty();
    }
}
