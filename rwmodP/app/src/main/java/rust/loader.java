package rust;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import org.libDeflate.InputGet;
import org.libDeflate.IoWriter;
import org.libDeflate.ParallelDeflate;
import org.libDeflate.ZipEntryM;
import org.libDeflate.ZipUtil;
import rust.loader;
import rust.loaders;
public class loader extends IoWriter implements Callable,Runnable {
 public void with(ParallelDeflate para, String str) throws Exception {
  bufSize = 8192;
  ZipEntryM en=ZipUtil.newEntry(str, 12);
  para.with(this, en);
 }
 public void flush() throws Exception {
  BufferedWriter buf=getWriter(StandardCharsets.UTF_8);
  try {
   boolean st=false;
   for (Map.Entry<String, section> ses:(Set<Map.Entry<String,section>>)ini.entrySet()) {
    HashMap v= ses.getValue().m;
    if (v.size() > 0) {
     if (st)buf.write('\n');
     st = true;
     buf.write('[');
     buf.write(ses.getKey());
     buf.write(']');
     for (Map.Entry<String,String> en:(Set<Map.Entry>)v.entrySet()) {
      buf.write('\n');
      buf.write(en.getKey());
      buf.write(':');
      buf.write(en.getValue());
     }
    }
   }
   buf.flush();
  } finally {
   if (task instanceof rwlib)
    rwlib.gc(this);
   read = null;
   buf.close();
  }
 }
 public static HashMap<String,section> load(InputStream read) throws IOException {
  return load(new BufferedReader(new InputStreamReader(read), Math.min(read.available(), 8192)));
 }
 public static HashMap<String,section> load(BufferedReader buff) throws IOException {
  StringBuilder bf=new StringBuilder();
  String str;
  HashMap list=null;
  String last=null;
  HashMap table=new LinkedHashMap();
  try {
   wh:
   while ((str = buff.readLine()) != null) {
    str = str.trim();
    char fat;
    if (str.length() == 0 || (fat = str.charAt(0)) == '#')continue;
    boolean skip=fat == '\"';
    boolean c=false;
    bf.setLength(0);
    tag:
    while (true) {
     int j=0;
     while (j >= 0) {
      int k = str.indexOf("\"\"\"", j);
      int m;
      if (k < 0) {
       m = k;
       k = str.length();
      } else {
       c = !c;
       m = k + 3;
      }
      if (!skip)bf.append(str, j, k);
      j = m;
     }
     if (c)str = buff.readLine().trim();
     else {
      if (skip)continue wh;
      str = bf.toString();
      break;
     }
    }
    int i=str.length() - 1;
	if (i == 0)continue;
    if (str.charAt(0) == '[' && str.indexOf(']', 1) == i) {
     if (str.startsWith("comment_", 1))last = null;
     else {
      last = str.substring(1, i).trim();
      section cpy=((section)table.get(last));
      list = cpy == null ?null: cpy.m;
     }
    } else if (last != null) {
     String value[]=str.split("[:=]", 2);
     if (value.length > 1) {
      if (list == null) {
       section cpy=new section();
       cpy.m = list = new HashMap();
       table.put(last, cpy);
      }
      String key=value[0].trim();
      String set=value[1].trim();
      list.put(key, set);
     }
    }
   }
  } finally {
   buff.close();
  }
  return table;
 }
 public Object call() {
  run();
  return null;
 }
 public void run() {
  loaderManager tas=task;
  UiHandler ui=tas.uih;
  tagw:
  try {
   if (ini == null) {
    HashMap<String, section> table=load(read.io());
    ini = table;
    loader all=null;
    loader[] orr=new loader[0];
    //请不要定义未使用的ini。该版本移除了检查，便于并行
    CharSequence file = getSuperPath(tas instanceof rwlib ?str: src);
    section cp=table.get("core");
    StringBuilder bf=new StringBuilder();
    String str;
    if (cp != null) {
     HashMap m=cp.m;
     str = (String)m.remove("dont_load");
     isini = isini && !"1".equals(str) && !"true".equalsIgnoreCase(str);
     str = (String)m.get("copyFrom");
     if (str != null && str.length() > 0 && !str.equals("IGNORE")) {
      String lrr[]=str.replace('\\', '/').split(",");
      int len= lrr.length;
      orr = new loader[len];
      for (int i=0;i < len;++i) {
       str = lrr[i].trim();
       loader lod;
       if (!str.startsWith("CORE:")) {
        CharSequence con;
        if (str.startsWith("ROOT:")) {
         str = str.substring(5);
         con = tas.rootPath;
        } else con = file;
        str = str.replaceFirst("^/+", "");
        lod = tas.getLoder(con + str);
       } else lod = (loader)rwlib.libMap.get(str.replaceFirst("^CORE:/*", "").toLowerCase());
       orr[i] = lod;
      }
     }
    }
	if (isini) {
	 bf.setLength(0);
	 bf.append(file);
	 int i=file.length();
	 while (true) {
	  bf.append("all-units.template");
	  String fin = bf.toString();
	  all = tas.getLoder(fin);
	  if (all != null)break;
	  i = fin.lastIndexOf('/', --i);
	  if (i < 0)break;
	  bf.setLength(i + 1);
	 }
	}
    copy = new loaders(orr, all);
   }
   loaders key=this.copy;
   loader all=key.all;
   tag2: {
    tag: {
     loader[] or=key.copy;
     for (loader orr:or)
      if (!orr.type)break tag;
     if (all != null && !all.type)break tag;
     tas.lod(this);
	 break tag2;
    }
    if (!ui.iscancel()) {
     UiHandler.ui_pool.execute(this);
     return;
    } else break tagw;
   }
   type = true;
   if (tas instanceof rwlib) {
	ParallelDeflate cre=tas.cre;
	if (cre != null)with(cre, str);
   }
  } catch (Throwable e) {
   ui.onError(e);
  }
  ui.pop();
  return;
 }
 iniobj put;
 volatile loaders copy;
 volatile boolean inSet;
 public boolean inSet() {
  boolean in=inSet;
  if (in)return in;
  synchronized (this) {
   in = inSet;
   if (in)return in;
   inSet = true;
  }
  return in;
 }
 volatile boolean type;
 boolean isini;
 HashMap ini;
 String str;
 String src;
 InputGet read;
 loaderManager task;
 static CharSequence getName(String file) {
  int len=file.length();
  int i=file.lastIndexOf('/', len - 1);
  if (i < 0)return file;
  return file.subSequence(++i, len);
 }
 static CharSequence getSuperPath(String str) {
  int i=str.lastIndexOf('/', str.length() - 2);
  if (i > 0)return str.subSequence(0, i + 1);
  return "";
 }
}
