package models;

import io.ebean.annotation.SoftDelete;
import play.data.validation.Constraints;

import javax.persistence.*;
import javax.validation.Constraint;

@MappedSuperclass
public abstract class  User extends BaseModel{

    @Constraints.Required
    public String name;

    @Constraints.Required
    public String lastName;

    @Constraints.Required
    public String file;

    @Constraints.Required
    @Column(unique = true)
    public String email;

    @Constraints.Required
    public String password;
}
