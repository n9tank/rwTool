package rust;

import java.util.Arrays;

public class BMFind {
 public byte bad[];
 public byte good[];
 public byte drc[];
 public BMFind(byte drc[]) {
  bad = bad(drc);
  good = good(drc);
  this.drc = drc;
 }
 public int indexOf(byte[] src, int start, int end) {
  return BMFind(src, drc, good, bad, start, end);
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
   for (int i=0;++k < len;++i) {
    if (src[k] != src[i])break;
   }
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
  int i,j=0;
  for (i = nlen + start;i < end;) {
   for (j = plen;src[i] == drc[--j];--i) {
    if (j == 0)return i;
   }
   int c=src[i];
   i += Math.max(good[nlen - j], c < 0 ?nlen: bad[c]);
  }
  return -1;
 }
}
