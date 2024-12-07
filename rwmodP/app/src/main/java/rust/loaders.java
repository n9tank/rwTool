package rust;
import java.util.Arrays;
import rust.loaders;

public class loaders implements Comparable {
 public int compareTo(Object o) {
  loaders obj=(loaders)o;
  loader orr[]=list;
  loader irr[]=obj.list;
  int len=orr.length;
  int i = len - irr.length;
  if (i != 0)return i;
  for (int k=0;k < len;++k) {
   loader p1=orr[k];
   loader p2=irr[k]; 
   i = (p1 == null ?0: 1) - (p2 == null ?0: 1);
   if (i != 0)return i;
   i = p1.compareTo(p2);
   if (i != 0)return i;
  }
  return 0;
 }
 public loader list[];
 public int hashCode;
 public loaders(loader copy[]) {
  this.hashCode = Arrays.hashCode(copy);
  this.list = copy;
 }
 public int hashCode() {
  return hashCode;
 }
 public boolean equals(Object obj) {
  return compareTo(obj) == 0;
 } 
}
