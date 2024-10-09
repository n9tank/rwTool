
import java.util.ArrayList;

public class MathExp {
 public static double cmp(String str) {
  ArrayList dr=new ArrayList();
  int i=0,ls=0,z=-1;
  int le=str.length();
  while (i <= le) {
   char c=i == le ?')': str.charAt(i);
   if ((c < '0' || c > '9') && c != '.') {
    if (i > ls) {
     pop(dr);
     dr.add(Double.valueOf(str.substring(ls, i)));
    }
    if (c < 'a' || c > 'z') {
     if (z >= 0)dr.add(Character.valueOf(str.charAt(z + 1)));
     z = -1;
     ++i;
     if (c >= 33) {
      boolean is=false;
      if (c == '+' || c == '-') {
       int s=dr.size();
       if (s-- > 0) {
        Object o=dr.get(s);
        if (o instanceof Character) {
         char lc=o;
         if (is = (lc == '+' || lc == '-')) {
          char set=0;
          if (lc != c)set = '-';
          else if (lc == c && c == '-')set = '+';
          if (set != 0)dr.set(s, Character.valueOf(set));
         }
        }
       }
      }
      if (c == '(' || c == ')')pop(dr);
      if (!is && c != ')')dr.add(Character.valueOf(c));
     }
    } else {
     if (z < 0) z = i;
     ++i;
    }
    ls = i;
   } else ++i;
  }
  return dr.get(0);
 }
 public static void pop(ArrayList dr) {
  int s;
  while ((s = dr.size()) > 1) {
   //System.out.println(dr);
   --s;
   Object o = dr.get(s);
   char cs=0;
   Double d=null;
   if (o instanceof Double)d = (double)o;
   else if ((cs = o) == '(' || (cs >= 'a' && cs <= 'z'))break;
   if (d == null) {
    o = dr.get(--s);
    if (o instanceof Double)d = (Double)o;
    else return;
   }
   if (--s < 0)return;
   char lc = dr.get(s);
   if (lc == '(') {
    if (cs == 0)dr.remove(s);
    if (cs != 0 || --s < 0)return;
    lc = dr.get(s);
   }
   double f=0d;
   int add=1;
   boolean has=false;
   if ((lc < 'a' || lc > 'z')) {
    int u;
    if ((u = s - 1) >= 0 && (o = dr.get(u)) instanceof Double) {
     --s;
     f = o;
     add = 2;
    } else has = true;
   }
   if (has || getl(lc) >= getl(cs)) {
    double ld=d;
    switch (lc) {
     case '+':
      f += ld;
      break;
     case '-':
      f -= ld;
      break;
     case '*':
      f *= ld;
      break;
     case '/':
      f /= ld;
      break;
     case '^':
      f = Math.pow(f, ld);
      break;
     case 'q':
      f = Math.sqrt(ld);
      break;
     case 'i':
      f = Math.sin(Math.toRadians(ld));
      break;
     case 'o':
      f = Math.cos(Math.toRadians(ld));
      break;
     default:
      f = (double)d.intValue();
      break;
    }
    dr.set(s, Double.valueOf(f));
    dr.remove(s += add);
    if (add > 1)dr.remove(--s);
   } else break;
  }
 }
 public static int getl(char c) {
  switch (c) {
   case 0:
   case '+':
   case '-':
    return 0;
   case '^':
    return 2;
  }
  return 1;
 }
}
