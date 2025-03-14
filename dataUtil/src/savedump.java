package rust;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import me.steinborn.libdeflate.Libdeflate;
import me.steinborn.libdeflate.LibdeflateDecompressor;
import org.libDeflate.RC;
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
 public void run() {
  Throwable ex=null;
  try {
   FileChannel io=FileChannel.open(in.toPath(), StandardOpenOption.READ);
   ByteBuffer buf=null;
   try {
    ByteBuffer mmap=io.map(FileChannel.MapMode.READ_ONLY, 0, io.size());
    int i=0;
    short tag;
    do{
     tag = mmap.getShort(i++);
    }while (tag != 0x1f8b);
    mmap.position(--i);
    mmap.limit(i + mmap.getInt(i - 4));
    mmap.order(ByteOrder.LITTLE_ENDIAN);
    int gzipsize=mmap.getInt(mmap.limit() - 4);
    buf = RC.newbuf(gzipsize);
    LibdeflateDecompressor def=new LibdeflateDecompressor(Libdeflate.GZIP);
    try {
     int len= def.decompress(mmap, buf);
     if (len < 0)throw new IOException();
    } finally {
     def.close();
    }
   } finally {
    io.close();
   }
   int size=buf.position();
   long xmlj= findsMin(buf, xml_finds, size);
   long mapj= findsMin(buf, map_finds, size);
   boolean isxml = xmlj != -1 && (xmlj < mapj || mapj == -1);
   if (!isxml)xmlj = mapj;
   if (xmlj != -1) {
    int ki=(int)(xmlj >> 32);
    Charset set = sets[ki];
    int start = (int)xmlj;
    FileChannel ch=FileChannel.open(ou.toPath(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
    try {
     int find=start;
     if (isxml) {
      ByteBuffer strbuf=set.encode("<?");
      buf.position(start -= strbuf.limit());
      buf.put(strbuf);
      find += xml_finds[ki].drc.length;
     } else  find += map_finds[ki].drc.length;
     BMFind[] finds=map_end;
     int end=-1;
     for (int i=0,len=finds.length;i < len;++i) {
      int j = finds[i].lastIndexOf(buf, find, size);
      if (j > end) {
       end = j;
       set = sets[i];
      }
     }
     buf.position(end);
     buf.put(set.encode(">"));
     buf.flip();
     buf.position(start);
     ch.write(buf);
     ch.truncate(ch.position());
    } finally {
     ch.close();
    }
   }
  } catch (Throwable e) {
   ex = e;
  }
  if (ex != null)ou.delete();
  ui.accept(UiHandler.toList(ex));
 }
}
