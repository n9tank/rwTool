package rust;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.file.*;
import java.util.*;
import org.libDeflate.*;
import rust.*;

public class zipunpack implements Runnable {
 File in;
 File mmap;
 public static boolean safe_mode=false;
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
 public static ByteBuffer modifyEns(long headoff, long cenpos, int cenlen, FileChannel rnio) throws IOException {
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
   long zpos32=buf.getInt(off += 10) & 0xffffffffL;
   int zpos=(int) (zpos32 + headoff);
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
   addall+=addlen;
   off += namelen;
   lastoff = off;
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
     if (zpos32 == 0xffffffffL)
      zpos = (int)(buf.getLong(zip64) + headoff);
    }
   }
   drc.putInt(nameIndrc + 14, zpos);
   off += exlen;
   off += cmlen;
   if(safe_mode){
	lastoff=off;
	drc.putInt(nameIndrc+10,0);
	//安全模式下吞掉扩展字段
   }
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
    long cenpos=-1;
	long cenover=0;
    long headoff=0;
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
		cenover=cenpos;
        int comlen = buf.getShort(i + 20) & 0xffff;
        if (nowpos + 22 + comlen != ziplen) {
         if (cenpos < 0 || headoff < 0)continue;
         zipFile.readFullyAt(rnio, buf, 128, 132, cenpos);
         if (buf.getInt(128) != 0x02014b50)continue;
         zipFile.readFullyAt(rnio, buf, 128, 132, headoff);
         if (buf.getInt(128) != 0x04034b50)continue;
        }
        if (cenlen == 0xffffffffL || cenoff == 0xffffffffL || centot == 0xffff) {
         zipFile.readFullyAt(rnio, buf, 0, 16, nowpos-20);
         if (buf.getInt(0) == 0x07064b50) {
		  cenover=nowpos-20;
          int nextpos = (int) buf.getLong(8);
          zipFile.readFullyAt(rnio, buf, 0, 56, nextpos);
          if (buf.getInt(0) == 0x06064b50) {
           cenlen = buf.getLong(40);
           cenoff = buf.getLong(48);
           cenpos = nextpos - cenlen;
		   if(nextpos==cenover-56)
			cenover=cenpos;
           headoff = cenpos - cenoff;
          }
         }
        }
        break tag;
       }
      }
     }
     return;
    }
	if(safe_mode)
	 cenover=rnio.size();
	if (cenpos >= 0) {
     ByteBuffer cens=modifyEns(headoff, cenpos, (int)cenlen, rnio);
     int censize=cens.position();
     cens.putInt(0X06054B50);
     cens.position(censize + 12);
     cens.putInt(censize);
     cens.putInt((int)cenover);
     cens.position(censize + 22);
     cens.flip();
     rnio.position(cenover);
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
