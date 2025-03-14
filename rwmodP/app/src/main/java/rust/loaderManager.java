package rust;
import java.io.File;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import org.libDeflate.Canceler;
import org.libDeflate.ErrorHandler;
import org.libDeflate.ParallelDeflate;
import org.libDeflate.UIPost;
import org.libDeflate.ZipInputGet;
import org.libDeflate.zipFile;
import rust.iniobj;
import rust.loader;
import org.libDeflate.zipEntry;

public abstract class loaderManager implements Callable,Canceler {
 String rootPath;
 File Ou;
 File In;
 zipFile Zip;
 UIPost back;
 ErrorHandler uih;
 ConcurrentHashMap Zipmap;
 ParallelDeflate cre;
 public loaderManager(File in, File ou, UIPost ui) {
  In = in;
  Ou = ou;
  Zipmap = new ConcurrentHashMap();
  back = ui;
 }
 public void init() {
  ErrorHandler uih = new ErrorHandler(UiHandler.ui_pool, this);
  this.uih = uih;
  uih.addN(this);
 }
 public loader addLoder(zipEntry za, String putkey, String src, String str, boolean isini) throws Throwable {
  loader lod = new loader();
  loader obj=(loader)Zipmap.putIfAbsent(putkey, lod);
  if (obj == null) {
   lod.isini = isini;
   lod.src = src;
   lod.str = str;
   lod.task = this;
   lod.ze = za;
   uih.add(lod);
  } else lod = obj;
  return lod;
 }
 public void cancel() {
  UiHandler.close(Zip);
  ErrorHandler uih=this.uih;
  ParallelDeflate cre=this.cre;   
  if (uih != null) {
   uih.ui = back;
   if (uih.cancel() && cre != null)
    uih.err.addAll(cre.on.err);
  }
  if (cre != null)cre.cancel();
 }
 public abstract loader getLoder(String str) throws Throwable;
 public static void lod(iniobj ini, loader orr[]) {
  for (int i=orr.length;--i >= 0;) {
   loader lod=orr[i];
   if (lod != null)
    ini.put(lod.put);
  }
 }
 public void lod(loader ini) {
  HashMap map=ini.ini;
  if (Ou != null)map = iniobj.clone(map);
  iniobj obj= new iniobj(map);
  ini.put = obj;
  lod(obj, ini.copy.copy);
 }
}
