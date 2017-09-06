import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * When execute is called, it is guaranteed that the input function will be applied exactly once. 
 * Further it's also guaranteed that execute will return only when the input function was applied
 * by the calling thread or some other thread OR if the calling thread is interrupted.
 */
public class OneTimeExecutor<T, R> {  
  private final Function<T, R> function;
  private final AtomicBoolean preGuard;
  private final CountDownLatch postGuard;
  private final AtomicReference<R> value;

  public OneTimeExecutor(Function<T, R> function) {
    Objects.requireNonNull(function, "function cannot be null");
    this.function = function;
    this.preGuard = new AtomicBoolean(false);
    this.postGuard = new CountDownLatch(1);
    this.value = new AtomicReference<R>();
  }
  
  public R execute(T input) throws InterruptedException {
    if (preGuard.compareAndSet(false, true)) {
      try {
        value.set(function.apply(input));
      } finally {
        postGuard.countDown();
      }
    } else if (postGuard.getCount() != 0) {
      postGuard.await();
    }
    return value();
  }
  
  public boolean executed() {
    return (preGuard.get() && postGuard.getCount() == 0);
  }
  
  public R value() {
    return value.get();
  }

}
