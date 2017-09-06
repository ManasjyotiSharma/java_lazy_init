import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/*
 * In this example the initializer object itself holds the needed singleton instance of SampleSingletonB.
 */
public class SingletonSampleB {  
  private static final OneTimeExecutor<File, SingletonSampleB> initializer = new OneTimeExecutor<>(
    (File configFile) -> {
      Properties properties = new Properties();
      try (FileInputStream fis = new FileInputStream(configFile)) { 
        properties.load(fis);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      return new SingletonSampleB(properties);
    }
  ); 
  
  // Some dummy data needed by SampleSingletonB
  private final Properties properties;
  
  private SingletonSampleB(Properties properties) {
    this.properties = properties;
  }
  
  /*
   * The singleton instance (of SampleSingletonB) can not be created in a static block because its 
   * initialization needs configFile path which is available only at runtime!
   */
  public static SingletonSampleB getInstance(File configFile) throws Exception {
    return initializer.execute(configFile);
  }
  
  /*
   * Overloaded helper to get the singleton instance from another context where 
   * configFile is not available, provided someone already called the base version 
   * getInstance(File configFile) to initialize the singleton.
   */
  public static SingletonSampleB getInstance() throws Exception {
    checkInit();
    return initializer.value();
  }
  
  
  public void doSomething() {
    /*
     * Application logic. 
     * No need to call checkInit here as doSomething is a non-static method so the singleton instance must have been created already.
     */
  }

  private static void checkInit() {
    if (!initializer.executed()) {
      throw new RuntimeException("SampleSingletonB not yet initialized");
    }    
  }  
}
