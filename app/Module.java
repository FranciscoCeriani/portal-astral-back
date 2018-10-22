//import com.google.inject.AbstractModule;
//import com.typesafe.config.Config;
//import play.Environment;
//import repository.AdminModule;
//import startUp.ApplicationStart;
//
//public class Module extends AbstractModule {
//
//
//    private final Environment environment;
//    private final Config config;
//
//    public Module(Environment environment, Config config) {
//        this.environment = environment;
//        this.config = config;
//    }
//
//    protected void configure() {
//        bind(ApplicationStart.class);
//    }
//}