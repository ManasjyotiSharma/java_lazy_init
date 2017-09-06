import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/*
 * In this example the initializer object does not hold the singleton instance of SampleSingletonA
 * rather thru the initialization function/lambda-expression initializes the INSTANCE variable of 
 * SampleSingletonA. Note that in this case INSTANCE must be volatile for publishing to all threads.
 * 
 * This example I'm giving as a possible usage, but I prefer the SampleSingletonB approach where
 * the initializer itself holds the needed singleton instance.
 */
public class SingletonSampleA {
  private static volatile SingletonSampleA INSTANCE;
  
  // The initializer does not hold any meaningful value object, rather holds a Void/null.
  // The lambda expression as a side effect initializes INSTANCE.
  private static final OneTimeExecutor<File, Void> initializer = new OneTimeExecutor<>(
    (File configFile) -> {
      Properties properties = new Properties();
      try (FileInputStream fis = new FileInputStream(configFile)) { 
        properties.load(fis);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      INSTANCE = new SingletonSampleA(properties);
      return null;
    }
  ); 

  // Some dummy data needed by SampleSingletonA
  private final Properties properties;
  
  private SingletonSampleA(Properties properties) {
    this.properties = properties;
  }
  
  /*
   * The singleton instance (of SampleSingletonA) can not be created in a static block because its 
   * initialization needs configFile path which is available only at runtime!
   */
  public static SingletonSampleA getInstance(File configFile) throws Exception {
    initializer.execute(configFile);
    return INSTANCE;
  }
    
  /*
   * Overloaded helper to get the singleton instance from another context where 
   * configFile is not available, provided someone already called the base version 
   * getInstance(File configFile) to initialize the singleton.
   */
  public static SingletonSampleA getInstance() throws Exception {
    checkInit();
    return INSTANCE;
  }
  
  public void doSomething() {
    /*
     * Application logic. 
     * No need to call checkInit here as doSomething is a non-static method so the singleton instance must have been created already.
     */
  }

  private static void checkInit() {
    if (!initializer.executed()) {
      throw new RuntimeException("SampleSingletonA not yet initialized");
    }    
  }  
}
