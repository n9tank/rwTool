package rust;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import org.libDeflate.ByteBufIo;
import org.libDeflate.UIPost;

public class savedump implements Runnable {
 File in;
 File ou;
 UIPost ui;
 static Charset sets[]=new Charset[]{
  StandardCharsets.US_ASCII,
  StandardCharsets.UTF_16BE,
  StandardCharsets.UTF_16LE,
  Charset.forName("UTF-32BE"),
  Charset.forName("UTF-32LE")};
 static byte[][] bom=new byte[][]{
  new byte[]{(byte)0,(byte)0,(byte)0xfe,(byte)0xff},
  new byte[]{(byte)0xff,(byte)0xfe,(byte)0,(byte)0},
 };
 static BMFind[] xml_finds=bmfind("xml", bom);
 static BMFind[] map_finds=bmfind("<map", null);
 static BMFind[] map_end=bmfind("/map", null);
 public savedump(File i, File o, UIPost u) {
  in = i;
  ou = o;
  ui = u;
 }
 public static BMFind[] bmfind(String str, byte end[][]) {
  int len=sets.length;
  int endlen=0;
  if (end != null) {
   endlen = end.length;
   len -= endlen;
  }
  BMFind[] list=new BMFind[len];
  for (int i=0;i < len;++i)
   list[i] = new BMFind(str.getBytes(sets[i]));
  for (int i=0;len < endlen;++len,++i)
   list[len] = new BMFind(end[i]);
  return list;
 }
 public static long findsMin(byte arr[], BMFind finds[], int len) {
  int k=Integer.MAX_VALUE,v=-1;
  for (int i=0,size=finds.length;i < size;++i) {
   int j=finds[i].indexOf(arr, 0, len);
   if (j < k) {
    k = j;
    v = i;
   }
  }
  if (v >= 0)return (v << 32) | k;
  return -1;
 }
 public static long findsMax(byte arr[], BMFind finds[], int len) {
  int k=-1,v=-1;
  for (int i=0,size=finds.length;i < size;++i) {
   int j=finds[i].indexOf(arr, 0, len);
   if (j > k) {
    k = j;
    v = i;
   }
  }
  if (v >= 0)return (v << 32) | k;
  return -1;
 }
 public static int readLoop(InputStream in, byte brr[], int off) throws IOException {
  int len=brr.length;
  while (off < len) {
   int n = in.read(brr, off, len - off);
   if (n < 0)return off;
   off += n;
  }
  return off;
 }
 public void run() {
  Throwable ex=null;
  try {
   BufferedInputStream buff=new BufferedInputStream(new FileInputStream(in));
   try {
    byte[] brr=new byte[8207];
    buff.mark(0);
    int len= buff.read(brr, 0, 8192) - 1;
    int i=0;
    while (i < len)
     if (brr[i++] == (byte)0x1f && brr[i] == (byte)0x8b)break;
    if (i > 0) {
     buff.reset();
     buff.skip(--i);
     GZIPInputStream gz=new GZIPInputStream(buff, 1024);
     try {
      int l;
      int off=0;
      int blen=brr.length;
      int offlen=blen - 15;
      //utf32-1
      boolean isxml=false;
      long xmlj;
      for (;;) {
       l = readLoop(gz, brr, off);
       off = 15;
       xmlj = findsMin(brr, xml_finds, l);
       long mapj= findsMin(brr, map_finds, l);
       if (isxml = (xmlj != -1 && mapj == -1 ||  ((int)mapj) > ((int)xmlj)))break;
       else xmlj = mapj;
       if (xmlj != -1)break;
       if (l < blen)break;
       //尾部结束没有必要复制
       System.arraycopy(brr, offlen , brr, 0, off);
      }
      if (xmlj != -1) {
       int ki=(int)(xmlj >> 32);
       Charset set = sets[ki];
       int k = (int)xmlj,lastPos=0;
       FileOutputStream f=new FileOutputStream(ou);
       FileChannel ch=f.getChannel();
       //这里是BufferedOutputStream对于输入超过缓冲的没有优化
       //避免余剩字节导致的低于4k写出。
       ByteBufIo out=new ByteBufIo(ch, 8192);
       try {
        int pos=0;
        byte[] head=(isxml ?xml_finds: map_finds)[ki].drc;
        byte[] outhead=isxml && ki <= 2 ?"<?xml".getBytes(set): head;
        pos = outhead.length;
        out.write(outhead);
        k += head.length;
        for (;;) {
         len = l - k;
         out.write(brr, k, len);
         long obj= findsMax(brr, map_end, l);
         if (obj != -1)lastPos = pos + ((int)obj) + map_end[(int)(obj >> 32)].drc.length - k;
         pos += len;
         k = off;
         if (l < blen)break;
         System.arraycopy(brr, offlen , brr, 0, off);
         l = readLoop(gz, brr, off);
        }
        out.flush();
        ch.position(lastPos);
        f.write(">".getBytes(set));
        /*ch.truncate(lastPos + end.length);
        ZipParallel新版本会自动截断*/
       } finally {
        out.close();
       }
      }
     } finally {
      gz.close();
     }
    }
   } finally {
    buff.close();
   }
  } catch (Throwable e) {
   ex = e;
  }
  if (ex != null)ou.delete();
  ui.accept(UiHandler.toList(ex));
 }
}
