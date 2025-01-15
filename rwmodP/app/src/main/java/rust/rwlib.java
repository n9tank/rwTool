package rust;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.libDeflate.ParallelDeflate;
import org.libDeflate.UIPost;
import org.libDeflate.ZipEntryOutput;
import java.util.List;
import java.nio.file.StandardOpenOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class rwlib extends loaderManager implements UIPost {
 public InputStream inp;
 public static Map libMap;
 public static rwlib last;
 public rwlib(File in, InputStream io, File ou, UIPost ui) {
  super(in, ou, ui);
  inp = io;
  rwlib last=rwlib.last;
  if (last != null)
   last.cancel();
  rwlib.last = this;
 }
 public loader getLoder(String str) throws Throwable {
  ZipEntry za=Zip.getEntry(Ou == null ?str: "assets/units/".concat(str));
  return addLoder(za, str, "/", str, false);
 }
 public static void tolow(Map src) {
  HashMap map=new HashMap(src.size());
  for (loader lod:(Collection<loader>)src.values())
   map.put(lod.str.toLowerCase(), lod);
  libMap = map;
 }
 public void cancel() {
  last = null;
  super.cancel();
 }
 public void end() {
  UiHandler.close(Zip);
  uih = null;
  if (Ou != null)
   cre.on.pop();
  else accept(null);
  tolow(Zipmap);
 }
 public static void gc(loader lod) {
  lod.ini = null;
  lod.read = null;
  lod.copy = null;
  lod.task = null;
 }
 public void accept(List<Throwable> list) {
  for (loader ini:(Collection<loader>)Zipmap.values())
   gc(ini);
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
    Ou = ou = null;
    try {
     Files.copy(in, red.toPath(), StandardCopyOption.REPLACE_EXISTING);
    } finally {
     in.close();
    }
   } else red = In;
   ZipFile zip = new ZipFile(red);
   Zip = zip;
   if (ou != null) {
    ZipEntryOutput out = zippack.zip(ou);
    ParallelDeflate cre = new ParallelDeflate(out, true);
    cre.on = new UiHandler(cre.pool, cre, this);
    this.cre = cre;
   }
   Enumeration<? extends ZipEntry> ens=zip.entries();
   while (ens.hasMoreElements()) {
    ZipEntry zipe=ens.nextElement();
    String name = zipe.getName();
    if (ou == null || (name.endsWith("i") && name.charAt(7) == 'u')) {
     if (ou != null)name = name.substring(13);
     addLoder(zipe, name, "/", name, false);
    }
   }
  } catch (Throwable e) {
   uih.onError(e);
  }
  uih.pop();
  return null;
 }
}
