import java.text.Bidi;
import java.util.Comparator;
public class charc implements Comparator<String> {
 public int compare(String o1, String o2) {
  return cons(o1) - cons(o2);
 }
 public static int cons(String text) {
  Bidi bidi = new Bidi(text, Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT);
  if (bidi.isRightToLeft())return 1;
  return 0;
 }
}
