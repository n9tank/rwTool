package rust;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import javax.imageio.ImageIO;
import javax.imageio.stream.MemoryCacheImageInputStream;
import org.pngquant;
import java.nio.*;

public class ImageUtil {
 public static byte[] base64decode(String str){
  return Base64.getDecoder().decode(str);
 }
 public static String base64encode(byte b[]){
  return Base64.getEncoder().encodeToString(b);
 }
 public static String base64encode(ByteBuffer buf){
  return new String(Base64.getEncoder().encode(buf).array());
 }
 public static String concat(String str, String str2){
  if (str.length() == 0)
   return str2;
  return str.concat(str2);
 }
 public static byte[] tmxOpt(List<rwmapOpt.base64png> list, HashSet tree, HashMap<Integer,Integer> tiles, int w, int h, int j, int size) throws Exception {
  int v=0;
  BufferedImage bit=new BufferedImage(w, h * size, BufferedImage.TYPE_INT_ARGB);
  Graphics gd=bit.getGraphics();
  for (rwmapOpt.base64png png:list) {
   if (png == null)continue;
   byte imgarr[] = Base64.getDecoder().decode(iniobj.base64trims(png.img.getTextContent())).array();
   BufferedImage img=ImageIO.read(new MemoryCacheImageInputStream(new ByteArrayInputStream(imgarr)));
   int first=png.start;
   int pw=img.getWidth() / w;
   for (int c=0,len=png.len;c < len;++c) {
    Integer key=c + first;     
    if (!tree.contains(key) && tiles.containsKey(key)) {
     int left=c % pw * w;   
     int top=c / pw * h;
     int n=v + h;
     tiles.put(key, j++);
     gd.drawImage(img, 0, w, v, n, left, left + w, top, top + h, null);
     v = n;  
    }
   }
  }
  byte out[]=pngquant.intEn(((DataBufferInt)bit.getRaster().getDataBuffer()).getData(), pngquant.attr(65, 80, 1), w, h * size, 0.5f, pngquant.ARGB, false);
  return out;
 }
}
