package rust;
public class MathExp {
 CharSequence m;
 int in;
 char t;
 public static double get(CharSequence str) {
  MathExp math=new MathExp();
  math.m = str;
  math.t = str.charAt(0);
  return math.cmp();
 }
 public double next() {
  while (nextChar('+'));
  if (nextChar('-'))return -next();
  double d=0;
  if (nextChar('(')) {
   d = cmp();
   nextChar(')');
  } else {
   int i=in;
   char c;
   if (((c = t) >= '0' && c <= '9') || c == '.') {
    while (((c = t) >= '0' && c <= '9') || c == '.')nextChar();
    d = Double.parseDouble(m.subSequence(i, in).toString());
   } else {
    while ((c = t) >= 'a' && c <= 'z')nextChar();
    d = next();
    c = m.charAt(i + 2);
    switch (c) {
     case 'q':
      d = Math.sqrt(d);
      break;
     case 'i':
      d = Math.sin(Math.toRadians(d));
      break;
     case 'o':
      d = Math.cos(Math.toRadians(d));
      break;
     default:
      d = (int)d;
      break;
    }
   }
  }
  if (nextChar('^')) {
   d = Math.pow(d, next());
  }
  return d;
 }
 public void nextChar() {
  CharSequence str=m;
  int i=++in;
  char c;
  int len=str.length();
  if (i >= len)c = ')';
  else c = str.charAt(i);
  t = c;
 }
 public boolean nextChar(char c) {
  while (t <= 32)nextChar();
  if (t != c)return false;
  nextChar();
  return true;
 }
 public double cmp() {
  double d=mul();
  while (true) {
   if (nextChar('+')) {
    d += mul();
   } else if (nextChar('-')) {
    d -= mul();
   } else return d;
  }
 }
 public double mul() {
  double d=next();
  while (true) {
   if (nextChar('*')) {
    d *= next();
   } else if (nextChar('/')) {
    d /= next();
   } else if (nextChar('%')) {
    d %= next();
   } else return d;
  }
 }
}
