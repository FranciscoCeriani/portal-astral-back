package models;

import javax.persistence.Entity;
import java.sql.Timestamp;

@Entity
public class Token extends BaseModel {

    private final String userId;

    private Timestamp validUntil;

    private int lifespan;

    /**
     * Creates a new Token that will be valid for lifetime minutes.
     *
     * Once a Token has been created, its lifespan value cannot be changed.
     * After the method reset() is called, the token will last for its original lifespan.
     *
     * @param userId The id of the user.
     * @param lifespan The life span of the token, in minutes.
     */
    public Token(String userId, int lifespan) {
        this.userId = userId;
        this.validUntil = new Timestamp(System.currentTimeMillis() + lifespan * 60000);
        this.lifespan = lifespan;
    }

    public Token(){
        this.userId = "";
    }

    public boolean isValid() {
        return new Timestamp(System.currentTimeMillis()).compareTo(this.validUntil) <= 0;
    }

    public String getUserId() {
        return this.userId;
    }

    /**
     * Resets the token so that it lasts for the full duration of its lifespan.
     */
    public void reset() {
        this.validUntil = new Timestamp(System.currentTimeMillis() + this.lifespan * 60000);
    }
}
