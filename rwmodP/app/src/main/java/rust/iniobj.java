package rust;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.nio.*;
import java.nio.charset.*;

public class iniobj {
 public HashMap put;
 public HashMap gl;
 public HashMap ascache;
 public static HashMap clone(HashMap map) {
  HashMap put=(HashMap)map.clone();
  for (Map.Entry<String,section> en:(Set<Map.Entry<String,section>>)map.entrySet()) {
   section cp=new section();
   cp.m = (HashMap)en.getValue().m.clone();
   en.setValue(cp);
  }
  return put;
 }
 public iniobj(HashMap map) {
  put = map;
 }
 public static void put(HashMap src, HashMap drc) {
  MapResize resize=new MapResize();
  for (Map.Entry<String,section>en:(Set<Map.Entry>)drc.entrySet()) {
   String ac=en.getKey();
   HashMap list=null;
   section cpy = (section)src.get(ac);
   if (cpy != null)
    list = cpy.m;
   else {
    cpy = new section();
    src.put(ac, cpy);
   }
   String str=list == null ?null: (String)list.get("@copyFrom_skipThisSection");
   boolean has="1".equals(str)/* || "true".equals(str)*/;
   if (!has) {
    section cp = en.getValue();
    HashMap listdrc=cp.m;
    if (list == null)cpy.m = (HashMap)listdrc.clone();
    else putIfAll(resize, list, listdrc);
   }
  }
 }
 public void put(iniobj drc) {
  put(put, drc.put);
 }
 public static final void putIfAll(MapResize resize, HashMap list, HashMap listdrc) {
  resize.size = listdrc.size();
  list.putAll(resize);   
  for (Map.Entry en:(Set<Map.Entry>)listdrc.entrySet())
   list.putIfAbsent(en.getKey(), en.getValue());
 }
 public void asFor(section cpy) {
  //copyFrom去重由于luke的屎容易引发BUG，应该交给开发者自己处理
  HashMap map=put;
  HashMap hash=cpy.m;
  String str = (String)hash.remove("@copyFromSection");
  //这里不允许尾部仅有“,”的占位符
  if (str != null && str.length() > 0 && !str.equals("IGNORE")) {
   String list[]=str.split(",");
   int len=list.length;
   HashMap copy=null;
   if (len == 1) {
	section set=(section)map.get(list[0].trim());
	if (set != null) {
	 asFor(set);
	 copy = set.m;
	}
   } else {
    for (int i=0;i < len;++i)
     list[i] = list[i].trim();
    HashMap maps[]= merge(list);
    if (maps[1] == null) {
     copy = maps[0];
    } else {
     copy = new HashMap();
     for (HashMap in:maps) {
      if (in == null)break;
      copy.putAll(in);
     }
     Comparables ckey=new Comparables();
     ckey.set(list, 0, list.length);
     ascache.put(ckey, copy);
    }
   }
   if (copy != null) {
	cpy.copy = copy;
    putIfAll(new MapResize(), hash, copy);
   }
  }
 }
 public void as() {
  globalMap();
  ascache = new HashMap();
  for (section v:(Collection<section>)put.values())
   asFor(v);
  ascache = null;
 }
 public HashMap[] merge(String list[]) {
  int len=list.length;
  HashMap copy[]=new HashMap[len];
  Comparables strs=new Comparables();
  HashMap ini=put;
  int c=0;
  wh:
  for (int i=0;i < len;) {
   int v=len;
   int n;
   for (;(n = (v - 1)) > i;v = n) {
    strs.set(list, i, v);
    HashMap map= (HashMap)ascache.get(strs);
    if (map != null) {
     //如果考虑大块会提高时间复制度，这里简单跳过
     i = v;
     copy[c++] = map;
     continue wh;
    }
   }
   String key=list[i++];
   section map=(section)ini.get(key);
   if (map != null) {
    asFor(map);
    copy[c++] = map.m;
   }
  }
  return copy;
 }
 static final HashSet set;
 static{
  HashSet sset=new HashSet();
  set = sset;
  sset.add("int");
  sset.add("cos");
  sset.add("sin");
  sset.add("sqrt");
 }
 public static int indexOfDefine(CharSequence str, int i) {
  int len=str.length();
  for (;i < len;++i) {
   char c=str.charAt(i);
   if (c == '_' || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
    return i;
   }
  }
  return -1;
 }
 public static int nextDefine(CharSequence str, int i) {
  int len=str.length();
  for (;++i < len;) {
   char c=str.charAt(i);
   if (c != '.' && c != '_' && (c < '0' || c > '9') && (c < 'a' || c > 'z') && (c < 'A' || c > 'Z')) {
    return i;
   }
  }
  return len;
 }
 static final char[] mathExp=new char[]{'+','-','*','/','%','^','(',')'};
 public static int indexOfChars(CharSequence str, char list[]) {
  for (int i=0,len=str.length();i < len;++i) {
   char c=str.charAt(i);
   for (char j:list)
    if (c == j)return i;
  }
  return -1;
 }
 public static ByteBuffer base64trims(String str) {
  int k=0;
  int len=str.length();
  byte[] cr=new byte[len];
  for (int i = 0; i < len; i++) {
   byte c =(byte)str.charAt(i);
   if (c != '\n')
    cr[k++] = c;
  }
  return ByteBuffer.wrap(cr, 0, k);
 }
 public static CharBuffer trims(String str) {
  int k=0;
  int len=str.length();
  char[] cr=new char[len];
  for (int i = 0; i < len; i++) {
   char c =str.charAt(i);
   if (c != ' ' && c != '\n')
    cr[k++] = c;
  }
  return CharBuffer.wrap(cr, 0, k);
 }
 public static String copyValue(HashMap<String,section> ini, String list, String k) {
  if (list != null && list.length() > 0 && ! list.equals("IGNORE")) {
   String keys[]=list.split(",");
   for (int i=keys.length;--i >= 0;) {
	section kvs= ini.get(keys[i].trim());
	if (kvs == null)continue;
	HashMap<String,String> kvmap=kvs.m;
	String obj=kvmap.get(k);
	if (obj != null)return obj;
	if ((obj = copyValue(ini, kvmap.get("@copyFromSection"), k)) != null)
	 return obj;
   }
  }
  return null;
 }
 public void globalMap() {
  HashMap gl=new HashMap();
  this.gl = gl;
  for (section cpy:(Collection<section>)put.values()) {
   Iterator<Map.Entry<String,String>> ite2=cpy.m.entrySet().iterator();
   while (ite2.hasNext()) {
    Map.Entry<String,String> en2=ite2.next();
    String key=en2.getKey();
    if (key.startsWith("@global ")) {
     String value=en2.getValue();
     if (!value.equals("IGNORE")) {
      gl.put(key.substring(8), value);
      ite2.remove();
     }
    }
   }
  }
 }
 String get(String str, String thisSectionKey, section thisSection, StringBuilder buff) {
  buff.setLength(0);
  int i=0,j=0;
  while ((i = str.indexOf("${", i)) >= 0) {
   buff.append(str, j, i);
   j = i;
   int n=str.indexOf('}', i += 2);
   if (n < 0)break;
   String matcher=str.substring(i, n).trim();
   //实际上应该考虑其他空字符，不过为了省事就这样
   if (matcher.length() > 0) {
    int st=buff.length();
    int groupst=0;
    int lastst=0;
    while ((groupst = indexOfDefine(matcher, groupst)) >= 0) {
     buff.append(matcher, lastst, groupst);
     lastst = groupst;
     groupst = nextDefine(matcher, groupst);
     String group=matcher.substring(lastst, groupst);
     if (!set.contains(group)) {
      Object o=null;
      int spiltIn=group.indexOf('.');
      String key=spiltIn < 0 ?group: group.substring(0, spiltIn);
      if (spiltIn >= 0) {
       section cpy;
       if (!key.equals("section") && !key.equals(thisSectionKey)) {
        cpy = (section)put.get(key);
        if (cpy == null)return null;
       } else cpy = thisSection;
       o = cpy.m.get(group.substring(spiltIn + 1));
      } else {
       o = thisSection.m.get("@define ".concat(key));
       if (o == null)o = gl.get(key);
      }
      if (o == null)return null;
      buff.append(o);
      lastst = groupst;
     }
    }
    buff.append(matcher, lastst, matcher.length());
    if (indexOfChars(matcher, mathExp) >= 0) {
     double b= MathExp.get(buff.subSequence(st, buff.length()));
     buff.setLength(st);
     int intd=(int)b;
     if (intd == b) buff.append(intd);
     else buff.append(b);
    }
   }
   j = i = ++n;
  }
  if (buff.length() == 0)return str;
  buff.append(str, j, str.length());
  return buff.toString();
 }
}
