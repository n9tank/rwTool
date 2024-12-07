package rust;


public class Comparables implements Comparable {
 public int hash;
 public Comparable list[];
 public int off;
 public int end;
 public int hashCode() {
  return hash;
 }
 public void set(Comparable arr[], int off, int end) {
  int hashcode=1;
  for (int i=off;i < end;++i)
   hashcode = hashcode * 31 + arr[i].hashCode();
  hash = hashcode; 
  list = arr;
  this.off = off;
  this.end = end;
 }
 public int compareTo(Object o) {
  Comparable orr[]=list;
  Comparables cp=((Comparables)o);
  Comparable irr[]=cp.list;
  int off=this.off;
  int end=this.end;
  int len=end - off;
  int off2=cp.off;
  int i = len - (cp.end - off2);
  if (i != 0)return i;
  for (;off < end;++off,++off2) {
   Comparable p1=orr[off];
   Comparable p2=irr[off2];
   i = p1.compareTo(p2);
   if (i != 0)return i;
  }
  return 0;
 }
 public boolean equals(Object obj) {
  return compareTo(obj) == 0;
 }
}
