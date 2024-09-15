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

public class zippack implements Runnable {
 File in;
 File ou;
 ui ui;
 public static int head[];
 public static boolean keepUnSize; 
 public boolean raw;
 public zippack(File i, File o, boolean rw, ui u) {
  in = i;
  ou = o;
  raw = rw;
  ui = u;
 }
 public static void writeOrCopy(ParallelDeflate para, InputStream in, ZipEntry en, ZipEntryM zip, boolean raw) throws Throwable {
  if (!raw || en.getMethod() <= 0 || zip.mode <= 0)para.writeToZip(in, zip);
  else para.copyToZip(ZipEntryInput.getRaw(in, en), zip);
 }
 public void run() {
  try {
   ZipFile zip= new ZipFile(in);
   try {
    ZipEntryOutput zipout=new ZipEntryOut(ou);
    try {
     ParallelDeflate cr=new ParallelDeflate(zipout, true);
     cr.on = new UiHandler(cr, zip, ui);
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
       if (name != null)
        writeOrCopy(cr, zip.getInputStream(en), en, ZipUtil.newEntry(name, mode), raw);
      }
      cr.close();
     } catch (Exception e) {
      cr.cancel();
      throw e;
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
   ui.end(e);
  }
 }
}
