package rust;
import java.util.Arrays;
import rust.loaders;

public class loaders implements Comparable {
 public int compareTo(Object o) {
  loaders obj=(loaders)o;
  int i=Integer.compare(hashCode, obj.hashCode);
  if (i != 0)return i;
  loader orr[]=copy;
  loader irr[]=obj.copy;
  int len=orr.length;
  i = len - irr.length;
  if (i != 0)return i;
  for (int k=0;k < len;++k) {
   loader p1=orr[k];
   loader p2=irr[k];     
   if (p1 == p2)continue;
   i = Integer.compare(p1.hashCode(), p2.hashCode());
   if (i != 0)return i;
   String path=p1.src;
   i = path.compareTo(p2.src);
   if (i != 0)return i;
   //对于依赖库仍需避免冲突
   if (path == "//") {
    i = p1.str.compareTo(p2.str);
    if (i != 0)return i;  
   }
  }
  return ifNullToBlank(all).compareTo(ifNullToBlank(all));
 }
 public static String ifNullToBlank(loader all) {
  return all == null ?"": all.src;
 }
 public loader copy[];
 public loader all;
 public int hashCode;
 public loaders(loader copy[], loader all) {
  this.all = all;
  hashCode = Arrays.hashCode(copy) * 31 + (all == null ?0: all.hashCode());
  this.copy = copy;
 }
 public int hashCode() {
  return hashCode;
 }
 public boolean equals(Object obj) {
  return compareTo(obj) == 0;
 } 
}
