package carsh;
import android.content.Context;
import android.util.Log;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
public class log implements Thread.UncaughtExceptionHandler {
 public static Logger log;
 public static boolean debug;
 public log(Context context) {
  log = Logger.getGlobal();
  try {
   FileHandler f=new FileHandler(context.getExternalCacheDir().getPath().concat("/log"), 0, 1, true);
   f.setFormatter(new SimpleFormatter());
   log.addHandler(f);
  } catch (Exception e) {}
 }
 public static void e(Object cla, Throwable e) {
  if (debug)log.log(Level.INFO, cla.toString(), e);
  else Log.e("rust.rwTool", cla.toString(), e);
 }
 public void uncaughtException(Thread thread, Throwable ex) {
  e(thread, ex);
 }
}
