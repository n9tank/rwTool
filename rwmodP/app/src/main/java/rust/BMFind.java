package rust;

import java.util.Arrays;

public class BMFind {
 public byte bad[];
 public byte good[];
 public byte drc[];
 public BMFind(byte drc[], boolean re) {
  if (re)reverse(drc);
  bad = bad(drc);
  good = good(drc);
  this.drc = drc;
 }
 public int indexOf(byte[] src, int start, int end) {
  return BMFind(src, drc, good, bad, start, end);
 }
 public int lastIndexOf(byte[] src, int start, int end) {
  return lastBMFind(src, drc, good, bad, start, end);
 }
 //good方法实在看不懂了，翻转将就一下
 public static void reverse(byte[] src) {
  int len=src.length;
  int nlen=len - 1;
  int l=len >> 1;
  for (int i = 0; i < l; i++) {
   byte a=src[i];
   int ni=nlen - i;
   byte b=src[ni];
   src[i] = b;
   src[ni] = a;
  }
 }
 public static byte[] bad(byte src[]) {
  byte[] list=new byte[128];
  int len=src.length,nlen=len - 1;
  Arrays.fill(list, (byte)len);
  for (int i=0;i < len;++i) 
   list[src[i]] = (byte)(nlen - i);
  return list;
 }
 public static byte[] good(byte src[]) {
  int len=src.length;
  byte[] list=new byte[len];
  int lastpos=len;
  int nlen=len - 1;
  for (int j=len;--j >= 0;) {
   int k=j;
   for (int i=0;++k < len && src[k] == src[i];++i);
   if (k == len)lastpos = j + 1;
   k = nlen - j;
   list[k] = (byte)(lastpos + k);
  }
  for (int j=0;j < nlen;++j) {
   int k=j;
   for (int i=nlen;k >= 0 && src[k] == src[i];--k,--i);
   list[j - k] = (byte)(nlen - k);
  }
  return list;
 }
 public static int BMFind(byte src[], byte drc[], byte good[], byte bad[], int start, int end) {
  int plen=drc.length;
  int nlen=plen - 1;
  int i,j;
  for (i = nlen + start;i < end;) {
   for (j = plen;src[i] == drc[--j];--i) {
    if (j == 0)return i;
   }
   int c=src[i];
   i += Math.max(good[nlen - j], c < 0 ?nlen: bad[c]);
  }
  return -1;
 }
 public static int lastBMFind(byte src[], byte drc[], byte good[], byte bad[], int start, int end) {
  int plen=drc.length;
  int nlen = drc.length - 1;
  int i,j;
  for (i = end - plen;i >= start;) {
   for (j = plen;src[i] == drc[--j]; ++i) {
    if (j == 0) return i;
   }
   int c = src[i];
   i -= Math.max(good[nlen - j], c < 0 ? nlen : bad[c]);
  }
  return -1;
 }
}
