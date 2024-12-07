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
import java.util.*;

public abstract class loaderManager implements Callable,Canceler {
 String rootPath;
 File Ou;
 File In;
 zipFile Zip;
 UIPost back;
 ErrorHandler uih;
 ConcurrentHashMap Zipmap;
 ParallelDeflate cre;
 Vector<Throwable> errlist;
 public loaderManager(File in, File ou, UIPost ui) {
  In = in;
  Ou = ou;
  Zipmap = new ConcurrentHashMap();
  back = ui;
  errlist = new Vector();
  uih = new ErrorHandler(UiHandler.ui_pool, this, errlist);
 }
 public final void init() {uih.addN(this);}
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
  zipFile zip=Zip;
  if (zip != null){
   synchronized (zip){
	UiHandler.close(zip);
	Zip = null;
   }
  }
  ErrorHandler uih=this.uih;
  if (uih != null) {
   uih.ui = back;
   uih.cancel();
  }
  ParallelDeflate cre=this.cre;   
  if (cre != null)
   cre.cancel();
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
  lod(obj, ini.copy.list);
 }
}
