package rust;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.libDeflate.UIPost;
import org.libDeflate.ParallelDeflate;

public class UiHandler {
 public static final ExecutorService ui_pool=Executors.newFixedThreadPool(ParallelDeflate.CPU);
 public static void close(AutoCloseable co) {
  if (co != null) {
   try {
    co.close();
   } catch (Exception e) {}
  }
 }
 public static File out(File path, int i, String end) {
  String name=path.getName();
  return new File(path.getParent(), ImageUtil.concat(name.substring(0, name.length() - i),end));
 }
 public static List<Throwable> toList(Throwable e) {
  return e == null ?null: Collections.singletonList(e);
 }
 public static boolean DefaultRunTask(File f, int id , int p1, int p2, boolean raw, File dir, UIPost StringUi) {
  Runnable run=null;
  loaderManager call=null;
  String path=f.getPath();
  if (path.endsWith(".rwmod")) {
   if (id == p1) {
	call = new rwmodProtect(f, out(f, 6, "_r.rwmod"), StringUi, raw);
   } else if (id == p2)run = new zippack(f, out(f, 6, "_p.rwmod"), raw, StringUi);
   else run = new zipunpack(f, StringUi);
  } else if (path.endsWith(".apk")) {
   call = new rwlib(f, null, new File(dir, "lib.zip"), StringUi);
  } else if (path.endsWith(".rwsave") || path.endsWith(".replay")) {
   run = new savedump(f,  out(f, 6, "tmx"), StringUi);
  } else if (path.endsWith(".tmx")) {
   run = new rwmapOpt(f, out(f, 4, "_r.tmx"), StringUi);
  } else if (path.endsWith(".png")) {
   run = new pngOpt(f, out(f, 4, "_r.png"), StringUi);
  }
  if (run != null) {
   UiHandler.ui_pool.execute((Runnable)run);
   return true;
  } else if (call != null) {
   call.init();
   return true;
  }
  return false;
 }
}
