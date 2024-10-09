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
import java.util.List;
import org.libDeflate.ParallelDeflate;
import org.libDeflate.ZipEntryOutput;
import org.libDeflate.UIPost;
import java.util.Collections;
import java.util.HashMap;

public class rwlib extends loaderManager implements UIPost {
 public InputStream inp;
 public static Map libMap;
 public static rwlib last;
 public rwlib(File in, InputStream io, File ou, UIPost ui) {
  super(in, ou, ui);
  inp = io;
  rwlib last=this.last;
  if (last != null)last.cancel();
  last = this;
 }
 public loader getLoder(String str) throws Throwable {
  ZipEntry za=Zip.getEntry(Ou == null ?str: "assets/units/".concat(str));
  return addLoder(za, str, "//", str, false);
 }
 public static void tolow(Map src) {
  HashMap map=new HashMap(src.size());
  for (loader lod:(Collection<loader>)src.values())
   map.put(lod.str.toLowerCase(), lod);
  libMap = map;
 }
 public void end() {
  UiHandler.close(Zip);
  File ou=Ou;
  if (ou != null) {
   try {
    cre.close();
   } catch (Exception e) {}
  } else accept(uih.err);
  tolow(Zipmap);
 }
 public static void gc(loader lod) {
  lod.gc();
  lod.copy = null;
  lod.task = null;
 }
 public void flush(loader ini) throws Exception {
  ParallelDeflate cre=this.cre;
  if (cre != null)ini.with(cre, ini.str);
 }
 public void accept(List<Throwable> list) {
  for (loader lod:(Collection<loader>)Zipmap.values())
   gc(lod);
  back.accept(list);
  last = null;
 }
 public Object call() {
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
   if (ou != null) {
    ZipEntryOutput out = zippack.zip(ou);
    ParallelDeflate cre = new ParallelDeflate(out, true);
    cre.on = new UiHandler(cre.pool, cre, this, null);
    this.cre = cre;
   }
   Enumeration<? extends ZipEntry> ens=zip.entries();
   while (ens.hasMoreElements()) {
    ZipEntry zipe=ens.nextElement();
    String name = zipe.getName();
    if (ou == null || (name.endsWith("i") && name.charAt(7) == 'u')) {
     if (ou != null)name = name.substring(13);
     addLoder(zipe, name, "//", name, false);
    }
   }
  } catch (Throwable e) {
   uih.onError(e);
  }
  uih.pop();
  return null;
 }
}
