import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

// For the sake of this example, assume that creating a PrintWriter is a costly operation and we'd want to lazily initialize it.
public class NonSingletonSampleA {
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
  
  private final File file;
  
  public NonSingletonSampleA(File file) {
    this.file = file;
  }
  
  public void doSomething() throws Exception {
    // Create one-and-only-one instance of PrintWriter only when someone calls doSomething().  
    PrintWriter pw = initializer.execute(file);
    
    // Application logic goes here, say write something to the file using the PrintWriter.
  }

  public void close() throws IOException {
    // This implementation assumes that close is idempotent. If not then look at NonSingletonSampleB.
    if (initializer.executed() && null != initializer.value()) {
      initializer.value().close();
    }
  }

}
