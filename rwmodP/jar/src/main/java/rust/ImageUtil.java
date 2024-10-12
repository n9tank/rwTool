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
  BufferedImage img=null;
  ImageInputStream inputStream =ImageIO.createImageInputStream(new ByteArrayInputStream(imgarr));
  ImageReader reader=ImageIO.getImageReadersByFormatName("png").next();
  try {
   reader.setInput(inputStream, false, true);
   ImageReadParam pr=reader.getDefaultReadParam();
   img = new BufferedImage(reader.getWidth(0), reader.getHeight(0), imageType);
   pr.setDestination(img);
   reader.read(0, pr);
  } finally {
   reader.dispose();
   inputStream.close();
  }
  WritableRaster imgrt=img.getRaster();
  BufferedImage bm2 = new BufferedImage(tileWidth, tileHeight * size, imageType);
  WritableRaster bmwt=bm2.getRaster();
  int v = 0, j = 0;
  for (int c = tilec; --c >= 0;) {
   Integer key = c + first;
   if (tiles.containsKey(key)) {
    int left = c % tilew * tileWidth;
    int top = c / tilew * tileHeight;
    int n = v + tileHeight;
    tiles.put(key, j++ + first);
    bmwt.setDataElements(0, v, imgrt.createChild(top, left, tileWidth, tileHeight, 0, 0, null));
    v = n;
   }
  }
  DataBuffer buf=bmwt.getDataBuffer();
  byte out[];
  long attr=pngquant.attr(65, 80, 1);
  long outattr=pngquant.pngAttr(bm2.getWidth(), bm2.getHeight(), ARGB_8888 ?pngquant.ARGB: pngquant.RGB_565, 0.5f);
  if (ARGB_8888)
   out = pngquant.intEn(((DataBufferInt)buf).getData(), attr, outattr);
  else out = pngquant.shortEn((((DataBufferShort)buf).getData()), attr, outattr);
  return out;
 }
}
