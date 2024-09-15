package rust;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;

public class iniobj {
 public HashMap put;
 public HashMap gl;
 public loder all;
 public iniobj() {
  put = new HashMap();
 }
 public static HashMap clone(HashMap map) {
  HashMap put=new HashMap();
  for (Map.Entry<String,cpys> en:(Set<Map.Entry<String,cpys>>)map.entrySet()) {
   cpys cp=new cpys();
   cp.m = (HashMap)en.getValue().m.clone();
   put.put(en.getKey(), cp);
  }
  return put;
 }
 public iniobj(HashMap map, loder path) {
  put = map;
  for (cpys cpy:(Collection<cpys>)map.values()) {
   HashMap m=cpy.m;
   HashMap coe;
   cpy.coe = coe = new HashMap();
   for (Map.Entry<String,String> en:(Set<Map.Entry<String,String>>)m.entrySet()) {
    String key= en.getKey();
    if (rwmodProtect.Res.containsKey(key))coe.put(key, path);
   }
  }
 }
 public void put(iniobj drc, loder path) {
  HashMap src=put;
  for (Map.Entry<String,cpys>en:(Set<Map.Entry>)drc.put.entrySet()) {
   String ac=en.getKey();
   HashMap list=null;
   cpys cpy = (cpys)src.get(ac);
   if (cpy != null) {
    list = cpy.m;
   } else {
    cpy = new cpys();
    src.put(ac, cpy);
   }
   String str=list == null ?null: (String)list.get("@copyFrom_skipThisSection");
   boolean has="1".equals(str) || "true".equals(str);
  if(!has){
   cpys cp = en.getValue();
   HashMap listdrc=cp.m;
   HashMap cpcoe=cp.coe;
   HashMap coe=cpy.coe;
    if (list == null) {
     cpy.m = (HashMap)listdrc.clone();
     if (path == null && cpcoe != null) {
      coe = (HashMap)cpcoe.clone();
      cpcoe = null;
     } else coe = new HashMap();
     cpy.coe = coe ;
    } else {
     coe.putAll(new MapResize(listdrc));   
     for (Map.Entry en2:(Set<Map.Entry>)listdrc.entrySet())
      list.putIfAbsent(en2.getKey(), en2.getValue());
    }
   if (cpcoe != null) {
   coe.putAll(new MapResize(cpcoe));     
	if (path != null) {
	 for (Object s:(Set)cpcoe.keySet())
	  coe.putIfAbsent(s, path);
	} else {
	 for (Map.Entry s:(Set<Map.Entry>)cpcoe.entrySet())
	  coe.putIfAbsent(s.getKey(), s.getValue());
	}
   }}
  }
 }
 void asFor(cpys cpy, String key) {
  HashMap map=put;
  HashMap hash=cpy.m;
  String str = (String)hash.remove("@copyFromSection");
  if (str != null && !str.equals("IGNORE")) {
   HashMap mapput = (HashMap)hash.clone();
   cpy.m = mapput;
   String list[]=str.split(",");
    for(String vl:list){
    vl=vl.trim();
    cpys set=(cpys)map.get(vl);
    if (set != null) {
	 asFor(set, vl);
	 mapput.putAll(set.m);
    }
   }
  }
 }
 public void as() {
  HashMap gl=new HashMap();
  this.gl = gl;
  HashMap map=put;
  for (cpys cpy:(Collection<cpys>)map.values()) {
   Iterator<Map.Entry<String,String>> ite2=cpy.m.entrySet().iterator();
   while (ite2.hasNext()) {
    Map.Entry<String,String> en2=ite2.next();
    String key=en2.getKey();
    if (key.startsWith("@global ")) {
     String value=en2.getValue();
     if(!value.equals("IGNORE")){
     gl.put(key.substring(8),value);
     ite2.remove();
     }
    }
   }
  }
  Set<Map.Entry> se=(Set<Map.Entry>)map.entrySet();
  for (Map.Entry<String,Object> en2:se)
   asFor((cpys)en2.getValue(), en2.getKey());
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
 static final Pattern find2=Pattern.compile("[-+/*^%()]");
 String get(String str, String eqz, cpys cpy) {
  HashMap map=put;
  Map gl=this.gl;
  StringBuilder buff=new StringBuilder();
  int i=0,j=0;
  buff.setLength(0);
  Pattern find0=find;
  Pattern find1=find2;
  while (true) {
   i = str.indexOf("${", i);
   if (i >= 0) {
	buff.append(str, j, i);
	j = i;
	int n=str.indexOf("}", i += 2);
	if (n > 0) {
	 String key=str.substring(i, n).trim();
	 if (key.length() > 0) {
	  boolean isNumber=find1.matcher(key).find();
	  HashSet sset=set;
	  Matcher matcher=find0.matcher(key);
	  int q=0,st=buff.length();
	  while (matcher.find()) {
	   buff.append(key, q, matcher.start());
	   q = matcher.end();
	   String group = matcher.group(0);
	   Object o=null;
	   if (!sset.contains(group)) {
		String list[]=group.split("\\.", 2);
		String keyv=list[0];
		if (list.length > 1) {
		 if (!keyv.equals("section") && !key.equals(eqz)) {
		  cpy = (cpys)map.get(keyv);
		  if (cpy == null)return null;
		 }
		 o = cpy.m.get(list[1]);
		} else {
		 o = cpy.m.get("@define ".concat(keyv));
		 if (o == null)o = gl.get(keyv);
		}
		if (o == null)return null;
	   }
	   buff.append((String)o);
	  }
	  int m = key.length();
	  if (m > q)buff.append(key, q, m);
	  if (isNumber) {
	   key = buff.substring(st, buff.length());
	   buff.setLength(st);
	   cmpU cmp = new cmpU();
	   cmp.m = key;
	   cmp.t = key.charAt(0);
	   double b = cmp.cmp();
	   int intd=(int)b;
	   if (intd == b) buff.append(intd);
	   else buff.append(b);
	  }
	 }
	 j = i = ++n;
	} else break;
   } else break;
  }
  if (buff.length() == 0)return str;
  buff.append(str, j, str.length());
  return buff.toString();
 }
}
