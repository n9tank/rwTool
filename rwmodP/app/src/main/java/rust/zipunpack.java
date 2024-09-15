package rust;

import java.io.File;
import java.io.FilterInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.libDeflate.ZipEntryM;
import org.libDeflate.ZipEntryOutput;

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
 ui ui;
 boolean raw;
 static int boomlen; 
 static Field unwarp;
 static{
  try {
   Field fid= FilterInputStream.class.getDeclaredField("in");
   fid.setAccessible(true);
   unwarp = fid;   
  } catch (Throwable e) {}  
 }
 public zipunpack(File i, File o, boolean fast, ui u) {
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
 public static InputStream getRaw(InputStream io, ZipEntry en)throws Exception {    
  return en.getMethod() == 0 ?io: (InputStream)unwarp.get(io);
 } 
 public void run() {
  Throwable ex=null;
  Charset encode=StandardCharsets.ISO_8859_1;  
  try {
   ZipFile zip= new ZipFile(in, encode);
   try {
    ZipEntryOutput zipout=new ZipEntryOutput(ou);
    zipout.setCharset(encode);
    HashSet set=new HashSet();
    StringBuilder str=new StringBuilder();
    Enumeration all=zip.entries();   
    Random ran=new Random();
    long time=System.currentTimeMillis();
    byte buf[]=zipout.buf;
    while (all.hasMoreElements()) {
     ZipEntry en=(ZipEntry)all.nextElement();
     String name=en.getName();
     if (name.endsWith("\\"))continue;
     try {
      InputStream input= zip.getInputStream(en);
      try {
       InputStream io=input;
       if (raw)io = getRaw(input, en);
       int size = io.read(buf);
       if (size <= 0 || (en.getMethod() == ZipEntry.DEFLATED && size <= 2))continue;
       name obj=getName(name, set, str, ran);
       ZipEntryM put = new ZipEntryM(obj.name, raw ?en.getMethod(): 0);
       put.setTime(obj.conts ?time: en.getTime()); 
       if (raw) {
        put.size = (int)en.getSize();
        put.crc = (int)en.getCrc();
        put.csize = (int)en.getCompressedSize();
       }
       if (put != null) {
        zipout.putEntry(put);
        zipout.write(buf, 0, size);
        int usize=boomlen - size;
        int i;
        while ((i = input.read(buf)) > 0 && usize > 0) {
         zipout.write(buf, 0, i);
         usize -= i;
        }
       }
      } finally {
       input.close();
      }
     } catch (Exception e) {}
    }
    zipout.close();
   } finally {
    zip.close();
   }
  } catch (Exception e) {
   ex = e;
  }
  ui.end(ex);
 }
}
