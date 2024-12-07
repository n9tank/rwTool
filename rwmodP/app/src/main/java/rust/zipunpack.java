package rust;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Random;
import java.util.TreeSet;
import org.libDeflate.BufOutput;
import org.libDeflate.UIPost;
import org.libDeflate.ZipEntryM;
import rust.UiHandler;

public class zipunpack implements Runnable {
 public static class intkv implements Comparable {
  public int compareTo(Object o) {
   return this.k - ((intkv)o).k;
  }
  int k;
  int v;
  public intkv(int k, int v) {
   this.k = k;
   this.v = v;
  }
 }
 File in;
 public static class name {
  String name;
  boolean conts;
  public name(String str, boolean con) {
   name = str;
   conts = con;
  } 
 }
 public static int toName(String name) {
  int i=name.length();
  if (i == 0)return 0;
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
    char c=(char)(ran.nextInt(94) + 33);
    if (c == '-')++c;
    buf.append(c);
   }
  }
  return new name(name, conts);
 }
 UIPost ui;
 public zipunpack(File in, UIPost ui) {
  this.in = in;
  this.ui = ui;
 }
 public static void readFullyAt(FileChannel fc, ByteBuffer buf, int off, int end, long pos) throws IOException {
  buf.position(off);
  buf.limit(end);
  fc.position(pos);
  fc.read(buf);
  buf.clear();
 }
 long zipcenpos=-1;
 long zipendpos;
 long zipcenlen;
 long zipheadoff;
 int cenlenoff=22;
 TreeSet offtree;
 public void findEnd(FileChannel rnio) throws IOException {
  ByteBuffer buf = ByteBuffer.allocateDirect(132);
  buf.order(ByteOrder.LITTLE_ENDIAN);
  long ziplen=(int)rnio.size();
  long minHDR = (ziplen - 65557) > 0 ? ziplen - 65557 : 0;
  long minPos = minHDR - 106;
  for (long pos = ziplen - 128; pos >= minPos; pos -= 106) {
   int off = pos < 0 ?(int)-pos: 0;
   buf.position(off);
   readFullyAt(rnio, buf, off, 128, pos + off);
   for (int i = 106; i >= off; i--) {
    if (buf.getInt(i) == 0x06054b50) {
     int centot=buf.getShort(i + 10) & 0xffff;
     long cenlen=buf.getInt(i + 12) & 0xffffffffL;
     long cenoff=buf.getInt(i + 16) & 0xffffffffL;
     long nowpos=pos + i;
     zipendpos = nowpos;
     long cenpos=nowpos - cenlen;
     long headoff=cenpos - cenoff;
     int comlen = buf.getShort(i + 20) & 0xffff;
     if (nowpos + 22 + comlen != ziplen) {
      if (cenpos < 0 || headoff < 0)continue;
      readFullyAt(rnio, buf, 128, 132, cenpos);
      if (buf.getInt(128) != 0x02014b50)continue;
      readFullyAt(rnio, buf, 128, 132, headoff);
      if (buf.getInt(128) != 0x04034b50)continue;
     }
     if (cenlen == 0xffffffffL || cenoff == 0xffffffffL || centot == 0xffff) {
      readFullyAt(rnio, buf, 0, 16, nowpos - 20);
      if (buf.getInt(0) == 0x07064b50) {
       int nextpos = (int) buf.getLong(8);
       readFullyAt(rnio, buf, 0, 56, nextpos);
       if (buf.getInt(0) == 0x06064b50) {
        cenlen = buf.getLong(40);
        cenoff = buf.getLong(48);
        cenpos = nextpos - cenlen;
        cenlenoff = nextpos == nowpos - 76 ?98: 56;
       }
      }
     }
     zipcenlen = cenlen;
     zipcenpos = cenpos;
     zipheadoff = cenpos - cenoff;
     return;
    }
   }
  }
  return;
 }
 long tsizeoff;
 public long off(long zpos) {
  if (zpos > zipcenpos + zipcenlen)
   return zpos -= zipcenlen;
  if (zpos > zipcenpos) {
   //用于处理特殊注入壳导致无法读取
   intkv gen=(intkv)offtree.lower(new intkv((int)(zpos - zipcenpos) , 0));
   if (gen != null)zpos += gen.v;
   zpos += tsizeoff;
  }
  return zpos;
 }
 public static void copy(ByteBuffer buf, ByteBuffer drc, int start, int end) {
  buf.position(start);
  buf.limit(end);
  drc.put(buf);
  buf.clear();
 }
 public ByteBuffer modifyEns(FileChannel rnio) throws IOException {
  long headoff=zipheadoff;
  long cenpos=zipcenpos;
  int cenlen=(int)zipcenlen;
  int time=ZipEntryM.dosTime(System.nanoTime());
  Charset utf8set=StandardCharsets.UTF_8;
  Charset iso=StandardCharsets.ISO_8859_1;
  CharsetEncoder utf8seten=utf8set.newEncoder();
  CharsetEncoder isoen=iso.newEncoder();
  ByteBuffer buf = ByteBuffer.allocateDirect((cenlen >> 2) + cenlen + 22);
  //每个条目扩张11b，完全足够不考虑扩容实现了。
  buf.order(ByteOrder.LITTLE_ENDIAN);
  ByteBuffer drc=buf.duplicate();
  int off=buf.capacity() - cenlen;
  buf.position(off);
  rnio.position(cenpos);
  rnio.read(buf);
  int addall=0;
  int lastoff=off;
  int drcoff=-off;
  HashSet set=new HashSet();
  TreeSet<intkv> tree=new TreeSet();
  offtree = tree;
  StringBuilder strbuf=new StringBuilder();
  Random ran=new Random();
  byte[] brr=null;
  int censub=buf.capacity() - 46;
  while (off <= censub) {
   off += 8;
   short gbit=buf.getShort(off);
   boolean utf8=(gbit & 2048) > 0;
   int mod=off += 2;
   int timeIn=off += 2;
   buf.putInt(off += 4, 0);//crc
   int csize=buf.getInt(off += 4);
   if (csize == 0)buf.putShort(mod, (short)0);
   off += 8;
   int nameIndrc=off + drcoff + addall;
   int namelen=buf.getShort(off) & 0xffff;
   int exlen=buf.getShort(off += 2) & 0xffff;
   int cmlen=buf.getShort(off += 2) & 0xffff;
   off += 2;
   long zpos32=buf.getInt(off += 8) & 0xffffffffL;
   buf.putInt(off, (int)(off(zpos32) + headoff));
   off += 4;
   buf.position(off);
   if (brr == null || brr.length < namelen)
    brr = new byte[BufOutput.tableSizeFor(namelen)];
   buf.get(brr, 0, namelen);
   Charset code=utf8 ?utf8set: iso;
   String str=new String(brr, 0, namelen, code);
   int addlen;
   zipunpack.name type=getName(str, set, strbuf, ran);
   if (type.conts)buf.putInt(timeIn, time);
   copy(buf, drc, lastoff, off);
   CharBuffer nstr=CharBuffer.wrap(type.name);
   CharsetEncoder codeen=utf8 ?utf8seten: isoen;
   int pos=drc.position();
   codeen.encode(nstr, drc, true);
   codeen.flush(drc);
   codeen.reset();
   addlen = drc.position() - pos - namelen;
   drc.putShort(nameIndrc, (short)(namelen + addlen));
   addall += addlen;
   off += namelen;
   lastoff = off;
   if (addlen != 0)
    tree.add(new intkv(off + drcoff - 1, addall));
   int zip64=off;
   int exoff=exlen + off;
   while (zip64 + 4 < exoff) {
    short ztag=buf.getShort(zip64);
    int sz=buf.getShort(zip64 + 2) & 0xffff;
    zip64 += 4;
    if (zip64 + sz > exoff)break;
    if (ztag == 0x0001) {
     zip64 += 16;
     sz -= 16;
     if (sz < 8 || (zip64 + 8) > exoff)break;
     if (zpos32 == 0xffffffffL) {
      int zpos = (int)(off(buf.getLong(zip64)) + headoff);
      drc.putInt(nameIndrc + 14, zpos);
     }
    }
   }
   off += exlen;
   off += cmlen;
  }
  copy(buf, drc, lastoff, off);
  return drc;
 }
 public void run() {
  Exception e=null;
  try {
   FileChannel rnio=new RandomAccessFile(in, "rw").getChannel();
   try {
    findEnd(rnio);
    long zipcenpos=this.zipcenpos;
    long nowpos=zipcenpos + zipcenlen + cenlenoff;
    if (zipcenpos >= 0) {
     long tsize=rnio.size() - nowpos;
     tsizeoff = tsize;
     ByteBuffer cens=modifyEns(rnio);
     rnio.position(zipcenpos);
     rnio.transferTo(nowpos, tsize , rnio);
     long cenpos=zipcenpos + tsize;
     int censize=cens.position();
     cens.putInt(0X06054B50);
     cens.position(censize + 12);
     cens.putInt(censize);
     cens.putInt((int)cenpos);
     cens.position(censize + 22);
     cens.flip();
     rnio.position(cenpos);
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
