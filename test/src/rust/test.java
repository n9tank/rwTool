package rust;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Pattern;

public class test {
 private static final Pattern skip= Pattern.compile("\\p{C}");
 public static void charList(int start, int max, boolean em, BufferedWriter buff) throws Exception {
  ArrayList crr=new ArrayList();
  ArrayList err=new ArrayList();
  Paint pain=new Paint();
  Rect rec=new Rect();
  pain.setAntiAlias(true);
  pain.setTextSize(40);
  pain.setColor(Color.BLACK);
  /*
   Bitmap map=Bitmap.createBitmap(100, 100, Bitmap.Config.ALPHA_8);
   Canvas vs=new Canvas(map);
   int pixes[]=new int[10000];
   int w=rec.width();
   vs.drawText(s, -rec.left, -rec.top, pain);
   map.getPixels(pixes, 0, w, 0, 0, w, h);
   int ym=w * h;
   int pi=0;
   while (--ym >= 0) {
   if (pixes[ym] != 0) {
   ++pi;
   }
   }
   map.eraseColor(0);*/
  while (start < max) {
   if (start >= 0xd800 && start <= 0xdfff)start = 0xe000;
   String s=String.valueOf((char)start++);
   if (skip.matcher(s).find())continue;
   float x=pain.measureText(s);
   if (x <= 0) {
    pain.getTextBounds(s, 0, 1, rec);
    int h=rec.height();
    if (h > 0)continue;
    err.add(s);
   } else if (!pain.hasGlyph(s)) {
    crr.add(s);
   }
  }
  charc charc=new charc();
  Collections.sort(err, charc);
  Collections.sort(crr, charc);
  if (em) {
   ArrayList irr=crr;
   crr = err;
   err = irr;
  }
  for (String str:crr)buff.write(str);
  for (String str:err)buff.write(str);
  buff.write('\n');
 }
 public static void main(String arg[])throws Exception {
  BufferedWriter buff=new BufferedWriter(new FileWriter("sdcard/a.txt"));
  charList(0x80, 0x07ff, false, buff);
  charList(0x0800, 0xffff, true, buff);
  buff.close();
  System.out.println();
 }
}
