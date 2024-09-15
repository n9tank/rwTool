package rust;
import java.io.File;
import com.nicdahlquist.pngquant.LibPngQuant;
import org.libDeflate.UIPost;
import java.util.Collections;
import java.util.List;

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
  LibPngQuant.pngQuantFile(in, ou, 65, 80, 1, 0.5f);
  ui.accept((List)Collections.emptyList());
 }
}
