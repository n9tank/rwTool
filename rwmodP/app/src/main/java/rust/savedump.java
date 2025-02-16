package rust;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import me.steinborn.libdeflate.Libdeflate;
import me.steinborn.libdeflate.LibdeflateDecompressor;
import org.libDeflate.BufOutput;
import org.libDeflate.ByteBufIo;
import org.libDeflate.UIPost;
import org.libDeflate.RC;

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
 public static long findsMin(ByteBuffer buf, BMFind finds[], int end) {
  int k=Integer.MAX_VALUE,v=-1;
  for (int i=0,size=finds.length;i < size;++i) {
   int j=finds[i].indexOf(buf, 0, end);
   if (j < k) {
    k = j;
    v = i;
   }
  }
  if (v >= 0)return (v << 32) | k;
  return -1;
 }
 public static ByteBuffer readLoop(LibdeflateDecompressor def, ByteBuffer src, ByteBuffer drc) throws IOException {
  boolean isfisrt=true;
  while (src.hasRemaining() && drc.hasRemaining()) {
   int size=def.decompress(src, drc);
   if (size <= 0) {
    if (size == 0)
     return drc;
    else if (isfisrt) {
     drc = BufOutput.copy(drc, ((drc.capacity() - 15) << 1) + 15);
     continue;
    } else break;
   }
   isfisrt = false;
  }
  return drc;
 }
 public void run() {
  Throwable ex=null;
  try {
   FileChannel io=FileChannel.open(in.toPath(), StandardOpenOption.READ);
   try {
    ByteBuffer mmap=io.map(FileChannel.MapMode.READ_ONLY, 0, io.size());
    int i=0;
    //考虑失败情况实际上用字节比更快，不过没必要优化，这是为了编译大小
    //（ZipFile也有个类似的扫描）
    short tag;
    do{
     tag = mmap.getShort(i++);
    }while (tag != 0x1f8b);
    mmap.position(--i);
    mmap.limit(i + mmap.getInt(i - 4));
    LibdeflateDecompressor def=new LibdeflateDecompressor(Libdeflate.GZIP);
    try {
     ByteBuffer buf=ByteBuffer.allocate(8192 + (4 * 3) + 3);
     int l=0;
     int k;
     boolean isxml=false;
     long xmlj=-1;
     do {
      k = buf.position();
      buf = readLoop(def, mmap, buf);
      l = buf.position();
      xmlj = findsMin(buf, xml_finds, l);
      long mapj= findsMin(buf, map_finds, l);
      if (isxml = (xmlj != -1 && mapj == -1 ||  ((int)mapj) > ((int)xmlj)))break;
      else xmlj = mapj;
      if (xmlj != -1)
       break;
      if (l > 15) {
       buf.position(l - 15);
       buf.limit(l);
       buf.compact();
       buf.clear();
       buf.position(15);
      }
     }while(l > k);
     if (xmlj != -1) {
      int ki=(int)(xmlj >> 32);
      Charset set = sets[ki];
      k = (int)xmlj;
      int lastPos=0;
      FileChannel ch=FileChannel.open(ou.toPath(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
      ByteBufIo out=new ByteBufIo(ch, RC.IOSIZE);
      try {
       if (isxml) {
        out.write(set.encode("<?xml"));
        k += xml_finds[ki].drc.length;
        i = k;
       } else i = k + map_finds[ki].drc.length;
       int pos=out.buf.position();
       do {
        buf.limit(l);
        buf.position(k);
        out.write(buf);
        BMFind[] finds=map_end;
        int fk=-1;
        for (int fi=0,flen=finds.length;fi < flen;++fi) {
         int j = finds[fi].lastIndexOf(buf, i, l);
         if (j > fk) {
          fk = j;
          set = sets[fi];
         }
        }
        i = 0;
        if (fk != -1)lastPos = pos + fk - k;
        pos += l - k;
        if (l > 15) {
         buf.position(l - 15);
         buf.limit(l);
         buf.compact();
         buf.clear();
         buf.position(15);
        }
        k = buf.position();
        buf = readLoop(def, mmap, buf);
        l = buf.position();
       }while(l > k);
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
     def.close();
    }
   } finally {
    io.close();
   }
  } catch (Throwable e) {
   ex = e;
  }
  if (ex != null)ou.delete();
  ui.accept(UiHandler.toList(ex));
 }
}
