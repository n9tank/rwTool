package rust;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
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
 static BMFind[] xml_finds=bmfind("xml", false);
 static BMFind[] map_finds=bmfind("<map", false);
 static BMFind[] map_end=bmfind("/map", true);
 public savedump(File i, File o, UIPost u) {
  in = i;
  ou = o;
  ui = u;
 }
 public static BMFind[] bmfind(String str, boolean re) {
  int len=sets.length;
  BMFind[] list=new BMFind[len];
  for (int i=0;i < len;++i)
   list[i] = new BMFind(str.getBytes(sets[i]), re);
  return list;
 }
 public static long findsMin(byte arr[], BMFind finds[], int end) {
  int k=Integer.MAX_VALUE,v=-1;
  for (int i=0,size=finds.length;i < size;++i) {
   int j=finds[i].indexOf(arr, 0, end);
   if (j < k) {
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
       FileChannel ch=FileChannel.open(ou.toPath(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
       //这里是BufferedOutputStream对于输入超过缓冲的没有优化
       //避免余剩字节导致的低于4k写出。
       ByteBufIo out=new ByteBufIo(ch, 8192);
       try {
        if (isxml) {
         out.write(set.encode("<?xml"));
         k += xml_finds[ki].drc.length;
         i = k;
        } else i = k + map_finds[ki].drc.length;
        int pos=out.buf.position();
        ByteBuffer buf=ByteBuffer.wrap(brr);
        for (;;) {
         buf.limit(l);
         buf.position(k);
         out.write(buf);
         BMFind[] finds=map_end;
         int fk=-1;
         for (int fi=0,size=finds.length;fi < size;++fi) {
          int j = finds[fi].lastIndexOf(brr, i, l);
          if (j > fk) {
           fk = j;
           set = sets[fi];
          }
         }
         i = 0;
         if (fk != -1)lastPos = pos + fk - k;
         pos += l - k;
         k = off;
         if (l < blen)break;
         System.arraycopy(brr, offlen , brr, 0, off);
         l = readLoop(gz, brr, off);
        }
        int offpos=(int)(lastPos - ch.position());
        WritableByteChannel f;
        if (offpos < 0) {
         ch.position(lastPos);
         out.buf = null;
         f = ch;
        } else {
         out.buf.position(offpos);
         f = out;
        }
        f.write(set.encode(">"));
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
