package rust;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import org.libDeflate.ErrorHandler;
import org.libDeflate.IoWriter;
import org.libDeflate.NioReader;
import org.libDeflate.ParallelDeflate;
import org.libDeflate.ZipInputGet;
import org.libDeflate.ZipUtil;
import org.libDeflate.zipEntry;
import rust.loader;
import rust.loaders;
import java.io.BufferedInputStream;
import java.nio.channels.Channels;
import java.nio.ByteBuffer;
public class loader extends IoWriter implements Callable,Runnable,Comparable {
 public int compareTo(Object o) {
  if (this != o) {
   loader p2=((loader)o);
   String path=this.src;
   int i = path.compareTo(p2.src);
   if (i != 0)return i;
   //对于依赖库仍需避免冲突
   if (path == "/") {
    i = this.str.compareTo(p2.str);
    if (i != 0)return i;  
   }
  }
  return 0;
 }
 public static HashSet boolset;
 public static boolean isBool(String key) {
  if (boolset.contains(key))return true;
  int i=key.indexOf('_');
  if (i > 0) {
   if (boolset.contains(key.substring(0, i + 1))) {
    int j=key.lastIndexOf('_');
    String end=key.substring(j + 1);
    return boolset.contains(end);
   }
  }
  return false;
 }
 public static String replace(String key, String value) {
  //现在不允许bool通过${section.key}访问，因为这与优化冲突
  //你仍可以将bool值使用宏 key:${bool}
  if (value.equalsIgnoreCase("true")) {
   if (isBool(key))return "1";
  } else if (value.equalsIgnoreCase("false")) {
   if (isBool(key))return "0";
  }
  return value;
 }
 public void with(ParallelDeflate para, String str) throws Exception {
  int cbytes=0;
  int len=0;
  for (Map.Entry<String, section> ses:(Set<Map.Entry<String,section>>)ini.entrySet()) {
   HashMap v= ses.getValue().m;
   int size=v.size();
   if (size > 0) {
    len += ses.getKey().length();
    cbytes += (size << 1) + 3;
    for (Map.Entry<String,String> en:(Set<Map.Entry>)v.entrySet()) {
     len += en.getKey().length();
     len += en.getValue().length();
    }
   }
  }
  bufSize = cbytes > 0 ?len + (len >> 1) + cbytes - 1: 0;
  para.with(this, ZipUtil.newEntry(str, 12), false);
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
   //buf.flush();
  } finally {
   buf.close();
  }
 }
 public static InputStream loop(final InputStream input) {
  return new InputStream(){
   public int read() {
    return 0;
   }
   public int read(byte b[], int off, int len) throws IOException {
    int i=0;
    while (true) {
     int n= input.read(b, off + i, len - i);
     if (n > 0)i += n;
     else return i == 0 ?-1: i;
    }
   }
   public void close() throws IOException {
    input.close();
   }
  };
 }
 public static HashMap<String,section> load(InputStream read) throws IOException {
  //现在与NioReader是一样的性能
  return load(new BufferedReader(new InputStreamReader(loop(read))));
 }
 public static final char[] split=new char[]{':','='};
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
    int i=str.length();
    if (str.charAt(0) == '[' && str.indexOf(']', 1) == --i) {
     if (str.startsWith("comment_", 1))last = null;
     else {
      last = str.substring(1, i).trim();
      section cpy=((section)table.get(last));
      list = cpy == null ?null: cpy.m;
     }
    } else if (last != null) {
     int splitIn=iniobj.indexOfChars(str, split);
     if (splitIn >= 0) {
      if (list == null) {
       section cpy=new section();
       cpy.m = list = new HashMap();
       table.put(last, cpy);
      }
      String key=str.substring(0, splitIn).trim();
      String set=str.substring(splitIn + 1).trim();
      set = replace(key, set);
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
  ErrorHandler ui=tas.uih;
  tagw:
  try {
   if (copy == null) {
    HashMap<String, section> table=load(ZipInputGet.reader(tas.Zip, ze, StandardCharsets.UTF_8));
    ini = table;
    loader[] orr=new loader[1];
    //请不要定义未使用的ini。该版本移除了检查，便于并行
    String file = getSuperPath(tas instanceof rwlib ?str: src);
    section cp=table.get("core");
    String str;
    if (cp != null) {
     HashMap m=cp.m;
     str = (String)m.remove("dont_load");
     isini = isini && !"1".equals(str) && !"true".equalsIgnoreCase(str);
     str = (String)m.get("copyFrom");
     if (str != null && str.length() > 0 && !str.equals("IGNORE")) {
      String lrr[]=str.replace('\\', '/').split(",");
      int len= lrr.length;
      orr = new loader[len + 1];
      for (int i=0;i < len;++i) {
       str = lrr[i].trim();
       loader lod;
       if (!str.startsWith("CORE:")) {
        String con;
        if (str.startsWith("ROOT:")) {
         str = str.substring(5);
         con = tas.rootPath;
        } else con = file;
        lod = tas.getLoder(ImageUtil.concat(con,str));
       } else lod = (loader)rwlib.libMap.get(str.substring(5).toLowerCase());
       orr[i + 1] = lod;
      }
     }
    }
	if (isini) {
	 int len=file.length();
	 while (true) {
	  file = ImageUtil.concat(file,("all-units.template"));
	  loader all = tas.getLoder(file);
	  if (all != null) {
       orr[0] = all;
       break;
      }
	  len = file.lastIndexOf('/', --len);
	  if (len < 0)break;
      file = file.substring(0, len + 1);
	 }
	}
    copy = new loaders(orr);
   }
   loaders key=this.copy;
   tag2: {
    tag: {
     for (loader lod:key.list)
      if (lod != null && !lod.type)break tag;
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
 loaders copy;
 int lvl=-1;
 volatile HashMap old;
 volatile boolean type;
 boolean isini;
 HashMap ini;
 String str;
 String src;
 zipEntry ze;
 loaderManager task;
 static String getName(String file) {
  int len=file.length();
  int i=file.lastIndexOf('/', len - 1);
  if (i < 0)return file;
  return file.substring(++i, len);
 }
 static String getSuperPath(String str) {
  int i=str.lastIndexOf('/', str.length() - 2);
  if (i > 0)return str.substring(0, i + 1);
  return "";
 }
}
