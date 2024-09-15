package rust;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipFile;
import org.libDeflate.Canceler;
import org.libDeflate.ErrorHandler;
import org.libDeflate.UIPost;
import android.util.Log;

public class UiHandler extends ErrorHandler {
 public static final ExecutorService ui_pool=Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
 volatile UIPost ui;
 ZipFile zip;
 public UiHandler(ExecutorService pool, Canceler can, UIPost on, ZipFile f) {
  super(pool, can);
  ui = on;
  zip = f;
 }
 public static void close(AutoCloseable co) {
  if (co != null) {
   try {
    co.close();
   } catch (Exception e) {}
  }
 }
 public void onClose() {
  close(zip);
  UIPost ui=this.ui;
  if (ui != null)ui.accept(err);
 }
}
