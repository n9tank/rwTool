package rust;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Iso;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.libDeflate.UIPost;
import org.libDeflate.ZipEntryInput;
import org.libDeflate.ZipEntryM;
import org.libDeflate.ZipEntryOutput;
import java.util.Collections;
import android.util.Log;
import org.libDeflate.ParallelDeflate;

public class zipunpack implements Runnable {
 public static class name {
  String name;
  boolean conts;  
  public name(String str, boolean con) {
   name = str;
   conts = con;
  } 
 }
 File in;
 File ou;
 UIPost ui;
 boolean raw;
 static int boomlen; 
 public zipunpack(File i, File o, boolean fast, UIPost u) {
  in = i;
  ou = o;
  ui = u;
  raw = fast;  
 }
 public static int toName(String name) {
  int i=name.length();
  while (name.charAt(--i) == '/');
  return ++i;
 } 
 public static name getName(String name, HashSet set, StringBuilder buf, Random ran) {
  boolean conts=false;
  int i=toName(name);
  if (name.length() > i)name = name.substring(0, i);
  if (!set.add(name)) {  
   buf.setLength(0);      
   buf.append(name);
   if (i > 0) {
    while (--i > 0)buf.append('-');	
   } else buf.append('_');
   while (!set.add(name = buf.toString())) {
    conts = true; 
    char c=(char)(ran.nextInt(93) + 33);
    if (c == '-')++c;
    buf.append(c);
   }
  }
  return new name(name, conts);
 }
 public static List<Throwable> toList(Throwable e) {
  return e == null ?null: Collections.singletonList(e);
 }
 public void run() {
  Throwable ex=null;
  try {
   ZipFile zip= new ZipFile(in, StandardCharsets.ISO_8859_1);
   try {
    ZipEntryOutput zipout=new ZipEntryOutput(ou, 16384, new Iso());
    zipout.flag = (byte)zipout.igonUtf8;
    HashSet set=new HashSet();
    StringBuilder str=new StringBuilder();
    Enumeration all=zip.entries();   
    Random ran=new Random();
    int time=ZipEntryM.dosTime(System.currentTimeMillis());
    byte buf[]=new byte[16384];
    while (all.hasMoreElements()) {
     ZipEntry en=(ZipEntry)all.nextElement();
     String name=en.getName();
     if (name.endsWith("\\"))continue;
     if (!raw || en.getCompressedSize() == 0)en.setMethod(0);
     try {
      InputStream input= zip.getInputStream(en);
      try {
       InputStream io=input;
       if (raw)io = ZipEntryInput.getRaw(io, en);
       int size = ParallelDeflate.readLoop(io, buf);
       if (size <= 0 || (en.getMethod() == ZipEntry.DEFLATED && size <= 2))continue;
       name obj=getName(name, set, str, ran);
       ZipEntryM put = new ZipEntryM(obj.name, raw ?en.getMethod(): 0);
       put.xdostime = obj.conts ?time: ZipEntryM.dosTime(en.getTime());
       if (raw) {
        put.size = (int)en.getSize();
        put.csize = (int)en.getCompressedSize();
       }
       if (put != null) {
        zipout.putEntry(put, true);
        zipout.write(buf, 0, size);
        int usize=boomlen - size;
        int i;
        while ((i = ParallelDeflate.readLoop(io, buf)) > 0 && usize > 0) {
          zipout.write(buf, 0, i);
          usize -= i;
        }
       }
      } finally {
       input.close();
      }
     } catch (Throwable e) {
      //忽略的错误
     }
    }
    zipout.close();
   } finally {
    zip.close();
   }
  } catch (Exception e) {
   ex = e;
  }
  ui.accept(toList(ex));
 }
}
