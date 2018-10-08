package models;

import play.data.validation.Constraints;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.swing.text.html.Option;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Entity
public class Student extends User{

    @Constraints.Required
    public String birthday;

    @Constraints.Required
    public String identificationType;

    @Constraints.Required
    public String identification;

    @Constraints.Required
    public String address;

    public Student(){
        name = "";
        lastName = "";
        file = "";
        email = "";
        password = "";
        birthday = "";
        identificationType = "";
        identification = "";
        address = "";
    }

    public Student(String name, String lastName, String file, String email, String password, String birthday, String identificationType, String identification, String address) {
        this.name = name;
        this.lastName = lastName;
        this.file = file;
        this.email = email;
        this.password = password;
        this.birthday = birthday;
        this.identificationType = identificationType;
        this.identification = identification;
        this.address = address;
    }
}
