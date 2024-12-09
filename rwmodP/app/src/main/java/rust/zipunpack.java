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
import java.util.Random;
import java.util.TreeSet;
import org.libDeflate.UIPost;
import org.libDeflate.ZipEntryM;
import rust.UiHandler;
import org.libDeflate.BufOutput;

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
    char c=(char)(ran.nextInt(93) + 33);
    if (c == '-')++c;
    buf.append(c);
   }
  }
  return new name(name, conts);
 }
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
 //from jdk
 long zip64pos=-1;
 long zipcenpos=-1;
 long zipcenlen;
 long zipheadoff;
 TreeSet offtree;
 public ByteBuffer findEnd(FileChannel rnio) throws IOException {
  ByteBuffer buf = ByteBuffer.allocateDirect(132);
  buf.order(ByteOrder.LITTLE_ENDIAN);
  long ziplen=rnio.size();
  long minHDR = (ziplen - 65557) > 0 ? ziplen - 65557 : 0;
  long minPos = minHDR - 106;
  for (long pos = ziplen - 128; pos >= minPos; pos -= 106) {
   int off = pos < 0 ?(int)-pos: 0;
   buf.position(off);
   readFullyAt(rnio, buf, off, 128, pos + off);
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
      readFullyAt(rnio, buf, 128, 132, cenpos);
      if (buf.getInt(128) != 0x02014b50)continue;
      readFullyAt(rnio, buf, 128, 132, headoff);
      if (buf.getInt(128) != 0x04034b50)continue;
     }
     readFullyAt(rnio, buf, 0, 20, nowpos - 20);
     if (buf.getInt(0) == 0x07064b50) {
      long zip64pos=nowpos - 12;
      nowpos = buf.getLong(8);
      readFullyAt(rnio, buf, 0, 56, nowpos);
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
     return buf;
    }
   }
  }
  return buf;
 }
 //我不知道为什么ISO_8859_1对于一些字符返回true
 public static boolean canEncode(byte[] cs, int len) {
  for (int i=0;i < len;++i)
   if (cs[i] > 0xff)return false;
  return true;
 }
 public long off(long zpos) {
  long cenpos=zipcenpos;
  if (zpos > cenpos) {
   //用于处理特殊注入壳导致无法读取
   intkv gen=(intkv)offtree.lower(new intkv((int)(zpos - cenpos) , 0));
   if (gen != null)zpos = zpos + gen.v;
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
  ByteBuffer buf = ByteBuffer.allocateDirect((int)(cenlen + (cenlen >> 4)));
  buf.order(ByteOrder.LITTLE_ENDIAN);
  int bufrem=(int)cenlen;
  buf.limit(bufrem);
  rnio.position(cenpos);
  rnio.read(buf);
  buf.clear();
  int lastrem=bufrem;
  int off=0;
  HashSet set=new HashSet();
  TreeSet<intkv> tree=new TreeSet();
  offtree = tree;
  StringBuilder strbuf=new StringBuilder();
  Random ran=new Random();
  byte[] brr=null;
  while (off + 46 <= cenlen) {
   off += 8;
   short gbit=buf.getShort(off);
   boolean utf8=(gbit & 2048) > 0;
   int mod=off += 2;
   int timeIn=off += 2;
   buf.putInt(off += 4, 0);//crc
   int sizeIn=off += 4;
   int csize=buf.getInt(off += 4);
   if (csize == 0) {
    //这是仅储存
    buf.putShort(mod, (short)0);
    buf.putInt(off, buf.getInt(sizeIn));
   }
   int nameIn=off += 4;
   int namelen=buf.getShort(nameIn) & 0xffff;
   int exlen=buf.getShort(off += 2) & 0xffff;
   int cmlen=buf.getShort(off += 2) & 0xffff;
   off += 2;
   long zpos=buf.getInt(off += 8) & 0xffffffffL;
   if (zpos != 0xffffffffL)
    buf.putInt(off, (int)(off(zpos) + headoff));
   off += 4;
   buf.position(off);
   if (brr == null || brr.length < namelen)
    brr = new byte[BufOutput.tableSizeFor(namelen)];
   buf.get(brr, 0, namelen);
   utf8 &= !canEncode(brr, namelen);
   Charset code=utf8 ?utf8set: iso;
   String str=new String(brr, code);
   zipunpack.name type=getName(str, set, strbuf, ran);
   if (type.conts)buf.putInt(timeIn, time);
   ByteBuffer nameBuf = code.encode(type.name);
   int nlen=nameBuf.limit();
   buf.putShort(nameIn, (short)nlen);
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
    if (addlen > 0) {
     buf.position(fpos);
     buf.put(nameBuf);
    }
   }
   int lastoff=bufrem - lastrem;
   bufrem += addlen;
   off += namelen;
   int offlen=lastoff + addlen;
   if (offlen != lastoff)
    tree.add(new intkv(off - lastoff - 1, offlen));
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
    ByteBuffer buf= findEnd(rnio);
    long zipcenpos=this.zipcenpos;
    long nowpos=zipcenpos + zipcenlen;
    if (zipcenpos >= 0) {
     ByteBuffer cens=modifyEns(rnio);
     int censize=cens.limit();
     long zip64pos=this.zip64pos;
     long offn=off(nowpos);
     if (zip64pos < 0) {
      buf.putInt(censize);
      buf.putInt((int)zipcenpos);
      rnio.position(nowpos + 12);
     } else {
      if (censize != zipcenlen) {
       buf.putLong(offn);
       buf.flip();
       rnio.position(zip64pos);
       rnio.write(buf);
       buf.clear();
      }
      buf.putLong(censize);
      buf.putLong(zipcenpos);
      rnio.position(nowpos + 40);
     }
     buf.flip();
     rnio.write(buf);
     if (censize != zipcenlen) {
      rnio.position(offn);
      long tsize=rnio.size() - nowpos;
      if (tsize > 0) {
       rnio.transferTo(nowpos, tsize , rnio);
      }
     }
     rnio.position(zipcenpos);
     rnio.write(cens);
     rnio.truncate(rnio.size() + censize - zipcenlen);
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
