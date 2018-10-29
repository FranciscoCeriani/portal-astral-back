package models;

import io.ebean.annotation.DbJson;
import play.data.validation.Constraints;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.validation.Constraint;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Subject extends BaseModel {

    @Constraints.Required
    public String subjectName;

    @Constraints.Required
    public int careerYear;

    @DbJson
    public ArrayList<String> requiredSubjects;


    public Subject() {
        subjectName = "";
        careerYear = 0;
        requiredSubjects = new ArrayList<>();
    }

    public Subject(String subjectName, int careerYear, ArrayList<String> requiredSubjects) {
        this.subjectName = subjectName;
        this.careerYear = careerYear;
        this.requiredSubjects = requiredSubjects;
    }

    public ArrayList<String> getRequiredSubjects() {
        return requiredSubjects;
    }

    public void addRequiredSubject(String rID) {
        requiredSubjects.add(rID);
    }

    public boolean deleteRequiredSubject(String rID) {
        return requiredSubjects.remove(rID);
    }
}