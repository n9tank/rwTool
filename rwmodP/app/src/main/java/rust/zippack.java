package rust;

import java.io.File;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.libDeflate.ParallelDeflate;
import org.libDeflate.ZipEntryInput;
import org.libDeflate.ZipEntryM;
import org.libDeflate.ZipEntryOutput;
import org.libDeflate.ZipUtil;
import org.libDeflate.ZipInputGet;
import org.libDeflate.UIPost;
import java.util.Collections;

public class zippack implements Runnable {
 File in;
 File ou;
 UIPost ui;
 public static int head[];
 public static boolean keepUnSize; 
 public boolean raw;
 public zippack(File i, File o, boolean rw, UIPost u) {
  in = i;
  ou = o;
  raw = rw;
  ui = u;
 }
 public static void writeOrCopy(ParallelDeflate para, ZipFile zipf, ZipEntry en, ZipEntryM zip, boolean raw) throws Throwable {
  if (!raw || en.getMethod() <= 0 || zip.mode <= 0)para.writeToZip(new ZipInputGet(zipf, en, false), zip);
  else para.copyToZip(new ZipInputGet(zipf, en, true), zip);
 }
 public static ZipEntryOutput zip(File ou) throws Exception {
  ZipEntryOutput out= new ZipEntryOutput(ou);
  out.flag = (byte)out.openJdk8opt;
  return out;
 }
 public static ZipEntryOutput enZip(File ou) throws Exception {
  ZipEntryOutput out=new ZipEntryOutput(ou);
  int flag= out.openJdk8opt | out.enmode;
  if (!keepUnSize)flag |= out.rcise;
  out.flag = (byte)flag;
  ZipUtil.addRandomHead(out, zippack.head);
  out.outDef.free();//释放空间
  return out;
 }
 public void run() {
  try {
   ZipFile zip= new ZipFile(in);
   try {
    ZipEntryOutput zipout=enZip(ou);
    try {
     ParallelDeflate cr=new ParallelDeflate(zipout, true);
     cr.on = new UiHandler(cr.pool, cr, ui, zip);
     try {
      Enumeration all=zip.entries();
      while (all.hasMoreElements()) {
       ZipEntry en=(ZipEntry)all.nextElement();
       String name=en.getName();
       int mode=12;
       if (!en.isDirectory()) {
        int n=name.length() - 4;          
        if (name.regionMatches(true, n, ".png", 0, 4))mode = 0;   
        if (!name.regionMatches(true, n, ".ogg", 0, 4) && !name.regionMatches(true, n, ".wav", 0, 4))name = name.concat("/");
       } else name = null;
       if (name != null) {
        ZipEntryM put=ZipUtil.newEntry(name, mode);
        put.size = (int)en.getSize();
        writeOrCopy(cr, zip, en, put , raw);
       }
      }
     } catch (Exception e) {
      cr.cancel();
     }finally{
      cr.close();
     }
    } catch (Exception e) {
     zipout.cancel();
     throw e;
    }
   } catch (Exception e) {
    zip.close();
    throw e;
   }
  } catch (Throwable e) {
   ui.accept(Collections.singletonList(e));
  }
 }
}
