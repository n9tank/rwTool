package rust;
import java.io.File;
import org.libDeflate.UIPost;
import java.util.Collections;
import java.util.List;
import org.pngquant;
import java.io.IOException;

public class pngOpt implements Runnable {
 File in;
 File ou;
 UIPost ui;
 public pngOpt(File f, File o, UIPost u) {
  in = f;
  ou = o;
  ui = u;
 }
 public void run() {
  List ex=null;
  if(!pngquant.file(in.getAbsolutePath(), ou.getAbsolutePath(), pngquant.attr(65, 80, 1), 0.5f)){
   ex=UiHandler.toList(new IOException("pngqunat fail"));
  }
  ui.accept(ex);
 }
}
