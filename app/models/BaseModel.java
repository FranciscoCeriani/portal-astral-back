package models;

import io.ebean.Model;
import io.ebean.annotation.SoftDelete;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class BaseModel extends Model {
   @Id
   public String id;

   @SoftDelete
   boolean deleted;
}
