package models;

import play.data.validation.Constraints;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class ExamInscription extends BaseModel {

    @ManyToOne
    public Student student;

    @ManyToOne
    public Exam exam;

    @Constraints.Required
    public Integer result;

    public ExamInscription() {
        student = new Student();
        exam = new Exam();
        result = 0;
    }

    public ExamInscription(Student student, Exam exam) {
        this.student = student;
        this.exam = exam;
        this.result = 0;
    }
}
