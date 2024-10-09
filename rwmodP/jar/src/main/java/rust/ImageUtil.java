package rust;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.WritableRaster;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import org.pngquant;
import java.awt.image.DataBufferShort;

public class ImageUtil {
 public static byte[] tmxPngOpt(byte[] imgarr, boolean ARGB_8888, int tileWidth, int tileHeight, int size, int first, int tilec, int tilew, HashMap<Integer, Integer> tiles) throws IOException {
  int imageType = ARGB_8888 ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_USHORT_565_RGB;
  ImageInputStream inputStream =ImageIO.createImageInputStream(new ByteArrayInputStream(imgarr));
  BufferedImage img=null;
  try {
   img = ImageIO.read(inputStream);
  } finally {
   inputStream.close();
  }
  BufferedImage bm2 = new BufferedImage(tileWidth, tileHeight * size, imageType);
  Graphics wt=bm2.getGraphics();
  int v = 0, j = 0;
  for (int c = tilec; --c >= 0;) {
   Integer key = c + first;
   if (tiles.containsKey(key)) {
    int left = c % tilew * tileWidth;
    int top = c / tilew * tileHeight;
    int n = v + tileHeight;
    tiles.put(key, j++ + first);
    wt.drawImage(img, 0, v, tileWidth, v + tileHeight, top, left, top + tileWidth, left + tileHeight, null);
    v = n;
   }
  }
  DataBuffer buf=bm2.getRaster().getDataBuffer();
  byte out[];
  long attr=pngquant.attr(65, 80, 1);
  long outattr=pngquant.pngAttr(bm2.getWidth(), bm2.getHeight(), 0.5f);
  if (ARGB_8888)
   out = pngquant.intEn(((DataBufferInt)buf).getData(), attr, outattr);
  else out = pngquant.shortEn((((DataBufferShort)buf).getData()), attr, outattr);
  return out;
 }
}
