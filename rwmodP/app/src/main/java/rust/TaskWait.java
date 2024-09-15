package rust;
import android.util.Log;
import carsh.log;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.LongAdder;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import rust.copyKey;
import rust.iniobj;
import rust.loder;

public abstract class TaskWait implements Runnable,ui {
 LongAdder ato;
 String rootPath;
 File Ou;
 File In;
 Throwable err;
 ZipFile Zip;
 ui back;
 Map Zipmap;
 public TaskWait(File in, File ou, ui ui) {
  In = in;
  Ou = ou;
  ato = new LongAdder();
  Zipmap = new ConcurrentHashMap();
  back = ui;
 }
 public loder addLoder(ZipEntry za, String putkey, boolean isini) throws Throwable {
  loder lod = new loder();
  loder obj=(loder)Zipmap.putIfAbsent(putkey, lod);
  if (obj == null) {
   lod.isini = isini;
   lod.src = za.getName();
   lod.task = this;
   lod.read = Zip.getInputStream(za);
   add(lod);
  } else lod = obj;
  return lod;
 }
 public abstract loder getLoder(String str) throws Throwable;
 public static void lod(iniobj ini, loder orr[], loder all) {
  int i=orr.length;
  while (--i >= 0) {
   loder lod=orr[i];
   ini.put(lod.put, lod);
  }
  if (all != null)ini.put(all.put, all); 
 }
 public boolean lod(loder ini) {
  HashMap map=ini.ini;
  if (Ou != null)map = iniobj.clone(map);
  iniobj obj= new iniobj(map, ini);
  ini.put = obj;
  iniobj old=ini.old;
  copyKey key=ini.copy;
  loder[] orr=key.copy;
  if (old == null) {
   lod(obj, orr, null);  
  } else obj.put(old, null);
  return true;
 }
 void add(Callable o) throws Throwable {
  Throwable er=err;
  if (er != null)throw er;
  ato.increment();
  pool.submit(o);
 }
 public void down(Throwable e) {
  if (e != null)log.e(this, e);
  if (err != null)return;
  if (e != null)err = e;
  ui ui=back;
  LongAdder at=ato;
  if (e == null) {
   at.decrement();
   if (at.sum() >= 0l)ui = null;
  }
  if (ui != null) {
   end(e);
  }
 }
}
