package rust;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public interface ui {
 public final static ExecutorService pool=//Executors.newWorkStealingPool();
 Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
 //使用队列堵塞，必须先进先出
 public void end(Throwable e);
}
