//package startUp;
//
//import javax.inject.*;
//
//import models.Admin;
//import repository.AdminModule;
//
//// This creates an `startUpModules.ApplicationStart` object once at start-up.
//@Singleton
//public class ApplicationStart {
//
//
//
//    @Inject
//    public ApplicationStart(AdminModule adminModule) {
//        Admin admin = new Admin();
//        admin.name = "Jhon";
//        admin.lastName = "Doe";
//        admin.email = "john-doe@gmail.com";
//        admin.file = "123321";
//        admin.password = "123";
//        adminModule.insert(admin);
//    }
//}