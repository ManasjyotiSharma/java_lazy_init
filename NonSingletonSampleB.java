import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/*
 * This example is same as NonSingletonSampleA except that it assumes that close is non-idempotent.
 * In other words, just like initialization, the de-initialization should also happen once and only once.
 */
public class NonSingletonSampleB {
  private final OneTimeExecutor<File, PrintWriter> initializer = new OneTimeExecutor<>(
    (File configFile) -> {
      try { 
        FileOutputStream fos = new FileOutputStream(configFile);
        OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
        BufferedWriter bw = new BufferedWriter(osw);
        PrintWriter pw = new PrintWriter(bw);
        return pw;
      } catch (IOException e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
    }
  );  

  private final OneTimeExecutor<Void, Void> deinitializer = new OneTimeExecutor<>(
    (Void v) -> {
      if (initializer.executed() && null != initializer.value()) {
        initializer.value().close();
      }
      return null;
    }
  );  
  
  private final File file;
  
  public NonSingletonSampleB(File file) {
    this.file = file;
  }
  
  public void doSomething() throws Exception {
    // Create one-and-only-one instance of PrintWriter only when someone calls doSomething().  
    PrintWriter pw = initializer.execute(file);
    
    // Application logic goes here, say write something to the file using the PrintWriter.
  }

  public void close() throws Exception {
    // non-idempotent close, the de-initialization lambda is invoked only once. 
    deinitializer.execute(null);
  }

}
