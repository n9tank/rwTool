package rust;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;
import org.libDeflate.*;
import rust.*;
public class rwmodProtect extends loaderManager implements Consumer {
 HashMap lowmap;
 ConcurrentHashMap resmap;
 ZipEntryOutput out;
 HashMap oldobj;
 int arr[];
 AtomicInteger safeInt[];
 String musicPath;
 int oggindex=-1; 
 String oggput; 
 boolean raw;
 static int BGMShortCharCounts;
 static int splitcous;
 static int splitdeep;
 static int splitpow; 
 static char[] cr;
 static HashMap<String,Integer> Res;
 public rwmodProtect(File in, File ou, UIPost ui, boolean rw) {
  super(in, ou, ui);
  raw = rw;  
 }
 public loader getLoder(String str) throws Throwable {
  zipEntry za=toPath(str);
  if (za == null)return null;
  str = za.name;
  return addLoder(za, str, str, null, getType(str) == 4);
 }
 public static HashSet toSet(String str) {
  String[] list=str.split(",");
  int len=list.length;
  HashSet add=new HashSet((len << 2 / 3) + 1);
  Collections.addAll(add, list);
  return add;
 }
 public static void init(HashMap<String,section> src)throws Exception {
  rwmapOpt.init(src);
  HashMap<String,String> set = src.get("ini").m;
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
  loader.boolset = toSet(set.get("bool"));
  zippack.zip64enmode = set.get("end").length() > 0;
  char irr[]=set.get("split").toCharArray();
  if (irr.length > 0)
   splitpow = (int)Math.pow(splitcous = irr[0] - '/', splitdeep = irr[1] - '/');  
  BGMShortCharCounts = Integer.parseInt(set.get("BGMS"));
  cr = set.get("chars").toCharArray();
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
 public zipEntry toPath(String str) {
  zipEntry za=Zip.ens.get(str);
  if (za == null) {
   String low=str.toLowerCase();
   if (low != str)
    za = (zipEntry)lowmap.get(low);
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
  int u=splitcous;   
  if (u > 0) {
   int max=splitpow;
   int music=oggindex;
   if (checkOgg && music >= 0 && i >= music)
    i += u;
   int end=i % max;
   append(i /= max, buff);
   int cou=splitdeep;
   while (--cou >= 0) {
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
 public void appstr(String str, loader put, StringBuilder buff) {
  String add=put.str; 
  if (put.task == null)buff.append("CORE:");
  else if (splitcous > 0)
   buff.append("ROOT:");
  buff.append(add);
  buff.append(',');
 } 
 public static boolean with(loader copy[]) {
  loader all=copy[0];
  if (copy != null) {
   for (int i=1,len=copy.length;i < len;++i) {
    loader key[]=copy[i].copy.copy;
    if (key[0] == all || with(key))
     return true;
   }
  }
  return false;
 }
 public HashMap[] merge(loaders key) throws IOException {
  loader[] list=key.copy;
  int len=list.length;
  HashMap copy[]=new HashMap[len];
  Comparables cms=new Comparables();
  int c=0;
  wh:
  for (int i=0;i < len;) {
   int v=len;
   int n;
   for (;(n = (v - 1)) > i;v = n) {
    cms.set(list, list[i] == null ?1: i, v);
    loader obj=(loader) oldobj.get(cms);
    if (obj != null) {
	 HashMap map=obj.old;
     if (map == null)
	  return null;
     i = v;
     copy[c++] = map;
     continue wh;
    }
   }
   loader lod=list[i++];
   if (lod != null)
    copy[c++] = lod.ini;
  }
  return copy;
 }
 boolean write(loader ini) throws Throwable {
  loaders copy=ini.copy;
  boolean ru=true;
  loader[] orr=copy.copy;
  for (loader lod:orr) {
   if (lod != null)
    ru &= !lod.type;
  }
  if (!ru)return false;
  HashMap[] maps=merge(copy);
  if (maps == null)return false;
  HashMap oldsrc=null;
  int len=maps.length;
  if (len > 1 && maps[1] != null) {
   oldsrc = new HashMap();
   for (;--len >= 0;) {
	HashMap lod=maps[len];
	if (lod != null)
	 iniobj.put(oldsrc, lod);
   }
  } else oldsrc = maps[0];
  ini.old = oldsrc;
  StringBuilder buff=new StringBuilder();
  StringBuilder bf=new StringBuilder();
  String file=loader.getSuperPath(ini.src);
  HashMap map=ini.ini;
  loader alls=orr[0];
  if (orr.length > 1 || alls != null) {
   section cp=(section)map.get("core");
   HashMap core;
   if (cp == null) {
    cp = new section();
    cp.m = core = new HashMap();
    map.put("core", cp);
   } else core = cp.m;
   String str=ini.str;
   if (!with(orr))
    appstr(str, alls, buff);
   for (int i=1,size=orr.length;i < size;++i)
    appstr(str, orr[i], buff);
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
     boolean iscpoy=ascopy != null && value.equals(ascopy.get(key));
     HashMap<String, Integer> res=rwmodProtect.Res;
     Object o=res.get(key);
     if (o != null) {
      int type = (Integer)o;
      String next=put.get(value, ac, cpys, buff);
      if (next != null) {
	   zipunpack.name pathret=AllPath(next, file, type, buff, bf);
       Object path=pathret.name;
       boolean withDefine=!value.equals(next);
       String oldPathcopy=oldmap == null ?null: iniobj.copyValue(oldmap, (String)oldmap.get("@copyFromSection"), key);
       if (!(eq || path.equals(oldPath)) && 
       //结果不相同
           !(pathret.conts && (value.equals(oldPath) || value.equals(oldPathcopy))) && 
       //如果在同一个宏的情况下，上次的值与现在的值都不属于真实路径 
           (oldPath != null ||
       //上次的衍射如果有值
           (withDefine && !path.equals(oldPathcopy)
       //路径是否与上次复制的路径相等
           || !iscpoy))) {
        //键是否是从节复制过来的
        if (list == null) {
         section cp=new section();
         cp.m = list = new HashMap();
         map.put(ac, cp);
        }
        eq = false;
		list.put(key, pathret.conts ?value: path);
	   } else eq = true;
	  }
	 } else if (!"x".equals(key) && !"y".equals(key)) eq |= oldPath == null && iscpoy;
	 if (list != null && eq)list.remove(key);
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
  if (splitcous > 0)buff.append("ROOT:");
  String str = add.substring(0, i);
  zipEntry ze = toPath(str);
  if (ze != null) {
   String name=ze.name;
   Object obj = resmap.put(name, "");
   if (obj == null) {
	resmap.put(name, str = safeName(getType(name), bf));
	ZipEntryM outen=ZipUtil.newEntry(str, 12);
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
 zipunpack.name AllPath(String str, String path, int type, StringBuilder buff, StringBuilder bf) throws Throwable {
  //不予修复非法auto图像
  if (str.length() == 0 || str.equalsIgnoreCase("none") || str.equals("IGNORE") || str.equalsIgnoreCase("auto"))
   return new zipunpack.name(str, true);
  str = str.replace('\\', '/');
  buff.setLength(0);
  String list[] = type < 0 ?new String[]{str}: str.split(",");
  int l=list.length,m=0;
  boolean useReal=false;
  do {
   str = list[m].trim();
   int st=ResTry(str, type <= 0, buff);
   useReal |= st >= 0;
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
  return new zipunpack.name(buff.toString(), !useReal);
 }
 int getType(String file) {
  int i=file.length() - 4;
  int ed=i;
  //if (file.endsWith("/"))--ed;
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
 //用于决定加载循序，减少空检查导致的线程切换性能
 public static void addAll(Collection<loader> list){
  for (loader lod:list)
   addAll(lod, 0);
 }
 public static void addAll(loader lod, int cou){
  int lvl=lod.lvl;
  if (cou > lvl){
   int n=cou + 1;
   for (loader ini:lod.copy.copy){
	if (ini != null)
	 addAll(ini, n);
   }
   lod.lvl = cou;
  }
 }
 public static void set(Collection<loader> list, ArrayList qlist[]){
  for (loader lod:list){
   int lvl=lod.lvl;
   if (lvl >= 0){
	ArrayList mlist=qlist[lvl];
	if (mlist == null)
	 qlist[lvl] = mlist = new ArrayList();
	mlist.add(lod);
	lod.lvl = -1;
   }
  }
 }
 public int endtype;
 public void end() {
  switch (endtype++) {
   case 0:
	//不用java默认的ForkJoinPool.commonPool()
	ParallelDeflate.pool.execute(new Runnable(){
	  public void run() {
	   List iniList = Arrays.asList(Zipmap.values().toArray());
	   rwmodProtect rwmod= rwmodProtect.this;
	   iniList.parallelStream().forEach(rwmod);
	   Collections.shuffle(iniList);//用于提升随机性
	   StringBuilder bf=new StringBuilder();
	   for (Object obj:iniList) {
		loader lod=(loader)obj;
		lod.str = safeName(lod.isini ?4: 1, bf);
		loaders lods=lod.copy;
		loader copy[]=lods.copy;
		int off=(copy[0] == null ?1: 0);
		int len=copy.length;
		if (len - off > 1){
		 Comparables ckey=new Comparables();
		 ckey.set(copy, off, len);
		 oldobj.putIfAbsent(ckey, lod);
		}		 
	   }
	   ErrorHandler err = new ErrorHandler(UiHandler.ui_pool, rwmod);
	   err.ui = back;
	   uih = err;
	   try { 
		Collection list=oldobj.values();
		addAll(list);
		addAll(iniList);
		ArrayList qlist[]=new ArrayList[10];
		set(list, qlist);
		set(iniList, qlist);
		for (int i=10;--i >= 0;){
		 ArrayList mlist=qlist[i];
		 if (mlist != null){
		  for (loader lod:(ArrayList<loader>)mlist)
		   new iniTask(rwmod, lod);
		 }
		}
	   } catch (Exception e) {
	   }
	   err.pop();
	  }
	 });
    break;
   case 1:
    uih = null;
    cre.on.pop();
    break;
   default:
    cre.end();
    UiHandler.close(Zip);
    break;
  }
 }
 public Object call() {
  oldobj = new HashMap();
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
   zipFile zip=new zipFile(In);
   Zip = zip;
   out = zippack.enZip(Ou);
   final ParallelDeflate cr = new ParallelDeflate(out);
   ErrorHandler err=new ErrorHandler(cr.pool, this);
   err.ui = back;
   cr.on = err;
   cre = cr;
   String name=null;
   HashSet rset=new HashSet();
   for (zipEntry zipEntry:zip.ens.values()) {
    String fileName=zipEntry.name;
    String root=loader.getSuperPath(fileName);
    if (!rset.add(root) && (name == null || root.length() < name.length()))
     name = root;
    String icase=fileName.toLowerCase();
    if (icase != fileName)
     lows.putIfAbsent(icase, zipEntry);
   }
   rootPath = name;
   zipEntry inf=toPath(name.concat("mod-info.txt"));
   if (inf != null) {
    loader ini=new loader();
    HashMap<String, section> info=loader.load(ZipInputGet.reader(zip, inf, StandardCharsets.UTF_8));
    section cp=info.get("music");
    if (cp != null) {
     HashMap map=cp.m;
     String str =(String)map.get("sourceFolder");
     if (str != null && str.length() > 0) {
      str = str.replace('\\', '/');
      if (!str.endsWith("/"))
       str = str.concat("/");
      musicPath = str;
      int max=splitcous;
      int oggput=new Random().nextInt(BGMShortCharCounts * Math.max(1, max) + 1);
      oggindex = oggput;
      appendName(oggput, false, mbuff); 
      if (max > 0)
       mbuff.setLength(mbuff.length() - 2);
      map.put("sourceFolder", this.oggput = mbuff.toString());
     }
    }
    ini.ini = info;
    ini.with(cre, "mod-info.txt/");
   }
   ArrayList<zipEntry> ogg=new ArrayList();
   for (zipEntry zipEntry:zip.ens.values()) {
    name = zipEntry.name;
	int type=getType(name);
	boolean istem=name.regionMatches(true, name.length() - 18, "all-units.template", 0, 18);
	if (type == 4 || istem) {
	 addLoder(zipEntry, name, name, null, !istem);
	} else if (type == 0) {
     zippack.writeOrCopy(cre, zip, zipEntry, ZipUtil.newEntry(loader.getName(name).concat("/"), 12), raw);
	} else if (type >= 5) {
     ogg.add(zipEntry);
    }
   }
   Collections.shuffle(ogg);
   for (zipEntry en:ogg)
    zippack.writeOrCopy(cre, zip, en, ZipUtil.newEntry(safeName(getType(en.name), mbuff), 12), raw);
  } catch (Throwable e) {
   uih.onError(e);
  }
  uih.pop();
  return null;
 }
}
