package models;

import io.ebean.annotation.DbJson;
import play.data.validation.Constraints;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.validation.Constraint;
import java.util.ArrayList;

@Entity
public class Career extends BaseModel {

    @Constraints.Required
    public String careerName;

    @DbJson
    public ArrayList<String> careerSubjects;

    @ManyToMany
    public ArrayList<Student> students;

    public Career() {
        careerName = "";
        careerSubjects = new ArrayList<>();
        students = new ArrayList<>();
    }

    public Career(String careerName, ArrayList<String> careerSubjects, ArrayList<Student> students) {
        this.careerName = careerName;
        this.careerSubjects = careerSubjects;
        this.students = students;
    }

    public ArrayList<String> getCareerSubjects() {
        return careerSubjects;
    }

    public void addCareerSubject(String cID) {
        careerSubjects.add(cID);
    }

    public boolean deleteCareerSubject(String cID) {
        return careerSubjects.remove(cID);
    }
}