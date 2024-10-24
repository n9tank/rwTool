package rust;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import org.pngquant;
import org.bitmapquant;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.imageio.stream.MemoryCacheImageInputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.awt.image.DataBufferInt;
import java.awt.Graphics;
import java.util.HashSet;
import java.util.BitSet;

public class ImageUtil {
 public static byte[] tmxOpt(List<rwmapOpt.base64png> list, HashSet tree, HashMap<Integer,Integer> tiles, int w, int h, int j, int size) throws Exception {
  int v=0;
  BufferedImage bit=new BufferedImage(w, h * size, BufferedImage.TYPE_INT_ARGB);
  Graphics gd=bit.getGraphics();
  for (int i=list.size();--i >= 0;) {
   rwmapOpt.base64png png=list.get(k);
   if (png == null)continue;
   byte imgarr[] = Base64.getDecoder().decode(png.img.getTextContent().replaceAll("\\s", ""));
   BufferedImage img=ImageIO.read(new MemoryCacheImageInputStream(new ByteArrayInputStream(imgarr)));
   int first=png.start;
   int pw=img.getWidth() / w;
   for (int c =png.len;--c >= 0;) {
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
  byte out[]=pngquant.intEn(((DataBufferInt)bit.getRaster().getDataBuffer()).getData(), pngquant.attr(65, 80, 1), pngquant.pngAttr(w, h * size, 0.5f));
  return out;
 }
}
