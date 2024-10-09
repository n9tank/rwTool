package rust;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.nio.ByteBuffer;
import org.pngquant;
import java.nio.ByteOrder;
import org.bitmapquant;

public class ImageUtil {
 //这个函数给桌面端适配
 public static byte[] tmxPngOpt(byte imgarr[], boolean ARGB_8888, int tileWidth, int tileHeight, int size, int first, int tilec, int tilew, HashMap tiles) throws IOException {
  Bitmap.Config cf=ARGB_8888 ?Bitmap.Config.ARGB_8888: Bitmap.Config.RGB_565;
  BitmapFactory.Options options = new BitmapFactory.Options();
  options.inPreferredConfig = cf;
  Bitmap bmp=BitmapFactory.decodeByteArray(imgarr, 0, imgarr.length, options);
  Bitmap bm2= Bitmap.createBitmap(tileWidth, tileHeight * size, Bitmap.Config.ARGB_8888);                               
  Canvas cv= new Canvas(bm2);
  Paint pt= new Paint();
  pt.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));      
  int v=0,j=0;                   
  for (int c = tilec;--c >= 0;) {
   Integer key=c + first;     
   if (tiles.containsKey(key)) {
    int left=c % tilew * tileWidth;   
    int top=c / tilew * tileHeight;
    int n=v + tileHeight;
    tiles.put(key, j++ + first);                                                         
    cv.drawBitmap(bmp, new Rect(left, top, left + tileWidth, top + tileHeight), new Rect(0, v, tileWidth, n), pt);
    v = n;  
   }                           
  }
  bmp.recycle();
  byte out[]=bitmapquant.en(bm2, pngquant.attr(65, 80, 1), ARGB_8888 ?pngquant.ARGB: pngquant.RGB_565,  0.5f);
  bm2.recycle();
  return out;
 }
}
