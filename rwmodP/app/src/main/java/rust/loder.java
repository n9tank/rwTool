package rust;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import org.libDeflate.IoWriter;
import org.libDeflate.ParallelDeflate;
import org.libDeflate.ZipUtil;
import rust.copyKey;
import rust.loder;
class loder extends IoWriter implements Callable {
 public void with(ParallelDeflate para,String str) throws Exception {
  bufSize = 8192;
  para.with(this, ZipUtil.newEntry(str, 12));
 }
 public void flush() throws Exception {
  BufferedWriter buf=new BufferedWriter(new OutputStreamWriter(out));
  try {
   for (Map.Entry<String,cpys> ses:(Iterable<Map.Entry<String,cpys>>)ini.entrySet()) {
    HashMap v= ses.getValue().m;
    if (v.size() > 0) {
     buf.write('[');
     buf.write(ses.getKey());
     buf.write(']');
     buf.write('\n');
     for (Map.Entry<String,String> en:(Iterable<Map.Entry>)v.entrySet()) {
      buf.write(en.getKey());
      buf.write(':');
      buf.write(en.getValue());
      buf.write('\n');
     }
    }
   }
   buf.flush();
  } finally {
   buf.close();
  }
 }
 public Object call() throws Exception {
  try {
   TaskWait tas=task;   
   if (ini == null) {
    BufferedReader buff=new BufferedReader(new InputStreamReader(read), Math.min(read.available(), 8192));
    try {
     StringBuilder bf=new StringBuilder();
     String str;
     HashMap list=null;
     String last=null;
     HashMap table=new LinkedHashMap();
     ini = table;
     wh:
     while ((str = buff.readLine()) != null) {
      str = str.trim();
      if (str.length() == 0 || str.startsWith("#"))continue;
      boolean skip=str.startsWith("\"");
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
      if (str.startsWith("[") && str.indexOf(']', 1) == i) {
       if (str.startsWith("comment_", 1))last = null;
       else {
        last = str.substring(1, i).trim();
        cpys cpy=((cpys)table.get(last));
        list = cpy == null ?null: cpy.m;
       }
      } else if (last != null) {
       String value[]=str.split("[=:]", 2);
       if (value.length > 1) {
        if (list == null) {
         cpys cpy=new cpys();
         cpy.m = list = new HashMap();
         table.put(last, cpy);
        }
        String key=value[0].trim();
        String set=value[1].trim();
        list.put(key, set);
       }
      }
     }
     String file=src;
     if (file == null)return null;
     loder all=null;
     loder[] orr=new loder[0];
     //请不要定义未使用的ini。该版本移除了检查，便于并行
     file = getSuperPath(file);
     cpys cp=(cpys)table.get("core");
     if (cp != null) {
      HashMap m=cp.m;
      str = (String)m.remove("dont_load");
      isini = isini && !"1".equals(str) && !"true".equalsIgnoreCase(str);
      str = (String)m.get("copyFrom");
      if (str != null && str.length() > 0 && !str.equals("IGNORE")) {
       String lrr[]=str.replace('\\', '/').split(",");
       int i = lrr.length;
       orr = new loder[i];
       while (--i >= 0) {
        str = lrr[i].trim();
        loder lod;
        if (!str.startsWith("CORE:")) {
         String con;
         if (str.startsWith("ROOT:")) {
          str = str.substring(5);
          con = tas.rootPath;
         } else con = file;
         str = str.replaceFirst("^/+", "");
         lod = tas.getLoder(con.concat(str));
        } else lod = (loder)lib.libMap.get(str.replaceFirst("^CORE:/*", "").toLowerCase());
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
       all = task.getLoder(fin);
       if (all != null)break;
       i = fin.lastIndexOf("/", --i);
       if (i < 0)break;
       bf.setLength(i + 1);
      }
     }
     copy = new copyKey(orr);
     this.all = all;    
    } finally {
     buff.close();
    }
   }
   loder all=this.all;
   tag2: {
    tag: {
     loder[] or=copy.copy;
     for (loder orr:or)
      if (!orr.finsh)break tag;
     if (all != null && !all.finsh)break tag;
     if (tas.lod(this))break tag2;
    }
    if (tas.err == null)ui.pool.submit(this);
    return null;
   }
   finsh = true;
   tas.down(null);
  } catch (Throwable ex) {
   TaskWait tas=task;
   if (tas != null)tas.down(ex);
   throw (Exception)ex;
  }
  return null;
 }
 iniobj put;
 iniobj old;
 loder all; 
 boolean finsh;
 boolean isini;
 HashMap ini;
 copyKey copy;
 String str;
 String src;
 InputStream read;
 TaskWait task;
 static String getName(String file) {
  int len=file.length();
  if (file.endsWith("/"))--len;
  int i=file.lastIndexOf("/", len - 1);
  return file.substring(++i, len);
 }
 static String getSuperPath(String str) {
  int i=str.lastIndexOf('/', str.length() - 2);
  if (i > 0)return str.substring(0, i + 1);
  return "";
 }
}
