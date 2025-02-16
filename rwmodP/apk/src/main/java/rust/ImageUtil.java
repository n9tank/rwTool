package rust;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import org.pngquant;

public class ImageUtil {
 public static byte[] tmxOpt(List<rwmapOpt.base64png> list, HashSet tree, HashMap<Integer,Integer> tiles, int w, int h, int j, int size) {
  int v=0;
  Bitmap bit=Bitmap.createBitmap(w, h * size, Bitmap.Config.ARGB_8888);
  for (rwmapOpt.base64png png:list) {
   if (png == null)continue;
   byte imgarr[] = Base64.getDecoder().decode(iniobj.trims(png.img.getTextContent()));
   Bitmap bmp=BitmapFactory.decodeByteArray(imgarr, 0, imgarr.length);
   int pw=bmp.getWidth() / w;
   Canvas cv= new Canvas(bit);
   Paint pt= new Paint();
   pt.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
   int first=png.start;
   for (int c=0,len=png.len;c < len;++c) {
    Integer key=c + first;     
    if (!tree.contains(key) && tiles.containsKey(key)) {
     int left=c % pw * w;   
     int top=c / pw * h;
     int n=v + h;
     tiles.put(key, j++);                                                         
     cv.drawBitmap(bmp, new Rect(left, top, left + w, top + h), new Rect(0, v, w, n), pt);
     v = n;  
    }                           
   }
   bmp.recycle();
  }
  byte out[]=pngquant.en(bit, pngquant.attr(65, 80, 1), pngquant.ARGB,  0.5f);
  bit.recycle();
  return out;
 }
}
