package rust;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
  for (Map.Entry<String,section>en:(Set<Map.Entry>)drc.entrySet()) {
   String ac=en.getKey();
   HashMap list=null;
   section cpy = (section)src.get(ac);
   if (cpy != null) {
    list = cpy.m;
   } else {
    cpy = new section();
    src.put(ac, cpy);
   }
   String str=list == null ?null: (String)list.get("@copyFrom_skipThisSection");
   boolean has="1".equals(str)/* || "true".equals(str)*/;
   if (!has) {
    section cp = en.getValue();
    HashMap listdrc=cp.m;
    if (list == null)cpy.m = (HashMap)listdrc.clone();
    else {
	 list.putAll(new MapResize(listdrc));   
	 for (Map.Entry en2:(Set<Map.Entry>)listdrc.entrySet())
      list.putIfAbsent(en2.getKey(), en2.getValue());
    }
   }
  }
 }	
 public void put(iniobj drc) {
  put(put, drc.put);
 }
 void asFor(section cpy) {
  //copyFrom去重由于luke的屎容易引发BUG，应该交给开发者自己处理
  HashMap map=put;
  HashMap hash=cpy.m;
  String str = (String)hash.remove("@copyFromSection");
  //这里不允许尾部仅有“,”的占位符
  if (str != null && str.length() > 0 && !str.equals("IGNORE")) {
   String list[]=str.replace(" ", "").split(",");
   int i=list.length;
   HashMap copy=null;
   if (i == 1) {
	String vl=list[0];
	section set=(section)map.get(vl);
	if (set != null) {
	 asFor(set);
	 copy = set.m;
	}
   } else {
    i = 0;
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
	hash.putAll(new MapResize(copy));
	for (Map.Entry en:(Set<Map.Entry>)copy.entrySet())
	 hash.putIfAbsent(en.getKey(), en.getValue());
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
 static final Pattern find=Pattern.compile("[a-zA-Z_][0-9a-zA-Z_.]*");
 static final Pattern mathExp=Pattern.compile("[-+/*^%()]");
 public static String copyValue(HashMap<String,section> ini, String list, String k) {
  if (list != null && list.length() > 0 && ! list.equals("IGNORE")) {
   String keys[]=list.replace(" " , "").split(",");
   for (int i=keys.length;--i >= 0;) {
	section kvs= ini.get(keys[i]);
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
 String get(String str, String eqz, section cpy, StringBuilder buff) {
  buff.setLength(0);
  int i=0,j=0;
  while ((i = str.indexOf("${", i)) >= 0) {
   buff.append(str, j, i);
   j = i;
   int n=str.indexOf('}', i += 2);
   if (n < 0)break;
   String key=str.substring(i, n).trim();
   if (key.length() > 0) {
    Matcher matcher=find.matcher(key);
    int q=0,st=buff.length();
    while (matcher.find()) {
     int k;
     buff.append(key, q, k = matcher.start());
     String group = matcher.group(0);
     if (!set.contains(group)) {
      Object o=null;
      String list[]=group.split("\\.", 2);
      String keyv=list[0];
      if (list.length > 1) {
       if (!keyv.equals("section") && !key.equals(eqz)) {
        cpy = (section)put.get(keyv);
        if (cpy == null)return null;
       }
       o = cpy.m.get(list[1]);
      } else {
       o = cpy.m.get("@define ".concat(keyv));
       if (o == null)o = gl.get(keyv);
      }
      if (o == null)return null;
      buff.append(o);
      q = matcher.end();
     } else q = k;
    }
    buff.append(key, q, key.length());
    if (mathExp.matcher(key).find()) {
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
