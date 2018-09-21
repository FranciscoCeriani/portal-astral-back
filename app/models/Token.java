package models;
import models.BaseModel;

import javax.persistence.Entity;
import java.sql.Timestamp;

@Entity
public class Token extends BaseModel {

    private final String userId;

    private Timestamp validUntil;

    public Token(String userId, Timestamp validUntil) {
        this.userId = userId;
        this.validUntil = validUntil;
    }

    public Token(){
        this.userId = "";
    }

    public boolean isValid(Timestamp currentTime) {
        return currentTime.compareTo(this.validUntil) <= 0;
    }

    public String getId() {
        return this.userId;
    }
}
