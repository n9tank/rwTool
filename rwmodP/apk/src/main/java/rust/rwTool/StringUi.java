package rust.rwTool;

import java.util.List;
import org.libDeflate.UIPost;

public class StringUi implements UIPost,Runnable {
 String ti;
 List<Throwable> err;
 public StringUi(String s) {
  ti = s;
 }
 public void accept(List<Throwable> e) {
  err = e;
  Main.show.runOnUiThread(this);
 }
 public String toString() {
  return ti;
 }
 public void run() {
  List<Throwable> e=err;
  if (e != null && e.size() > 0)Main.error(e, ti);
  Main.arr.remove(this);
 }
}
