package rust;
import java.util.Arrays;

public class Strings implements Comparable {
 public int compareTo(Object o) {
  Strings drc=((Strings)o);
  int i=Integer.compare(hash, drc.hash);
  if (i != 0)return i;
  String arr[]=this.arr;
  String next[]=drc.arr;
  int len=arr.length;
  i = len - next.length;
  if (i != 0)return i;
  for (i = 0;i < len;++i) {
   String str=arr[i];
   String str2=next[i];
   int j=Integer.compare(str.hashCode(), str2.hashCode());
   if (j != 0)return j;
   j = str.compareTo(str2);
   if (j != 0)return j;
  }
  return 0;
 }
 public String arr[];
 public int hash;
 public Strings(String obj[]) {
  arr = obj;
  hash = Arrays.hashCode(obj);
 }
 public int hashCode() {
  return hash;
 }
 public boolean equals(Object obj) {
  return compareTo(obj) == 0;
 }
}
