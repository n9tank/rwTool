package rust;

import java.io.File;
import java.util.List;
import org.libDeflate.ErrorHandler;
import org.libDeflate.ParallelDeflate;
import org.libDeflate.UIPost;
import org.libDeflate.ZipEntryM;
import org.libDeflate.ZipEntryOutput;
import org.libDeflate.ZipInputGet;
import org.libDeflate.ZipUtil;
import org.libDeflate.zipEntry;
import org.libDeflate.zipFile;
import java.util.*;

public class zippack implements Runnable,UIPost {
 File in;
 File ou;
 UIPost ui;
 public static int head[];
 public boolean raw;
 public zippack(File i, File o, boolean rw, UIPost u) {
  in = i;
  ou = o;
  raw = rw;
  ui = u;
 }
 public static void writeOrCopy(ParallelDeflate para, zipFile zipf, zipEntry en, ZipEntryM zip, boolean raw) throws Throwable {
  para.writeToZip(new ZipInputGet(zipf, en), zip, raw);
 }
 public static boolean zip64enmode;
 public static ZipEntryOutput enZip(File ou) throws Exception {
  ZipEntryOutput out=new ZipEntryOutput(ou);
  int flag= out.openJdk8opt | out.enmode;
  if (zip64enmode)flag |= out.zip64enmode;
  out.flag = flag;
  int irr[]=zippack.head;
  if (irr != null)
   ZipUtil.addRandomHead(out, irr);
  return out;
 }
 zipFile Zip;
 public void accept(List<Throwable> list) {
  UiHandler.close(Zip);
  ui.accept(list);
 }
 public void run() {
  try {
   zipFile zip= new zipFile(in);
   this.Zip = zip;
   try {
    ParallelDeflate cr=new ParallelDeflate(enZip(ou));
    ErrorHandler err = new ErrorHandler(cr.pool, cr, new Vector());
    err.ui = this;
    cr.on = err;
    try {
     for (zipEntry en:zip.ens.values()) {
      String name=en.name;
      int mode=12;
      if (!name.endsWith("/")) {
       int n=name.length() - 4;          
       if (!name.regionMatches(true, n, ".ogg", 0, 4) && !name.regionMatches(true, n, ".wav", 0, 4))
        name = ImageUtil.concat(name,"/");
       ZipEntryM put=ZipUtil.newEntry(name, mode);
       put.size = (int)en.size;
       writeOrCopy(cr, zip, en, put , raw);
      }
     }
    } catch (Exception e) {
     cr.cancel();
    }
    cr.on.pop();
   } catch (Exception e) {
    zip.close();
    throw e;
   }
  } catch (Throwable e) {
   ui.accept(UiHandler.toList(e));
  }
 }
}
