package rust;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.libDeflate.ParallelDeflate;
import org.libDeflate.ZipEntryOutput;

public class lib extends TaskWait {
 public InputStream inp;
 public static Map libMap;
 public lib(File in, InputStream io, File ou, ui ui) {
  super(in, ou, ui);
  inp = io;
 }
 public loder getLoder(String str) throws Throwable {
  ZipEntry za=Zip.getEntry(str);
  if (Ou != null)str = str.substring(13);
  str = str.toLowerCase();
  return addLoder(za, str, false);
 }
 public void end(Throwable e) {
  try {
   ZipFile zip=Zip;
   if (zip != null)zip.close();
  } catch (Exception e2) {
  }
  File ou=Ou;
  if (e == null) {
   Collection<loder> vl=Zipmap.values();
   if (ou != null) {
    try {
     ZipEntryOutput out = new ZipEntryOutput(ou);
     out.AsInput = false;
     ParallelDeflate cre = new ParallelDeflate(out, true);
     cre.on = new UiHandler(cre, null, back);
     try {
      for (loder lod:vl) {
       lod.with(cre, lod.src);
      }
     } finally {
      cre.close();
     }
    } catch (Throwable e2) {
     back.end(e);
    }
   }
   for (loder lod:vl) {
    lod.task = null;
    lod.src = "//";//CORE://
   }
   libMap = Zipmap;
  }
  if (ou == null)back.end(e);
 }
 public void run() {
  File ou=Ou;
  try {
   File red;
   InputStream in=inp;
   if (ou != null)ou.getParentFile().mkdirs();
   if (in != null) {
    red = ou;
    FileChannel ch=new FileOutputStream(ou).getChannel();
    Ou = ou = null;
    try {
     ch.transferFrom(Channels.newChannel(in), 0L, Long.MAX_VALUE);
    } finally {
     ch.close();
    }
   } else red = In;
   ZipFile zip = new ZipFile(red);
   Zip = zip;
   Enumeration<? extends ZipEntry> ens=zip.entries();
   while (ens.hasMoreElements()) {
    ZipEntry zipe=ens.nextElement();
    String name = zipe.getName();
    if (ou == null || (name.endsWith("i") && name.charAt(7) == 'u')) {
     if (ou != null)name = name.substring(13);
     name = name.toLowerCase();
     loder lod=addLoder(zipe, name, false);
     lod.str = name;
    }
   }
   ato.decrement();
  } catch (Throwable e) {
   down(e);
  }
 }
}
