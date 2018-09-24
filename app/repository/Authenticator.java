package repository;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.SqlQuery;
import io.ebean.Transaction;
import models.Admin;
import models.Professor;
import models.Student;
import models.User;
import play.db.ebean.EbeanConfig;

import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class Authenticator {

    private final EbeanServer ebeanServer;
    private final DatabaseExecutionContext executionContext;

    @Inject
    public Authenticator(EbeanConfig ebeanConfig, DatabaseExecutionContext executionContext) {
        this.ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
        this.executionContext = executionContext;
    }

    public CompletionStage<Optional<User>> authenticateUser(String email, String password){
        return supplyAsync(() -> {
            Transaction txn = ebeanServer.beginTransaction();
            Optional<User> user = Optional.empty();
            try{
                User student = ebeanServer.find(Student.class).where().eq("email" , email).findOne();
                if(student != null){
                    if(student.password.equals(password)){
                        user = Optional.of(student);
                    }
                }
                User professor = ebeanServer.find(Professor.class).where().eq("email" , email).findOne();
                if(professor != null){
                    if(professor.password.equals(password)){
                        user = Optional.of(professor);
                    }
                }
                User admin = ebeanServer.find(Admin.class).where().eq("email" , email).findOne();
                if(admin != null){
                    if(admin.password.equals(password)){
                        user = Optional.of(admin);
                    }
                }

            }
            finally{
                txn.end();
            }
            return user;

        }, executionContext);

    }



}