package rust;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
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
import org.libDeflate.Canceler;
import org.libDeflate.ErrorHandler;
import org.libDeflate.ParallelDeflate;
import org.libDeflate.UIPost;
import org.libDeflate.ZipEntryM;
import org.libDeflate.ZipEntryOutput;
import org.libDeflate.ZipUtil;
import rust.iniobj;
import rust.loader;
import rust.loaders;
import rust.rwmapOpt;
import rust.zippack;
import rust.zipunpack;
public class rwmodProtect extends loaderManager implements Consumer {
 HashMap lowmap;
 ConcurrentHashMap resmap;
 ZipEntryOutput out;
 ConcurrentHashMap<loaders,HashMap> oldobj;
 int arr[];
 AtomicInteger safeInt[];
 String musicPath;
 int musicPut=-1; 
 String oggput; 
 boolean raw;
 static int BGMShortCharCounts;
 static int maxSplit;
 static int splitMod; 
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
  rwmapOpt.init(src);
  HashMap<String,String> set = src.get("ini").m;
  zipunpack.dumpMaxSize = Integer.parseInt(set.get("dumpMax"));    
  String str=set.get("head");
  if (str.length() > 0) {
   String[] list = str.split(",");
   int i = 0,len = list.length;
   int[] irr=new int[len];
   do{
	irr[i] = Integer.parseInt(list[i]);
   }while(++i < len);
   zippack.head = irr;
  }
  char irr[]=set.get("split").toCharArray();
  if (irr.length > 0) {
   maxSplit = irr[0] - '/';
   splitMod = (int)Math.pow(maxSplit, irr[1] - '/');  
  }
  zippack.keepSize = set.get("keepSize").length() > 0;
  BGMShortCharCounts = Integer.parseInt(set.get("BGMS"));
  cr = set.get("chars").toCharArray();
  HashSet put=new HashSet();
  HashMap res=new HashMap();
  Res = res;
  putType(res, set, "image", -1);
  putType(res, set, "images", 0);
  putType(res, set, "music", 1);  
 }
 public static void putType(HashMap res, HashMap map, String key, int type) {
  String[] list=((String)map.get(key)).split(",");
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
  //0 copy oly
  //1 res
  //2 wav
  //3 ogg
  //4 ini
  //5 music
  //6 music [noloop]
  if (ini < 4) {
   int i=safeInt[--ini].incrementAndGet() - 1;
   boolean isogg=ini == 2;
   appendName(i, isogg, buff);
   if (ini == 1)buff.append(".wav");
   else if (isogg)buff.append(".ogg");
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
 public static boolean with(loader copy[], loader all) {
  if (copy != null) {
   for (loader lod:copy) {
	loaders key=lod.copy;
    if (key.all == all || with(key.copy, all))
	 return true;
   }
  }
  return false;
 }
 UiHandler asyncOut;
 public boolean asyncAdd(loader lod) throws IOException {
  boolean ok=!lod.type;
  if (ok || lod.inSet())return ok;
  new iniTask(this, lod, asyncOut);
  return ok;
 }
 boolean write(loader ini) throws Throwable {
  loaders copy=ini.copy;
  HashMap oldsrc=oldobj.get(copy);
  if (oldsrc == null) {
   boolean ru=true;
   loader all=copy.all;
   if (all != null)ru &= asyncAdd(all);
   loader[] list=copy.copy;
   for (loader lod:list)ru &= asyncAdd(lod);
   if (!ru)return false;
   oldsrc = new HashMap();
   for (int i=list.length;--i >= 0;) {
	loader lod=list[i];
	iniobj.put(oldsrc, lod.ini);
   }
   if (all != null)iniobj.put(oldsrc, all.ini); 
   oldobj.put(copy, oldsrc);
  }
  StringBuilder buff=new StringBuilder();
  StringBuilder bf=new StringBuilder();
  CharSequence file=loader.getSuperPath(ini.src);
  HashMap map=ini.ini;
  loader alls=copy.all;
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
  iniobj put=ini.put;
  HashMap as=put.put;
  for (Map.Entry<String,section>en:(Set<Map.Entry<String,section>>)as.entrySet()) {
   String ac=en.getKey();
   section cpys=en.getValue();
   HashMap asmap = cpys.m;
   HashMap ascopy=cpys.copy;
   section asold=oldsrc == null ?null: (section)oldsrc.get(ac);
   HashMap oldmap=asold == null ?null: asold.m;
   section licp=(section)map.get(ac);
   HashMap list=licp == null ?null: licp.m;
   for (Map.Entry<String,String> en2:(Set<Map.Entry<String,String>>)asmap.entrySet()) {
    String key=en2.getKey();
    if (!"@copyFrom_skipThisSection".equals(key)) {
     String value=en2.getValue();
	 String oldPath=null;
	 boolean eq= oldmap != null && value.equals(oldPath = (String)oldmap.get(key)); 
     HashMap<String, Integer> res=rwmodProtect.Res;
     Object o=res.get(key);
     if (o != null) {
      int type = (Integer)o;
      String next=put.get(value, ac, cpys, buff);
      if (next != null) {
	   String path=AllPath(next, file, type, buff, bf);
       if (!(eq || path.equals(oldPath)) &&
		   (oldPath != null || (!value.equals(next) && !path.equals(iniobj.copyValue(oldmap, (String)oldmap.get("@copyFromSection"), key))
		   || (ascopy == null || !value.equals(ascopy.get(key)))))) {
        if (list == null) {
         section cp=new section();
         cp.m = list = new HashMap();
         map.put(ac, cp);
        }
		list.put(key, path);
	   }
	   eq = false;
	  } else eq = true;
	 }
	 if (list != null && eq)list.remove(key);
	 //这里不能直接删除复制的重复键，因为@copyFromSection允许动态变化
	}}}
  ini.with(cre, ini.str);
  ini.type = false;
  return true;
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
 void addRealPath(String add, int type, StringBuilder buff, StringBuilder bf) throws Throwable {
  int i = 0;
  char c=0;
  if (type == 0)c = '*';
  else if (type > 0)c = ':';
  if (c != 0)i = add.lastIndexOf(c);
  if (i <= 0)i = add.length();
  if (maxSplit > 0)buff.append("ROOT:");
  String str = add.substring(0, i);
  ZipEntry ze = toPath(str);
  if (ze != null) {
   String name=ze.getName();
   Object obj = resmap.put(name, "");
   if (obj == null) {
	resmap.put(name, str = safeName(getType(name), bf));
	ZipEntryM outen=ZipUtil.newEntry(str, type <= 0 ?0: 12);
	zippack.writeOrCopy(cre, Zip, ze, outen, raw);
   } else {
	while (obj == "")
	 obj = resmap.get(name);
	//CSA自旋 
	str = (String)obj;
   }
  }
  buff.append(str);
  if (i > 0)buff.append(add, i, add.length());
 }
 String AllPath(String str, CharSequence path, int type, StringBuilder buff, StringBuilder bf) throws Throwable {
  //不予修复非法auto图像
  if (str.length() == 0 || str.equalsIgnoreCase("none") || str.equals("IGNORE") || str.equalsIgnoreCase("auto"))
   return str;
  str = str.replace('\\', '/');
  buff.setLength(0);
  String list[] = type < 0 ?new String[]{str}: str.split(",");
  int l=list.length,m=0;
  do {
   str = list[m].trim();
   int st=ResTry(str, type <= 0, buff);
   if (st >= 0) {
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
	st = buff.length();
    buff.append(path);
   }
   buff.append(str);
   //注意不要使用/./这种路径
   if (st >= 0) {
	String add=buff.substring(st);
	buff.setLength(st);
	addRealPath(path(add), type, buff, bf);
   }
   buff.append(',');
  }while(++m < l);
  buff.setLength(buff.length() - 1);
  return buff.toString();
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
 public void accept(Object obj) {
  loader lod=(loader)obj;
  lod.put.as();
 }
 public void end() {
  List vl=Arrays.asList(Zipmap.values().toArray());
  vl.parallelStream().forEach(this);
  Collections.shuffle(vl);
  StringBuilder bf=new StringBuilder();
  for (Object obj:vl) {
   loader lod=(loader)obj;
   lod.str = safeName(lod.isini ?4: 1, bf);
  }
  final UiHandler err = new UiHandler(UiHandler.ui_pool, null, back);
  err.can = new Canceler(){
   public void cancel() {
	err.cancel();
	rwmodProtect.this.cancel();
   }
   public void end() {
	UiHandler.close(cre);
   }
  };
  asyncOut = err;
  try {
   for (Object obj:vl)
	asyncAdd((loader)obj);
  } catch (Exception e) {
  }
  err.pop();
 }
 public Object call() {
  oldobj = new ConcurrentHashMap();
  resmap = new ConcurrentHashMap();
  arr = new int[3];
  AtomicInteger ato[]=new AtomicInteger[3];
  safeInt = ato;
  for (int i=0;i < ato.length;++i)
   ato[i] = new AtomicInteger();
  HashMap lows=new HashMap();
  lowmap = lows;
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
    String root=loader.getSuperPath(fileName).toString();
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
      str = str.replace('\\', '/').replaceFirst("^/+", "");
      if (str.length() > 0 && !str.endsWith("/"))str = str.concat("/");
      musicPath = str;
      int max=maxSplit;
      appendName(musicPut = new Random().nextInt(BGMShortCharCounts * Math.max(1, max) + 1), false, mbuff);
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
     zippack.writeOrCopy(cre, zip, zipEntry, ZipUtil.newEntry(loader.getName(name) + "/", 12), raw);
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
