package rust;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import org.libDeflate.RC;
import org.libDeflate.UIPost;
import org.libDeflate.ZipEntryM;
import org.libDeflate.zipFile;
import rust.UiHandler;

public class zipunpack implements Runnable {
 File in;
 File mmap;
 public static class name {
  Object name;
  boolean conts;
  public name(Object str, boolean con) {
   name = str;
   conts = con;
  } 
 }
 public static int toName(ByteBuffer name) {
  int i=name.limit();
  while (--i >= 0 && name.get(i) == '/');
  return ++i;
 }
 public static ByteBuffer appendChar(ByteBuffer buf, byte c) {
  int in=buf.limit();
  int cy=buf.capacity();
  if (cy <= in) {
   ByteBuffer next= buf.allocate(cy + 12);
   next.put(buf);
   next.rewind();
   buf = next;
  }
  buf.limit(++in);
  buf.put(in, c);
  return buf;
 }
 public static name getName(ByteBuffer name, HashSet set, Random ran) {
  boolean conts=false;
  int i=toName(name);
  name.limit(i);
  while (!set.add(name)) {
   conts = true; 
   byte c=(byte)(ran.nextInt(92) + 33);
   if (c == '/' | c == '\\')++c;
   name = appendChar(name, c);
  }
  return new name(name, conts);
 }
 UIPost ui;
 public zipunpack(File in, UIPost ui) {
  this.in = in;
  this.ui = ui;
 }
 public static final long off(TreeMap offtree, long zipcenpos, long zipcenlen, long tsizeoff, long zpos) {
  if (zpos > zipcenpos + zipcenlen)
   return zpos -= zipcenlen;
  if (zpos > zipcenpos) {
   //用于处理特殊注入壳导致无法读取
   Map.Entry<Integer,Integer> gen=offtree.lowerEntry((int)(zpos - zipcenpos));
   if (gen != null)zpos += gen.getValue();
   zpos += tsizeoff;
  }
  return zpos;
 }
 public static ByteBuffer modifyEns(long headoff, long cenpos, int cenlen, long tsize, FileChannel rnio) throws IOException {
  int time=ZipEntryM.dosTime(System.nanoTime());
  ByteBuffer buf = RC.newDbuf((cenlen >> 2) + cenlen + 22);
  //每个条目扩张11b，完全足够不考虑扩容实现了。
  buf.order(ByteOrder.LITTLE_ENDIAN);
  ByteBuffer drc=buf.duplicate();
  drc.order(ByteOrder.LITTLE_ENDIAN);
  int off=buf.capacity() - cenlen;
  buf.position(off);
  rnio.position(cenpos);
  rnio.read(buf);
  int addall=0;
  int lastoff=off;
  int drcoff=-off;
  HashSet set=new HashSet();
  TreeMap<Integer,Integer> tree=new TreeMap();
  Random ran=new Random();
  int censub=buf.capacity() - 46;
  while (off <= censub) {
   int mod=off += 10;
   int timeIn=off += 2;
   buf.putInt(off += 4, 0);//crc
   int csize=buf.getInt(off += 4);
   if (csize == 0)
    buf.putShort(mod, (short)0);
   int ucsize=buf.get(off += 4); 
   off += 4;
   int nameIndrc=off + drcoff + addall;
   int namelen=buf.getShort(off) & 0xffff;
   int exlen=buf.getShort(off += 2) & 0xffff;
   int cmlen=buf.getShort(off += 2) & 0xffff;
   off += 2;
   long zpos32=buf.getInt(off += 8) & 0xffffffffL;
   buf.putInt(off, (int)(off(tree, cenpos, cenlen, tsize, zpos32 + headoff)));
   off += 4;
   buf.position(off);
   byte name[]=new byte[namelen];
   buf.get(name);
   zipunpack.name type=getName(ByteBuffer.wrap(name), set, ran);
   if (type.conts)buf.putInt(timeIn, time);
   buf.position(lastoff);
   buf.limit(off);
   drc.put(buf);
   buf.clear();
   ByteBuffer nstr=(ByteBuffer)type.name;
   drc.put(nstr);
   int addlen = nstr.limit() - namelen;
   drc.putShort(nameIndrc, (short)(namelen + addlen));
   addall += addlen;
   off += namelen;
   lastoff = off;
   if (addlen != 0)
    tree.put(off + drcoff - 1, addall);
   int zip64=off;
   int exoff=exlen + off;
   while (zip64 + 4 < exoff) {
    short ztag=buf.getShort(zip64);
    int sz=buf.getShort(zip64 + 2) & 0xffff;
    zip64 += 4;
    if (zip64 + sz > exoff)break;
    if (ztag == 0x0001) {
     if (sz < 8)break;
     if (ucsize == 0xffffffff)
      drc.putInt(nameIndrc - 4, (int)buf.getLong(zip64));
     zip64 += 8;
     sz -= 8;
     if (sz < 8)break;
     if (csize == 0xffffffff)
      drc.putInt(nameIndrc - 8, (int)buf.getLong(zip64));
     zip64 += 8;
     sz -= 8;
     if (sz < 8)break;
     if (zpos32 == 0xffffffffL) {
      int zpos = (int)(off(tree, cenpos, cenlen, tsize, buf.getLong(zip64) + headoff));
      drc.putInt(nameIndrc + 14, zpos);
     }
    }
   }
   off += exlen;
   off += cmlen;
  }
  buf.position(lastoff);
  drc.put(buf);
  return drc;
 }
 public void run() {
  Exception e=null;
  try {
   FileChannel rnio=FileChannel.open(in.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE);
   try {
    ByteBuffer buf = ByteBuffer.allocate(132);
    buf.order(ByteOrder.LITTLE_ENDIAN);
    long cenlen=0;
    long cenpos=0;
    long headoff=0;
    int cenlenoff=22;
    tag: {
     long ziplen=rnio.size();
     long minHDR = (ziplen - 65557) > 0 ? ziplen - 65557 : 0;
     long minPos = minHDR - 106;
     for (long pos = ziplen - 128; pos >= minPos; pos -= 106) {
      int off = pos < 0 ?(int)-pos: 0;
      buf.position(off);
      zipFile.readFullyAt(rnio, buf, off, 128, pos + off);
      for (int i = 106; i >= off; i--) {
       if (buf.getInt(i) == 0x06054b50) {
        int centot=buf.getShort(i + 10) & 0xffff;
        cenlen = buf.getInt(i + 12) & 0xffffffffL;
        long cenoff=buf.getInt(i + 16) & 0xffffffffL;
        long nowpos=pos + i;
        cenpos = nowpos - cenlen;
        headoff = cenpos - cenoff;
        int comlen = buf.getShort(i + 20) & 0xffff;
        if (nowpos + 22 + comlen != ziplen) {
         if (cenpos < 0 || headoff < 0)continue;
         zipFile.readFullyAt(rnio, buf, 128, 132, cenpos);
         if (buf.getInt(128) != 0x02014b50)continue;
         zipFile.readFullyAt(rnio, buf, 128, 132, headoff);
         if (buf.getInt(128) != 0x04034b50)continue;
        }
        if (cenlen == 0xffffffffL || cenoff == 0xffffffffL || centot == 0xffff) {
         zipFile.readFullyAt(rnio, buf, 0, 16, nowpos - 20);
         if (buf.getInt(0) == 0x07064b50) {
          int nextpos = (int) buf.getLong(8);
          zipFile.readFullyAt(rnio, buf, 0, 56, nextpos);
          if (buf.getInt(0) == 0x06064b50) {
           cenlen = buf.getLong(40);
           cenoff = buf.getLong(48);
           cenpos = nextpos - cenlen;
           headoff = cenpos - cenoff;
           cenlenoff = nextpos == nowpos - 76 ?98: 56;
          }
         }
        }
        break tag;
       }
      }
     }
     return;
    }
    long nowpos=cenpos + cenlen + cenlenoff;
    if (cenpos >= 0) {
     long tsize=rnio.size() - nowpos;
     ByteBuffer cens=modifyEns(headoff, cenpos, (int)cenlen, tsize, rnio);
     rnio.position(cenpos);
     rnio.transferTo(nowpos, tsize , rnio);
     long loccenpos=cenpos + tsize;
     int censize=cens.position();
     cens.putInt(0X06054B50);
     cens.position(censize + 12);
     cens.putInt(censize);
     cens.putInt((int)loccenpos);
     cens.position(censize + 22);
     cens.flip();
     rnio.position(loccenpos);
     rnio.write(cens);
     rnio.truncate(rnio.position());
    }
   } finally {
    rnio.close();
   }
  } catch (Exception ex) {
   e = ex;
  }
  ui.accept(UiHandler.toList(e));
 }
}
