package rust;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import org.libDeflate.ErrorHandler;
import org.libDeflate.ParallelDeflate;
import org.libDeflate.UIPost;
import org.libDeflate.ZipEntryOutput;
import org.libDeflate.zipEntry;
import org.libDeflate.zipFile;

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
  zipEntry za=Zip.ens.get(Ou == null ?str: "assets/units/".concat(str));
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
  lod.ze = null;
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
   zipFile zip = new zipFile(red);
   Zip = zip;
   if (ou != null) {
    ZipEntryOutput out = new ZipEntryOutput(ou);
    ParallelDeflate cre = new ParallelDeflate(out);
    ErrorHandler err= new ErrorHandler(cre.pool, cre, errlist);
    err.ui = this;
    cre.on = err;
    this.cre = cre;
   }
   for (zipEntry zipe:zip.ens.values()) {
    String name = zipe.name;
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
