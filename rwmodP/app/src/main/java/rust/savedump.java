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
  new byte[0]
  ,new byte[]{(byte)0xfe,(byte)0xff},
  new byte[]{(byte)0xff,(byte)0xfe},
  new byte[]{(byte)0,(byte)0,(byte)0xfe,(byte)0xff},
  new byte[]{(byte)0xff,(byte)0xfe,(byte)0,(byte)0},
 };
 static BMFind[] xml_finds=bmfind("xml");
 static BMFind[] map_finds=bmfind("<map");
 public static byte[][] getBytes(String str) throws UnsupportedEncodingException {
  int l=sets.length;
  byte[][] brr=new byte[l][];
  for (;--l >= 0;)
   brr[l] = str.getBytes(sets[l]);
  return brr;
 }
 public savedump(File i, File o, UIPost u) {
  in = i;
  ou = o;
  ui = u;
 }
 public static BMFind[] bmfind(String str) {
  int len=sets.length;
  BMFind[] list=new BMFind[len];
  for (;--len >= 0;)
   list[len] = new BMFind(str.getBytes(sets[len]));
  return list;
 }
 public static int[] finds(byte arr[], BMFind finds[], int len) {
  for (int i=finds.length;--i >= 0;) {
   int j=finds[i].indexOf(arr, 0, len);
   if (j >= 0)return new int[]{i,j};
  }
  return null;
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
     GZIPInputStream gz=new GZIPInputStream(buff, 8192);
     try {
      int l;
      int off=0;
      int offlen=brr.length - 15;
      //utf32-1
      boolean isxml=false;
      int xmlj[]=null;
      while ((l = readLoop(gz, brr, off)) - off > 0) {
       xmlj = finds(brr, xml_finds, l);
       int mapj[] = finds(brr, map_finds, l);
       if (isxml = (xmlj != null && (mapj == null ||  mapj[1] > xmlj[1])))break;
       else xmlj = mapj;
       if (xmlj != null)break;
       System.arraycopy(brr, offlen , brr, 0, off = 15);
      }
      if (xmlj != null) {
       int ki=xmlj[0];
       Charset set = sets[ki];
       int k = xmlj[1],lastPos=0;
       FileOutputStream f=new FileOutputStream(ou);
       FileChannel ch=f.getChannel();
       //这里是BufferedOutputStream对于输入超过缓冲的没有优化
       //避免余剩字节导致的低于4k写出。
       ByteBufIo out=new ByteBufIo(ch, 8192);
       try {
        byte bo[]=bom[ki];
        int pos=bo.length;
        out.write(bo);
        byte[] head=(isxml ?xml_finds: map_finds)[ki].drc;
        byte[] outhead=isxml ?"<?xml".getBytes(set): head;
        pos += outhead.length;
        out.write(outhead);
        k += head.length;
        byte[] finds = "/map".getBytes(set);
        off = finds.length - 1;
        offlen = brr.length - off;
        BMFind bm = new BMFind(finds);
        do{
         len = l - k;
         out.write(brr, k, len);
         int j = bm.indexOf(brr, k, l);
         if (i >= 0)lastPos = pos + j + bm.drc.length - k;
         pos += len;
         k = off;
         System.arraycopy(brr, offlen, brr, 0, off);
        }while((l = readLoop(gz, brr, off)) - off > 0);
        out.flush();
        ch.position(lastPos);
        byte end[]=">".getBytes(set);
        f.write(end);
        ch.truncate(lastPos + end.length);
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
