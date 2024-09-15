package rust;
import java.io.File;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.libDeflate.Canceler;
import org.libDeflate.ParallelDeflate;
import org.libDeflate.UIPost;
import org.libDeflate.ZipInputGet;
import rust.iniobj;
import rust.loader;
import rust.loaders;
import android.util.Log;

public abstract class loaderManager implements Callable,Canceler {
 String rootPath;
 File Ou;
 File In;
 ZipFile Zip;
 UIPost back;
 UiHandler uih;
 ConcurrentHashMap Zipmap;
 ParallelDeflate cre;
 public loaderManager(File in, File ou, UIPost ui) {
  In = in;
  Ou = ou;
  Zipmap = new ConcurrentHashMap();
  back = ui;
 }
 public void init() {
  UiHandler uih = new UiHandler(UiHandler.ui_pool, this, null, null);
  this.uih = uih;
  uih.addN(this);
 }
 public loader addLoder(ZipEntry za, String putkey, String src, String str, boolean isini) throws Throwable {
  loader lod = new loader();
  loader obj=(loader)Zipmap.putIfAbsent(putkey, lod);
  if (obj == null) {
   lod.isini = isini;
   lod.src = src;
   lod.str = str;
   lod.task = this;
   ZipInputGet inget=new ZipInputGet(Zip, za, false);
   inget.buf = true;
   lod.read = inget;
   uih.add(lod);
  } else lod = obj;
  return lod;
 }
 public void cancel() {
  if (uih.cancel()) {
   ParallelDeflate cre=this.cre;
   if (cre != null)cre.cancel();
   uih.ui = back;
  }
 }
 public abstract loader getLoder(String str) throws Throwable;
 public static void lod(iniobj ini, loader orr[], loader all) {
  int i=orr.length;
  while (--i >= 0) {
   loader lod=orr[i];
   ini.put(lod.put, lod);
  }
  if (all != null)ini.put(all.put, all); 
 }
 public boolean lod(loader ini) {
  HashMap map=ini.ini;
  if (Ou != null)map = iniobj.clone(map);
  iniobj obj= new iniobj(map, ini);
  ini.put = obj;
  iniobj old=ini.old;
  loaders key=ini.copy;
  loader[] orr=key.copy;
  if (old == null) {
   lod(obj, orr, null);
  } else obj.put(old, null);
  return true;
 }
}
