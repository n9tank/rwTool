package rust;

import java.util.Arrays;
import java.nio.ByteBuffer;

public class BMFind {
 public byte bad[];
 public byte good[];
 public byte drc[];
 public BMFind(byte drc[], boolean re) {
  bad = bad(drc, re);
  good = good(drc, re);
  this.drc = drc;
 }
 public int indexOf(ByteBuffer src, int start, int end) {
  return BMFind(src, drc, good, bad, start, end, false);
 }
 public int lastIndexOf(ByteBuffer src, int start, int end) {
  return BMFind(src, drc, good, bad, start, end, true);
 }
 public static byte[] bad(byte src[], boolean re) {
  byte[] list=new byte[128];
  int len=src.length,nlen=len - 1;
  Arrays.fill(list, (byte)len);
  if (!re) {
   for (int i=0;i < len;++i) 
    list[src[i]] = (byte)(nlen - i);
  } else {
   for (int i=len;--i >= 0;) 
    list[src[i]] = (byte)i;
  }
  return list;
 }
 public static byte[] good(byte src[], boolean re) {
  int len=src.length;
  byte[] list=new byte[len];
  int lastpos=len;
  int nlen=len - 1;
  if (!re) {
   for (int j=len;--j >= 0;) {
    int k=j;
    for (int i=0;++k < len && src[k] == src[i];++i);
    if (k == len)lastpos = j + 1;
    list[j] = (byte)(lastpos + nlen - j);
   }
   for (int j=0;j < nlen;++j) {
    int k=j;
    for (int i=nlen;k >= 0 && src[k] == src[i];--k,--i);
    list[nlen - j + k] = (byte)(nlen - k);
   }
  } else {
   for (int j=0;j < len;++j) {
    int k=j;
    for (int i=len;--k >= 0 && src[k] == src[i];--i);
    if (k == 0)lastpos = (nlen - j) + 1;
    list[j] = (byte)(lastpos + j);
   }
   for (int j=nlen;--j >= 0;) {
    int k=j;
    for (int i=0;k < len && src[k] == src[i];++k,++i);
    list[k - j] = (byte)k;
   }
  }
  return list;
 }
 public static int BMFind(ByteBuffer src, byte drc[], byte good[], byte bad[], int start, int end, boolean re) {
  int plen=drc.length;
  int nlen=plen - 1;
  int i,j;
  if (!re) {
   for (i = nlen + start;i < end;) {
    for (j = plen;src.get(i) == drc[--j];--i) {
     if (j == 0)return i;
    }
    int c=src.get(i);
    i += Math.max(good[j], c < 0 ?nlen: bad[c]);
   }
  } else {
   for (i = end - plen;i >= start;) {
    int n;
    for (j = plen;src.get(i) == drc[n = nlen - (--j)]; ++i) {
     if (j == 0) return i;
    }
    int c = src.get(i);
    i -= Math.max(good[n], c < 0 ? nlen : bad[c]);
   }
  }
  return -1;
 }
}
