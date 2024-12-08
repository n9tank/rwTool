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
import java.util.TreeSet;
public class zipunpack implements Runnable {
 File in;
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
 public zipunpack(File in,  UIPost ui) {
  this.in = in;
  this.ui = ui;
 }
 public static void readFullyAt(FileChannel fc, ByteBuffer buf, long pos) throws IOException {
  fc.position(pos);
  fc.read(buf);
 }
 //from jdk
 long zip64pos=-1;
 long zipcenpos=-1;
 long zipcenlen;
 long zipheadoff;
 TreeMap<Integer,Integer> offtree;
 public void findEnd(FileChannel rnio) throws IOException {
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
     short centot=buf.getShort(i + 10);
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
      long zip64pos=nowpos - 12;
      nowpos = buf.getLong(8);
      buf.rewind();
      buf.limit(56);
      readFullyAt(rnio, buf, nowpos);
      if (buf.getInt(0) == 0x06064b50) {
       long centot64=buf.getLong(32);
       long cenlen64 = buf.getLong(40);
       long cenoff64 = buf.getLong(48);
       long cenpos64 = nowpos - cenlen;
       if (!(cenlen != cenlen64 && cenlen != 0xffffffffL ||
           cenoff != cenoff64 && cenoff != 0xffffffffL ||
           centot != centot64 && centot != 0xffff)) {
        this.zip64pos = zip64pos;    
        cenoff = cenoff64;
        cenpos = cenpos64;
        cenlen = cenlen64;
       }
      }
     }
     zipcenlen = cenlen;
     zipcenpos = cenpos;
     zipheadoff = cenpos - cenoff;
    }
   }
  }
 }
 //我不知道为什么ISO_8859_1对于一些字符返回true
 public static boolean canEncode(byte[] cs, int off, int len) {
  len += off;
  for (;off < len;++off)
   if (cs[off] > 0xff)return false;
  return true;
 }
 public long off(long zpos) {
  long cenpos=zipcenpos;
  if (zpos > cenpos) {
   //用于处理特殊注入壳导致无法读取
   int zipos=(int)(zpos - cenpos);
   Map.Entry<Integer, Integer> gen=offtree.lowerEntry(zipos);
   if (gen != null)zpos = zpos + gen.getValue();
  }
  return zpos;
 }
 public ByteBuffer modifyEns(FileChannel rnio) throws IOException {
  long headoff=zipheadoff;
  long cenpos=zipcenpos;
  long cenlen=zipcenlen;
  int time=ZipEntryM.dosTime(System.nanoTime());
  Charset utf8set=StandardCharsets.UTF_8;
  Charset iso=StandardCharsets.ISO_8859_1;
  ByteBuffer buf = ByteBuffer.allocateDirect((int)(cenlen + (cenlen >> 3)));
  buf.order(ByteOrder.LITTLE_ENDIAN);
  rnio.position(cenpos);
  rnio.read(buf);
  int bufrem=buf.position();
  int lastrem=bufrem;
  int off=0;
  HashSet set=new HashSet();
  TreeMap<Integer,Integer> tree=new TreeMap();
  offtree = tree;
  StringBuilder strbuf=new StringBuilder();
  Random ran=new Random();
  while (off <= cenlen) {
   off += 8;
   short gbit=buf.getShort(off);
   boolean utf8=(gbit & 2048) > 0;
   int mod=off += 2;
   int timeIn=off += 2;
   buf.putInt(off += 4, 0);//crc
   off += 4;
   int sizeIn=off;
   off += 4;
   int csize=buf.getInt(off);
   if (csize == 0) {
    //这是仅储存
    buf.putShort(mod, (short)0);
    buf.putInt(off, buf.getInt(sizeIn));
   }
   int namelen=buf.getShort(off += 2) & 0xffff;
   int exlen=buf.getShort(off += 2) & 0xffff;
   int cmlen=buf.getShort(off += 2) & 0xffff;
   long zpos=buf.getInt(off += 8) & 0xffffffffL;
   if (zpos != 0xffffffffL)
    buf.putInt(off, (int)(off(zpos) + headoff));
   off += 4;
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
   int lastoff=bufrem - lastrem;
   bufrem += addlen;
   off += namelen;
   int offlen=lastoff + addlen;
   if (offlen != lastoff)
    tree.put(off - lastoff, offlen);
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
     zpos = buf.getLong(off);
     buf.putLong(off, off(zpos) + headoff);
    }
   }
   off += exlen;
   off += cmlen;
  }
  buf.rewind();
  buf.limit(bufrem);
  return buf;
 }
 public void run() {
  Exception e=null;
  try {
   FileChannel rnio=new RandomAccessFile(in, "rw").getChannel(); 
   try {
    findEnd(rnio);
    long zipcenpos=this.zipcenpos;
    long nowpos=zipcenpos + zipcenlen;
    if (zipcenpos >= 0) {
     ByteBuffer cens=modifyEns(rnio);
     long zip64pos=this.zip64pos;
     ByteBuffer buf= ByteBuffer.allocateDirect(zip64pos >= 0 ?16: 8);
     buf.order(ByteOrder.LITTLE_ENDIAN);
     int censize=cens.limit();
     long offn=off(nowpos);
     if (zip64pos < 0) {
      buf.putInt(censize);
      buf.putInt((int)zipcenpos);
      rnio.position(nowpos + 12);
     } else {
      buf.putLong(offn);
      buf.flip();
      rnio.position(zip64pos);
      rnio.write(buf);
      buf.clear();
      buf.putLong(censize);
      buf.putLong(zipcenpos);
      rnio.position(nowpos + 40);
     }
     buf.flip();
     rnio.write(buf);
     rnio.position(nowpos);
     rnio.transferTo(offn, rnio.size() - nowpos, rnio);
     rnio.position(zipcenpos);
     rnio.write(buf);
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
