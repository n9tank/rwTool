package rust;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import org.libDeflate.UIPost;
import org.libDeflate.ZipEntryM;
public class zipunpack implements Runnable {
 File in;
 File ou;
 UIPost ui;
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
 public zipunpack(File in, File ou, UIPost ui) {
  this.in = in;
  this.ou = ou;
  this.ui = ui;
 }
 public static void readFullyAt(FileChannel fc, ByteBuffer buf, long pos) throws IOException {
  fc.position(pos);
  fc.read(buf);
 }
 //from jdk
 public static long[] findEnd(FileChannel rnio) throws IOException {
  ByteBuffer head=ByteBuffer.allocate(4);
  head.order(ByteOrder.LITTLE_ENDIAN);
  ByteBuffer buf = ByteBuffer.allocateDirect(128);
  buf.order(ByteOrder.LITTLE_ENDIAN);
  long ziplen=rnio.size();
  long minHDR = (ziplen - 65557) > 0 ? ziplen - 65557 : 0;
  long minPos = minHDR - 106;
  for (long pos = ziplen - 128; pos >= minPos; pos -= 106) {
   int off = pos < 0 ?(int)-pos: 0;
   buf.position(off);
   readFullyAt(rnio, buf, pos + off);
   for (int i = 106; i >= off; i--) {
    if (buf.getInt(i) == 0x06054b50) {
     long cenlen=buf.getInt(i + 12) & 0xffffffffL;
     long cenoff=buf.getInt(i + 16) & 0xffffffffL;
     long nowpos=pos + i;
     long cenpos=nowpos - cenlen;
     long headoff=cenpos - cenoff;
     int comlen = buf.getShort(i + 20) & 0xffff;
     if (nowpos + 22 + comlen != ziplen) {
      if (cenpos < 0 || headoff < 0)continue;
      boolean match;
      readFullyAt(rnio, head, cenpos);
      if (match = head.getInt(0) == 0x02014b50) {
       head.clear();
       readFullyAt(rnio, head, cenpos);
       match = head.getInt(0) == 0x04034b50;
      }
      head.clear();
      if (!match)continue;
     }
     buf.rewind();
     buf.limit(20);
     readFullyAt(rnio, buf, nowpos - 20);
     if (buf.getInt(0) == 0x07064b50) {
      nowpos = buf.getLong(8);
      buf.rewind();
      buf.limit(56);
      readFullyAt(rnio, buf, nowpos);
      if (buf.getInt(0) == 0x06064b50) {
       cenlen = buf.getLong(40);
       cenoff = buf.getLong(48);
       cenpos = nowpos - cenlen;
      }
     }
     return new long[]{cenoff,cenpos,cenlen};
    }
   }
  }
  return null;
 }
 //我不知道为什么ISO_8859_1对于一些字符返回true
 public static boolean canEncode(byte[] cs, int off, int len) {
  len += off;
  for (;off < len;++off)
   if (cs[off] > 0xff)return false;
  return true;
 }
 public static void modifyEns(FileChannel rnio, long pos[]) throws IOException {
  long cenoff=pos[0];
  long cenpos=pos[1];
  long cenlen=pos[2];
  int time=ZipEntryM.dosTime(System.nanoTime());
  Charset utf8set=StandardCharsets.UTF_8;
  Charset iso=StandardCharsets.ISO_8859_1;
  ByteBuffer buf = ByteBuffer.allocateDirect((int)(cenlen * 1.1));
  buf.order(ByteOrder.LITTLE_ENDIAN);
  rnio.position(cenpos);
  rnio.read(buf);
  int bufrem=buf.position();
  int lastrem=bufrem;
  int off=0;
  HashSet set=new HashSet();
  TreeMap<Integer,Integer> tree=new TreeMap();
  StringBuilder strbuf=new StringBuilder();
  Random ran=new Random();
  while (off <= cenlen) {
   off += 8;
   short gbit=buf.getShort(off);
   boolean utf8=(gbit & 2048) > 0;
   int mod=off;
   int timeIn=off += 2;
   buf.putInt(off += 4, 0);//crc
   int csizeIn=off += 4;
   int csize=buf.getInt(csizeIn);
   off += 4;//size
   if (csize == 0) {
    //这是仅储存
    buf.putShort(mod, (short)0);
    buf.putInt(csizeIn, buf.getInt(off));
   }
   int namelen=buf.getShort(off += 2) & 0xffff;
   int exlen=buf.getShort(off += 2) & 0xffff;
   int cmlen=buf.getShort(off += 2) & 0xffff;
   off += 12;
   byte[] brr=new byte[namelen];
   buf.position(off);
   buf.get(brr);
   utf8 &= !canEncode(brr, off, namelen);
   Charset code=utf8 ?utf8set: iso;
   String str=new String(brr, off, namelen, code);
   zipunpack.name type=getName(str, set, strbuf, ran);
   if (type.conts)buf.putInt(timeIn, time);
   ByteBuffer nameBuf = code.encode(type.name);
   int nlen=nameBuf.limit();
   nameBuf.limit(Math.min(nlen, namelen));
   buf.position(off);
   buf.put(nameBuf);
   int fpos=buf.position();
   int addlen=nlen - namelen;
   if (addlen > 0) {
    int cy=buf.capacity();
    int nsize=bufrem + addlen;
    if (cy < nsize) {
     if (fpos >= 8192) {
      buf.rewind();
      fpos &= -4096;
      buf.limit(fpos);
      rnio.write(buf);
      bufrem -= fpos;
      buf.position(buf.position() - addlen);
      buf.limit(bufrem);
      buf.put(nameBuf);
      buf.compact();
      nameBuf = null;
     } else {
      buf.flip();
      ByteBuffer newbuf = ByteBuffer.allocateDirect(Math.max(nsize, (cy << 2) - (cy >> 1)));
      newbuf.put(buf);
      buf.limit(bufrem);
      newbuf.put(nameBuf);
      newbuf.put(buf);
      buf = newbuf;
      nameBuf = null;
     }
    }
   }
   if (nameBuf != null) {
    ByteBuffer copy= buf.duplicate();
    copy.position(off + namelen);
    copy.limit(bufrem);
    if (addlen > 0)
     buf.position(fpos + addlen);
    buf.put(copy);
    buf.position(fpos);
    buf.put(nameBuf);
   }
   bufrem += addlen;
   off += namelen;
   int offlen=bufrem - lastrem;
   if (offlen != 0)
    tree.put(off + Math.min(0, addlen), offlen);
   off += addlen;
   int zip64=off;
   int exoff=exlen + off;
   while (zip64 + 4 < exoff) {
    short ztag=buf.getShort(zip64);
    int sz=buf.getShort(zip64 + 2) & 0xffff;
    zip64 += 4;
    if (zip64 + sz > exoff)break;
    if (ztag == 0x0001) {
     off += 16;
     sz -= 16;
     if (sz < 8 || (zip64 + 8) > exoff)break;
     long zpos=buf.getLong(off);
     if (zpos > cenpos) {
      int zipos=(int)(zpos - cenpos);
      Map.Entry<Integer, Integer> gen=tree.lowerEntry(zipos);
      if (gen != null)buf.putLong(off, zpos + gen.getValue());
     }
    }
   }
   off += exlen;
   off += cmlen;
  }
  buf.rewind();
  buf.limit(bufrem);
  rnio.write(buf);
  rnio.truncate(rnio.size() - cenpos + cenoff + bufrem - lastrem);
 }
 public void run() {
  Exception e=null;
  try {
   FileChannel rnio=new RandomAccessFile(in, "rw").getChannel();
   try {
    long pos[]=findEnd(rnio);
    long cenoff=pos[0];
    long cenpos=pos[1];
    long headOff=cenpos - cenoff;
    if (headOff > 0) {
     rnio.position(headOff);
     rnio.transferTo(0, cenoff, rnio);
    }
    rnio.position(cenoff);
    modifyEns(rnio, pos);
   } finally {
    rnio.close();
   }
  } catch (Exception ex) {
   e = ex;
  }
  ui.accept(UiHandler.toList(e));
 }
}
