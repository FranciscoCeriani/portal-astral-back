package models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.Entity;

@Entity
public class Professor extends User {



    @JsonCreator
    public Professor(@JsonProperty("name") String name,
          @JsonProperty("lastName") String lastName,
          @JsonProperty("file") String file,
          @JsonProperty("email") String email,
          @JsonProperty("password") String password){
        super.name =name;
        super.lastName= lastName;
        super.file = file;
        super.email = email;
        super.password = password;

    }

}
