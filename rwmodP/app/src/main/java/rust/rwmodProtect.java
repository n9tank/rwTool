package rust;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import rust.loaders;
import rust.iniobj;
import rust.loader;
import rust.rwmapOpt;
import rust.savedump;
import rust.zippack;
import rust.zipunpack;
import org.libDeflate.ParallelDeflate;
import org.libDeflate.ZipEntryM;
import org.libDeflate.ZipEntryOutput;
import org.libDeflate.ZipInputGet;
import org.libDeflate.ZipUtil;
import java.nio.charset.Charset;
import org.libDeflate.UIPost;
import java.io.IOException;
import java.io.BufferedInputStream;
public class rwmodProtect extends loaderManager implements Consumer {
 HashMap lowmap;
 ConcurrentHashMap resmap;
 ZipEntryOutput out;
 ConcurrentHashMap coeMap;
 int arr[];
 AtomicInteger adds[];
 String musicPath;
 int musicPut=-1; 
 String oggput; 
 boolean raw; 
 static int maxSplit;
 static int splitMod; 
 static HashSet skip;
 static char[] cr;
 static HashMap<String,Integer> Res;
 public rwmodProtect(File in, File ou, UIPost ui, boolean rw) {
  super(in, ou, ui);
  raw = rw;  
 }
 public loader getLoder(String str) throws Throwable {
  ZipEntry za=toPath(str);
  if (za == null)return null;
  str = za.getName();
  return addLoder(za, str, str, null, getType(str) == 4);
 }
 public static void init(HashMap<String,section> src)throws Exception {
  HashMap re=src.get("tmx").m;
  for (Map.Entry<String,Object> en:(Set<Map.Entry>)re.entrySet()) {
   HashSet add=new HashSet();
   Collections.addAll(add, ((String)en.getValue()).split(","));  
   en.setValue(add);
  }
  rwmapOpt.remove = re;
  HashMap<String,String> set;
  set = src.get("unit").m;
  String oldunits[]=set.get("old").split(",");
  int i=oldunits.length;
  HashMap old=new HashMap();
  while (--i > 0) {
   String v=oldunits[i];
   String k=oldunits[--i];
   old.put(k, v);
  }
  rwmapOpt.oldunits = old;
  oldunits = set.get("unit").split(",");
  i = oldunits.length;
  old = new HashMap();
  while (--i > 0) {
   String id=oldunits[i];
   String v=oldunits[--i];
   String team=oldunits[--i];
   rwmapOpt.key key=new rwmapOpt.key(v, Integer.parseInt(team));
   key.id = Integer.parseInt(id);
   old.put(key, key);
  }
  rwmapOpt.units = old;
  set = src.get("dump").m;
  zipunpack.boomlen = Integer.parseInt(set.get("boomlen"));    
  set = src.get("ini").m;
  String str[]=set.get("head").split(",");
  int len=str.length;  
  if (len >= 1) {
   int[] irr=new int[len];
   while (--len >= 0)irr[len] = Integer.parseInt(str[len]);
   zippack.head = irr;
  }
  char irr[]=set.get("split").toCharArray();
  if (irr.length > 0) {
   maxSplit = irr[0] - '/';
   splitMod = (int)Math.pow(maxSplit, irr[1] - '/');  
  }
  zippack.keepUnSize = set.get("keep").length() > 0;
  cr = set.get("chars").toCharArray();
  HashSet put=new HashSet();
  skip = put;
  Collections.addAll(put, set.get("skip").split(","));
  HashMap res=new HashMap();
  Res = res;
  putType(res, set, "image", -1);
  putType(res, set, "images", 0);
  putType(res, set, "music", 1);  
 }
 public static void putType(HashMap res, HashMap set, String key, int type) {
  String[] list=((String)set.get(key)).split(",");
  Integer rp=Integer.valueOf(type);
  for (String str:list)
   res.put(str, rp);
 }
 public static String path(String str) {
  return Paths.get(str).normalize().toString();
 }
 public ZipEntry toPath(String str) {
  ZipEntry za=Zip.getEntry(str);
  if (za == null) {
   String low=str.toLowerCase();
   za = (ZipEntry)lowmap.get(low);
  }
  return za;
 }
 void append(int i, StringBuilder buff) {
  if (i >= 0) {
   char[] irr=cr;
   int l = irr.length;
   do{
    int u=i % l;
    i /= l;
    buff.append(irr[u]);
   }while(i > 0);
  }
 }
 void appendName(int i, boolean checkOgg, StringBuilder buff) {
  int max=splitMod;
  if (max > 0) {
   int u=maxSplit;   
   int music=musicPut;   
   if (checkOgg && music >= 0 && (i - (i % u)) == music)i += u;
   int end=i % max;
   append(i /= max, buff);
   while ((max /= u) > 0) {
    buff.append('/');
    append(end % u, buff);
    end /= u;    
   }
  } else append(i, buff);
 }
 String safeName(int ini, StringBuilder buff) {
  buff.setLength(0);
  if (ini < 4) {
   AtomicInteger add=adds[--ini];
   int i=((int)add.incrementAndGet() - 1);
   appendName(i, ini == 2, buff);
   String ed;
   switch (ini) {
	case 1:
	 ed = ".wav";
	 break;
	case 2:
	 ed = ".ogg";
	 break;
	default:
	 ed = "";
	 break;
   }
   buff.append(ed);
  } else {
   int i=arr[ini -= 4]++;
   if (ini > 0) {  
    buff.append(oggput);
    buff.append('/');  
    if (ini > 1)buff.append("[noloop]");
   } 
   if (ini == 0) {
    appendName(i, false, buff);
    buff.append(".ini");
   } else {
    append(i, buff);
    buff.append(".ogg"); 
   }    
  }
  if (ini == 0)buff.append('/');  
  return buff.toString();
 }
 public boolean lod(loader ini) {
  //便于改动到并行加载，没多大优化，主要耗时为io流。
  ConcurrentHashMap coe=coeMap;
  loaders key=ini.copy;
  iniobj old=new iniobj();
  Object obj=coe.putIfAbsent(key, ini);
  if (obj == null) {
   lod(old, key.copy, ini.all);
   coe.put(key, old); 
  } else if (obj instanceof iniobj) {
   old.put((iniobj)obj, null);
  } else return false;
  ini.old = old;
  super.lod(ini);
  return true;
 }
 public void appstr(String str, int last, loader put, StringBuilder buff) {
  String add=put.str;
  int st=0;  
  if (put.task == null)buff.append("CORE:");
  else if (maxSplit > 0) {
   if (add.regionMatches(0, str, 0, last))
    st = add.lastIndexOf('/', add.length() - 3) + 1;
   else buff.append("ROOT:");
  }
  buff.append(add, st, add.length());
  buff.append(',');
 } 
 public static iniobj em=new iniobj();
 public static boolean with(loader copy[], loader all) {
  if (copy != null) {
   for (loader lod:copy)
    if (lod.all == all || with(lod.copy.copy, all))
     return true;
  }
  return false;
 }
 void write(loader ini) throws Throwable {
  loader alls=ini.all;
  StringBuilder buff=new StringBuilder();
  StringBuilder bf=new StringBuilder();
  String file=ini.src;
  boolean ws=maxSplit > 0;  
  file = loader.getSuperPath(file);
  HashMap map=ini.ini;
  loaders copy=ini.copy;
  loader[] orr= copy.copy;
  if (orr.length > 0 || alls != null) {
   section cp=(section)map.get("core");
   HashMap core;
   if (cp == null) {
    cp = new section();
    cp.m = core = new HashMap();
    map.put("core", cp);
   } else core = cp.m;
   String str=ini.str;
   int last=str.lastIndexOf('/', str.length() - 3); 
   if (alls != null && !with(orr, alls))
    appstr(str, last, alls, buff);
   for (loader obj:orr)
    appstr(str, last, obj, buff);  
   buff.setLength(buff.length() - 1);
   core.put("copyFrom", buff.toString());
  }
  String str;
  iniobj put=ini.put;
  HashMap as=put.put;
  iniobj old=ini.old;
  if (old == null)old = em;
  HashMap oldsrc=old.put;
  for (Map.Entry<String,section>en:(Set<Map.Entry<String,section>>)as.entrySet()) {
   String ac=en.getKey();
   section cpys=en.getValue();
   HashMap asmap = cpys.m;
   section asold=(section)oldsrc.get(ac);
   HashMap oldmap;
   HashMap lastcoe;
   if (asold != null) {
    oldmap = asold.m;
    lastcoe = asold.coe;
   } else {
    oldmap = null;
    lastcoe = null;
   }
   section licp=(section)map.get(ac);
   HashMap list=licp == null ?null: licp.m;
   for (Map.Entry<String,String> en2:(Set<Map.Entry<String,String>>)asmap.entrySet()) {
    String key=en2.getKey();
    if (!skip.contains(key)) {
     String value=en2.getValue();
     boolean eq= oldmap != null && value.equals(oldmap.get(key)); 
     HashMap<String, Integer> res=rwmodProtect.Res;
     Object o=res.get(key);
     if (o != null) {
      int type = (Integer)o;
      String next=put.get(value, ac, cpys, buff);
      if (next != null) {
       String[] nowlist=AllPath(next, file, type, buff);
       boolean same=value.equals(next);
       String coe;
       eq &= same && (lastcoe != null && (coe = (String)lastcoe.get(key)) != null && Arrays.equals(nowlist, AllPath(next , coe, type, buff)));
       //补修宏绕过
       if (!same || !eq) {
        if (list == null) {
         section cp=new section();
         cp.m = list = new HashMap();
         map.put(ac, cp);
        }
        char c=0;
        if (type == 0)c = '*';
        else if (type > 0)c = ':';
        if (nowlist != null) {
         buff.setLength(0);
         for (String add:nowlist) {
          int st=ResTry(add, type <= 0, buff);
          if (st >= 0) {
           int i = 0;
           if (c != 0)i = add.lastIndexOf(c);
           if (i <= 0)i = add.length();
           if (ws)buff.append("ROOT:");
           str = add.substring(st, i);
           ZipEntry ze = toPath(path(str));
           if (ze != null) {
            String name=ze.getName();
			Object obj;
			do{
			 obj = resmap.putIfAbsent(name, "");
			}while(obj == "");
			if (obj == null) {
			 resmap.put(name, str = safeName(getType(name), bf));
			 ZipEntryM outen=ZipUtil.newEntry(str, type <= 0 ?0: 12);
             zippack.writeOrCopy(cre, Zip, ze, outen, raw);
            } else str = (String)obj;
		   }
		   buff.append(str);
		   if (i > 0)buff.append(add, i, add.length());
		  } else buff.append(add);
		  buff.append(',');
		 }
		 buff.setLength(buff.length() - 1);
		 value = buff.toString();
		}
		list.put(key, value);
	   }
	  }
	 }
	 if (list != null && eq)list.remove(key);
	}}}
  ini.with(cre, ini.str);
 }
 static int ResTry(String file, boolean isimg, StringBuilder buff) {
  int st=0;
  if (isimg) {
   if (file.startsWith("SHADOW:")) {
    st = 7;
   }
   if (file.startsWith("CORE:", st) || file.startsWith("SHARED:", st))st = -1;
   if (buff != null && st > 0)buff.append("SHADOW:");
  } else {
   if (file.startsWith("ROOT:"))return 0;
   int i = file.lastIndexOf(':');
   if (i < 0)i = file.length();
   i -= 4;
   if (!(file.regionMatches(true, i, ".ogg", 0, 4) || file.regionMatches(true, i, ".wav", 0, 4)))st = -1;
  }
  return st;
 }
 String[] AllPath(String str, String path, int type, StringBuilder buff) {
  //不予修复非法auto图像
  if (str.length() == 0 || str.equalsIgnoreCase("none") || str.equals("IGNORE") || str.equalsIgnoreCase("auto"))
   return null;
  str = str.replace('\\', '/');
  String list[];
  buff.setLength(0);
  boolean ru=false;
  list = type < 0 ?new String[]{str}: str.split(",");
  int l=list.length,m=0;
  do {
   buff.setLength(0);
   str = list[m].trim();
   int st=ResTry(str, type <= 0, buff);
   boolean tag=st >= 0;
   ru = ru || tag;
   if (tag) {
    if (str.startsWith("ROOT:", st)) {
     st += 5;
     path = rootPath;
    }
    if (type <= 0) {
     boolean shaow=st > 0;
     if (str.startsWith("SHADOW:", st)) {
      st += 7;
      if (!shaow)buff.append("SHADOW:");
     }
    }
    if (st != 0)str = str.substring(st);
    str = str.replaceFirst("^/+", "");
    buff.append(path);
   }
   buff.append(str);
   str = buff.toString();
   list[m] = buff.toString();
  }while(++m < l);
  if (!ru)list = null;
  return list;
 }
 int getType(String file) {
  int i=file.length() - 4;
  int ed=i;
  if (file.endsWith("/"))--ed;
  if (file.regionMatches(true, ed, ".ini", 0, 4)) {
   return 4;
  } else if (file.regionMatches(true, ed, ".tmx", 0, 4) || file.regionMatches(true, ed - 4, "_map.png", 0, 8))
   return 0;
  String path=musicPath;
  if (file.regionMatches(true, i, ".ogg", 0, 4)) {
   if (path != null && file.startsWith(path)) {
    if (file.indexOf("[noloop]", path.length()) < 0)return 5;
    return 6;
   } else {
    return 3;
   }
  } else if (file.regionMatches(true, i, ".wav", 0, 4)) {
   return 2;
  }
  return 1;
 }
 volatile int is;
 public void accept(Object o) {
  switch (is) {
   case 0:
	loader lod=(loader)o;
	lod.put.as();
	break;
   case 1:
	try {
	 write((loader)o);
	} catch (Throwable e) {
	 is = 2;
     uih.onError(e);
	}
	break;
  }
 }
 public void end() {
  List vl=Arrays.asList(Zipmap.values().toArray());
  vl.parallelStream().forEach(this);
  is = 1;
  Collections.shuffle(vl);
  StringBuilder bf=new StringBuilder();
  for (Object obj:vl) {
   loader lod=(loader)obj;
   lod.str = safeName(lod.isini ?4: 1, bf);
  }   
  vl.parallelStream().forEach(this);
  try {
   cre.close();
  } catch (IOException e) {}
 }
 public Object call() {
  resmap = new ConcurrentHashMap();
  AtomicInteger[] add=new AtomicInteger[3];
  adds = add;
  int i=3;
  while (i > 0)add[--i] = new AtomicInteger();
  arr = new int[4];
  HashMap lows=new HashMap();
  lowmap = lows;
  coeMap = new ConcurrentHashMap();
  StringBuilder mbuff = new StringBuilder();
  try {
   ZipFile zip=new ZipFile(In);
   Zip = zip;
   out = zippack.enZip(Ou);
   ParallelDeflate cr = new ParallelDeflate(out, true);
   cr.on = new UiHandler(cr.pool, cr, back, zip);
   cre = cr;
   String name=null;
   HashSet rset=new HashSet();
   Enumeration<? extends ZipEntry> zipEntrys=zip.entries();
   do{
    ZipEntry zipEntry=zipEntrys.nextElement();
    String fileName=zipEntry.getName();
    String root=loader.getSuperPath(fileName);
    if (!rset.add(root) && (name == null || root.length() < name.length()))name = root;
    lows.putIfAbsent(fileName.toLowerCase(), zipEntry);
   }while(zipEntrys.hasMoreElements());
   rootPath = name;
   ZipEntry inf=toPath(name.concat("mod-info.txt"));
   if (inf != null) {
    loader ini=new loader();
    InputStream in=zip.getInputStream(inf);
    if (inf.getMethod() > 0)in = new BufferedInputStream(in, Math.min(8192, in.available()));
    HashMap info=loader.load(in);
    section cp=(section)info.get("music");
    if (cp != null) {
     HashMap map=cp.m;
     String str =(String)map.get("sourceFolder");
     if (str != null) {
      str = str.replace("\\", "/").replaceFirst("^/+", "");
      if (str.length() > 0 && !str.endsWith("/"))str = str.concat("/");
      musicPath = str;
      int max=maxSplit;      
      appendName(musicPut = new Random().nextInt(5 * Math.max(1, max)), false, mbuff);
      if (max > 0)mbuff.setLength(mbuff.length() - 2);    
      map.put("sourceFolder", oggput = mbuff.toString());
     }
    }
    ini.ini = info;
    ini.with(cre, "mod-info.txt/");
   }
   ArrayList<ZipEntry> ogg=new ArrayList();
   zipEntrys = zip.entries();
   do{
    ZipEntry zipEntry=zipEntrys.nextElement();
    name = zipEntry.getName();
	int type=getType(name);
	if (type == 4) {
	 addLoder(zipEntry, name, name, null, true);
	} else if (type == 0) {
     zippack.writeOrCopy(cre, zip, zipEntry, ZipUtil.newEntry(loader.getName(name).concat("/"), 12), raw);
	} else if (type >= 5) {
     ogg.add(zipEntry);
    }
   }while(zipEntrys.hasMoreElements());
   Collections.shuffle(ogg);
   for (ZipEntry en:ogg)
    zippack.writeOrCopy(cre, zip, en, ZipUtil.newEntry(safeName(getType(en.getName()), mbuff), 12), raw);
  } catch (Throwable e) {
   uih.onError(e);
  }
  uih.pop();
  return null;
 }
}
