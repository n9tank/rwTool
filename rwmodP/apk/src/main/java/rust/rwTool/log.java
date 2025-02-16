package rust.rwTool;
import java.io.IOException;
import org.libDeflate.UIPost;
import rust.UiHandler;

public class log implements Runnable {
 public String str;
 public UIPost ui;
 public log(String str, UIPost ui) {
  this.str = str;
  this.ui = ui;
 }
 public void run() {
  Throwable ex=null;
  try {
   Main.save(str);
  } catch (IOException e) {
   ex = e;
  }
  ui.accept(UiHandler.toList(ex));
 }
}
