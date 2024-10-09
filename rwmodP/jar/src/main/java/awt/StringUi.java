package awt;
import org.libDeflate.UIPost;
import java.util.List;
import javax.swing.SwingUtilities;
import java.security.PublicKey;

public class StringUi implements UIPost,Runnable {
 String ti;
 List<Throwable> err;
 public StringUi(String ti) {
  this.ti = ti;
 }
 public void accept(List<Throwable> list) {
  err = list;
  SwingUtilities.invokeLater(this);
 }
 public String toString() {
  return ti;
 }
 public void run() {
  Main.error(err);
  Main.list.removeElement(this);
 }
}
