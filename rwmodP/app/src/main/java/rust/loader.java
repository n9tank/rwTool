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
import org.libDeflate.InputGet;
import org.libDeflate.IoWriter;
import org.libDeflate.ParallelDeflate;
import org.libDeflate.ZipUtil;
import rust.loader;
import rust.loaders;
import java.util.regex.Pattern;
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
 public static int utf8len(CharSequence str) {
  int len=str.length() ;
  return (len << 1) - (len >> 1);
  //1.5倍容量，最大3倍
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
  boolean st=false;
  int all=0;
  for (Map.Entry<String, section> ses:(Set<Map.Entry<String,section>>)ini.entrySet()) {
   HashMap v= ses.getValue().m;
   if (v.size() > 0) {
    if (st)all++;
    st = true;
    all += utf8len(ses.getKey());
    all += 2;
    for (Map.Entry<String,String> en:(Set<Map.Entry>)v.entrySet()) {
     all += 2;
     all += utf8len(en.getKey());
     all += utf8len(en.getValue());
    }
   }
  }
  bufSize = all;
  para.with(this, ZipUtil.newEntry(str, 12));
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
   buf.close();
  }
 }
 public static HashMap<String,section> load(InputStream read) throws IOException {
  return load(new BufferedReader(new InputStreamReader(read), Math.min(read.available(), 8192)));
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
 /*
  public static final Pattern pathtrim=Pattern.compile("^/+");
  public static final Pattern coretrim=Pattern.compile("^CORE:/*");
  public static String PathTrim(Pattern path, String str) {
  return path.matcher(str).replaceFirst("");
  }*/
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
    loader[] orr=new loader[1];
    //请不要定义未使用的ini。该版本移除了检查，便于并行
    CharSequence file = getSuperPath(tas instanceof rwlib ?str: src);
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
        CharSequence con;
        if (str.startsWith("ROOT:")) {
         str = str.substring(5);
         con = tas.rootPath;
        } else con = file;
        lod = tas.getLoder(con + str);
       } else lod = (loader)rwlib.libMap.get(str.substring(5).toLowerCase());
       orr[i + 1] = lod;
      }
     }
    }
	if (isini) {
     StringBuilder bf=new StringBuilder(file); 
	 int i=file.length();
	 while (true) {
	  bf.append("all-units.template");
	  String fin = bf.toString();
	  loader all = tas.getLoder(fin);
	  if (all != null) {
       orr[0] = all;
       break;
      }
	  i = fin.lastIndexOf('/', --i);
	  if (i < 0)break;
	  bf.setLength(i + 1);
	 }
	}
    copy = new loaders(orr);
   }
   loaders key=this.copy;
   tag2: {
    tag: {
     loader[] or=key.copy;
     for (loader orr:or)
      if (orr != null && !orr.type)break tag;
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
