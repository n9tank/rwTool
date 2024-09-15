import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.Scanner;
import rust.lib;
import rust.rwmodProtect;
import rust.ui;

public class cui implements ui {
 String show;
 long g;
 int len;
 cui(String s) {
  show = s;
  g = System.currentTimeMillis();
 }
 public void end(Throwable e) {
  if (e != null)e.printStackTrace();
  else {
   PrintStream out=System.out;
   if (len > 0)out.print('\n');
   out.print(show);
   out.print(':');
   out.print(System.currentTimeMillis() - g);
   out.println("ms");
  }
 }
 public static void main(String[] args) throws Exception {
  long g=System.currentTimeMillis();
  String dir=System.getProperty("user.dir");
  if (dir.length() == 1) {
   dir = "sdcard/rustedWarfare/rwmod";
  }
  try {
   rwmodProtect.init(new FileReader(new File(dir, ".ini")));
   File f=new File(dir, ".txt");
   if (f.length() != 0)rwmodProtect.dictionary(new FileReader(f));
   File in=new File(dir, "lib.zip");
   if (in.length() > 0)lib.exec(in, null, new cui("lib"));
   PrintStream out=System.out;
   out.print(System.currentTimeMillis() - g);
   out.println("ms");
   Scanner sc=new Scanner(System.in);
   while (true) {
    String str=sc.next();
    boolean islib;
    if (islib = str.equals("lib"))str = sc.next();
    File path=new File(str);
    if (path.length() == 0) {
     out.println("文件异常");
     continue;
    } else {
     cui ui=new cui(str);
     if (islib) {
      lib.exec(path, in, ui);
     } else rwmodProtect.exec(path, new File(path.getParent(), rwmodProtect.out(path)), ui);
    }
   }
  } catch (Throwable e) {
   e.printStackTrace();
  }
 }
}
