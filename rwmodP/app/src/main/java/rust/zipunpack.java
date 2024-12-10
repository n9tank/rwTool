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
    char c=(char)(ran.nextInt(93) + 33);
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
 public static void readFullyAt(FileChannel fc, ByteBuffer buf, int off, int end, int pos) throws IOException {
  buf.position(off);
  buf.limit(end);
  fc.position(pos);
  fc.read(buf);
  buf.clear();
 }
 //from jdk
 int zipcenpos=-1;
 int zipendpos;
 int zipcenlen;
 int zipheadoff;
 TreeSet offtree;
 public void findEnd(FileChannel rnio) throws IOException {
  ByteBuffer buf = ByteBuffer.allocateDirect(132);
  buf.order(ByteOrder.LITTLE_ENDIAN);
  int ziplen=(int)rnio.size();
  int minHDR = (ziplen - 65557) > 0 ? ziplen - 65557 : 0;
  int minPos = minHDR - 106;
  for (int pos = ziplen - 128; pos >= minPos; pos -= 106) {
   int off = pos < 0 ?-pos: 0;
   buf.position(off);
   readFullyAt(rnio, buf, off, 128, pos + off);
   for (int i = 106; i >= off; i--) {
    if (buf.getInt(i) == 0x06054b50) {
     int centot=buf.getShort(i + 10) & 0xffff;
     int cenlen=buf.getInt(i + 12);
     int cenoff=buf.getInt(i + 16);
     int nowpos=pos + i;
     zipendpos = nowpos;
     int cenpos=nowpos - cenlen;
     int headoff=cenpos - cenoff;
     int comlen = buf.getShort(i + 20) & 0xffff;
     if (nowpos + 22 + comlen != ziplen) {
      if (cenpos < 0 || headoff < 0)continue;
      readFullyAt(rnio, buf, 128, 132, cenpos);
      if (buf.getInt(128) != 0x02014b50)continue;
      readFullyAt(rnio, buf, 128, 132, headoff);
      if (buf.getInt(128) != 0x04034b50)continue;
     }
     if (cenlen == 0xffffffff || cenoff == 0xffffffff || centot == 0xffff) {
      readFullyAt(rnio, buf, 0, 16, nowpos - 20);
      if (buf.getInt(0) == 0x07064b50) {
       nowpos = (int) buf.getLong(8);
       readFullyAt(rnio, buf, 0, 56, nowpos);
       if (buf.getInt(0) == 0x06064b50) {
        cenlen = (int)buf.getLong(40);
        cenoff = (int)buf.getLong(48);
        cenpos = nowpos - cenlen;
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
 int tsizeoff;
 public long off(long zpos) {
  if (zpos > zipcenpos + zipcenlen)
   return zpos -= zipcenlen;
  if (zpos > zipcenpos) {
   //用于处理特殊注入壳导致无法读取
   intkv gen=(intkv)offtree.lower(new intkv((int)(zpos - zipcenpos) , 0));
   if (gen != null)zpos = zpos + gen.v + tsizeoff;
  }
  return zpos;
 }
 public static ByteBuffer copy(ByteBuffer buf, ByteBuffer drc, int start, int end) {
  int len=end - start;
  if (drc.remaining() < len) {
   int cy=drc.capacity();
   drc = BufOutput.copy(drc, Math.max(len, cy + (cy >> 1)));
  }
  buf.position(start);
  buf.limit(end);
  drc.put(buf);
  buf.clear();
  return drc;
 }
 public ByteBuffer modifyEns(FileChannel rnio) throws IOException {
  int headoff=zipheadoff;
  int cenpos=zipcenpos;
  int cenlen=zipcenlen;
  int time=ZipEntryM.dosTime(System.nanoTime());
  Charset utf8set=StandardCharsets.UTF_8;
  Charset iso=StandardCharsets.ISO_8859_1;
  CharsetEncoder utf8seten=utf8set.newEncoder();
  CharsetEncoder isoen=iso.newEncoder();
  ByteBuffer buf = ByteBuffer.allocateDirect(cenlen);
  buf.order(ByteOrder.LITTLE_ENDIAN);
  ByteBuffer drc=ByteBuffer.allocateDirect(cenlen + (cenlen >> 4) + 22);
  drc.order(ByteOrder.LITTLE_ENDIAN);
  buf.limit(cenlen);
  rnio.position(cenpos);
  rnio.read(buf);
  buf.clear();
  int addall=0;
  int off=0;
  int lastoff=0;
  HashSet set=new HashSet();
  TreeSet<intkv> tree=new TreeSet();
  offtree = tree;
  StringBuilder strbuf=new StringBuilder();
  Random ran=new Random();
  byte[] brr=null;
  int censub=cenlen - 46;
  while (off <= censub) {
   off += 8;
   short gbit=buf.getShort(off);
   boolean utf8=(gbit & 2048) > 0;
   int mod=off += 2;
   boolean deflate=buf.getShort(mod) != 0;
   int timeIn=off += 2;
   buf.putInt(off += 4, 0);//crc
   int sizeIn=off += 4;
   int size=buf.getInt(sizeIn);
   long csize=buf.getInt(off += 4) & 0xffffffffL;
   if (csize == 0) {
    //这是仅储存
    deflate = false;
    buf.putShort(mod, (short)0);
   }
   off += 4;
   int nameIndrc=off + addall;
   int namelen=buf.getShort(off) & 0xffff;
   int exlen=buf.getShort(off += 2) & 0xffff;
   int cmlen=buf.getShort(off += 2) & 0xffff;
   off += 2;
   int zpos32=buf.getInt(off += 8);
   if (zpos32 != 0xffffffff)
    buf.putInt(off, (int)(off(zpos32) + headoff));
   off += 4;
   boolean emptyfile=deflate ?csize <= 2: size == 0;
   drc = copy(buf, drc, lastoff, emptyfile ?off + namelen: off);
   buf.position(off);
   if (brr == null || brr.length < namelen)
    brr = new byte[BufOutput.tableSizeFor(namelen)];
   buf.get(brr, 0, namelen);
   Charset code=utf8 ?utf8set: iso;
   String str=new String(brr, 0, namelen, code);
   int addlen;
   /*
    不要丢弃空文件，理论上注释以及扩展字段也能塞条目
    将条目塞在名称上逻辑会罢工。
    */
   if (!emptyfile) {
    zipunpack.name type=getName(str, set, strbuf, ran);
    if (type.conts)buf.putInt(timeIn, time);
    CharBuffer nstr=CharBuffer.wrap(type.name);
    CharsetEncoder codeen=utf8 ?utf8seten: isoen;
    int pos=drc.position();
    int cy=drc.capacity();
    while (codeen.encode(nstr, drc, true).isOverflow())
     drc = BufOutput.copy(drc, cy + (cy >> 1));
    while (codeen.flush(drc).isOverflow())
     drc = BufOutput.copy(drc, cy + (cy >> 1));
    codeen.reset();
    addlen = drc.position() - pos - namelen;
    drc.putShort(nameIndrc, (short)(namelen + addlen));
    addall += addlen;
   } else {
    set.add(str);
    addlen = 0;
   }
   off += namelen;
   lastoff = off;
   if (!emptyfile) {
    if (addlen != 0)
     tree.add(new intkv(off - 1, addall)); 
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
      if (zpos32 == 0xffffffff) {
       int zpos = (int)(off(buf.getLong(zip64)) + headoff);
       drc.putInt(nameIndrc + 14, zpos);
      }
     }
    }
   }
   off += exlen;
   off += cmlen;
  }
  drc = copy(buf, drc, lastoff, cenlen);
  return drc;
 }
 public void run() {
  Exception e=null;
  try {
   FileChannel rnio=new RandomAccessFile(in, "rw").getChannel();
   try {
    findEnd(rnio);
    int zipcenpos=this.zipcenpos;
    int nowpos=zipcenpos + zipcenlen;
    if (zipcenpos >= 0) {
     int tsize=(int)(rnio.size() - nowpos);
     tsizeoff = tsize;
     ByteBuffer cens=modifyEns(rnio);
     rnio.position(zipcenpos);
     rnio.transferTo(nowpos, tsize , rnio);
     int cenpos=zipcenpos + tsize;
     int censize=cens.position();
     cens.putInt(0X06054B50);
     cens.position(censize + 12);
     cens.putInt(censize);
     cens.putInt(cenpos);
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
