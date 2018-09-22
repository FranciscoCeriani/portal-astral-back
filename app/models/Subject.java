package models;

import play.data.validation.Constraints;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.validation.Constraint;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Subject extends BaseModel{

    @Constraints.Required
    public String subjectName;

    @Constraints.Required
    public int careerYear;

    @Constraints.Required
    public ArrayList<String> requiredSubjects;

    @ManyToMany
    public List<Student> students;

    public Subject(){
        subjectName = "";
        careerYear = 0;
        requiredSubjects = new ArrayList<>();
        students = new ArrayList<>();
    }

    public Subject(String subjectName, int careerYear, ArrayList<String> requiredSubjects, ArrayList<Student> students){
        this.subjectName = subjectName;
        this.careerYear = careerYear;
        this.requiredSubjects = requiredSubjects;
        this.students = students;
    }

    public ArrayList<String> getRequiredSubjects() {
        return requiredSubjects;
    }

    public void addRequiredSubject(String rID) {
        requiredSubjects.add(rID);
    }
}