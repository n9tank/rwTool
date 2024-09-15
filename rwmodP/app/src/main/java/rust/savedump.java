package rust;

import carsh.log;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.zip.GZIPInputStream;

public class savedump implements Runnable {
 File in;
 File ou;
 ui ui;
 static String xmlencode;
 public savedump(File i, File o, ui u) {
  in = i;
  ou = o;
  ui = u;
 }
 public static int indexOf(byte arr[], byte find[], int off, int start, int len) {
  int l=find.length;
  byte b=find[off];
  for (int i=start;i < len;++i) {
   if (arr[i] == b) {
	if (++off == l)return ++i - l;
	b = find[off];
   } else if (off > 0) {
	i--;
	b = find[off = 0];
   }
  }
  return -off - 1;
 }
 public void run() {
  Throwable ex=null;
  try(BufferedInputStream buff=new BufferedInputStream(new FileInputStream(in))){
   byte[] brr=new byte[8192];
   buff.mark(0);
   buff.read(brr);
   int i=indexOf(brr, new byte[]{(byte)0x1f,(byte)0x8b}, 0, 0, 8192);
   if (i > 0) {
    buff.reset();
    buff.skip(i);
    try(GZIPInputStream gz=new GZIPInputStream(buff)){    
	 byte[] finds="<map".getBytes(xmlencode);
     i=0;          
     int l,k=0,pos=0;      
     while ((l = gz.read(brr, 0, 8192)) > 0) {
      int j = indexOf(brr, finds, i, k, l);
      if (j < 0)i = -j + 1;
      else {
       k = j + (pos=finds.length);  
       l -= k;   
       break;    
      }
     }
     if (k > 0) {
      int lastPos=0;      
      try(FileOutputStream f=new FileOutputStream(ou);   
      FileChannel ch=f.getChannel();             
      BufferedOutputStream out=new BufferedOutputStream(f)){   
      out.write(finds);
      finds="/map".getBytes(xmlencode);             
       i = 0;      
       do{
        out.write(brr, k, l);            
        int j = indexOf(brr, finds, i, k, l);
        if (j < 0)i = -j + 1;
        else lastPos = pos + j + finds.length;
        pos += l;
        k = 0;        
       }while((l = gz.read(brr, 0, 8192)) > 0);
       out.flush();         
       ch.truncate(lastPos);
       out.write(">".getBytes(xmlencode));
      }            
     }
    }
   }
  } catch (Throwable e) {
   log.e(this, ex = e);
  }
  if (ex != null)ou.delete();
  ui.end(ex);
 }
}
