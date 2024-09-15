package rust;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import carsh.log;
import com.googlecode.pngtastic.core.PngImage;
import com.googlecode.pngtastic.core.PngOptimizer;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import me.steinborn.libdeflate.LibdeflateCompressor;
import me.steinborn.libdeflate.LibdeflateJavaUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.util.Base64;

public class rwmap implements Runnable {
 public static class key implements Comparable {
  public int hash;  
  public String str;
//  public String type;  
  public int team;
  public int id;
  public key(String s, int t) {
   int h=17;   
   h = h * 31 + s.hashCode();
   h = h * 31 + t;
   //h*31+ty==null?0:ty.hashCode();    
   hash = h;   
   team = t;
   str = s; 
   //  type=ty;   
  }
  public int hashCode() {
   return hash;
  }
  public int compareTo(Object o) {
   if (o instanceof key) {
    key cp=(key)o;
    int u=team - cp.team;
    if (u != 0)return u;
    return str.compareTo(cp.str);
    /*if (u != 0)return u;
     if(type==cp.type)return 0;
     if(cp.type==null)return 1;
     return type.compareTo(cp.type);    */  
   }  
   return 1;     
  }
  public boolean equals(Object obj) {
   return compareTo(obj) == 0;
  }
 }
 ui ui;
 File in;
 File ou;
 static HashMap<String,HashSet> remove;
 static HashMap<key,key> units=new HashMap();
 static HashMap<String,String> oldunits;
 static{
  HashMap old=new HashMap();
  oldunits = old;
  old.put("tank", "Tank");
  old.put("extractor", "Extractor");
  old.put("airShip", "airship");
  old.put("turret", "Turret");
  old.put("antiAirTurret", "AntiAirTurret");
  old.put("helicopter", "Helicopter");
  old.put("artillery", "Artillery");
  old.put("turret_flamethrower", "Turret_flamethrower");
  old.put("laserTank", "lasertank");
  old.put("turretT2", "TurretT2");
  old.put("antiAirTurretT2", "AntiAirTurretT2");
  old.put("turret_artillery", "Turret_artillery");
  old.put("mammothTank", "mammothtank");
  old.put("turretT3", "TurretT3");
  old.put("experimentalTank", "experimentaltank");
  old.put("AntiNukeLaucher", "antiNukeLaucher");
  old.put("NukeLaucher", "nukeLaucher");
  add(270, "mechGun", 1);
  add(266, "lightGunship", 1);
  add(265, "scout", 1);
  add(262, "mechLaser", 1);
  add(261, "mechMissile", 1);
  add(258, "c_turret_t2_flame", 0);
  add(257, "c_turret_t1_artillery", 0);
  add(256, "c_turret_t2_gun", 0);
  add(254, "c_turret_t2_flame", 1);
  add(253, "c_turret_t1_artillery", 1);
  add(252, "c_turret_t2_gun", 1);
  add(247, "nautilusSubmarine", -1);
  add(246, "heavyMissileShip", -1);
  add(245, "nautilusSubmarineSurface", -1);
  add(244, "experiementalCarrier", -1);
  add(240, "heavyHoverTank", -1);
  add(239, "c_laserTank", -1);
  add(238, "gunShip", -1);
  add(237, "dropship", -1);
  add(236, "c_mammothTank", -1);
  add(235, "c_experimentalTank", -1);
  add(233, "fabricator", -1);
  add(232, "repairbay", -1);
  add(231, "experimentalLandFactory", -1);
  add(230, "laserDefence", -1);
  add(229, "battleShip", -1);
  add(228, "gunBoat", -1);
  add(227, "hovercraft", -1);
//add(226,"scoutShip",-1);
  add(225, "attackSubmarine", -1);
  add(224, "extractorT1", -1);
  add(223, "airFactory", -1);
  add(222, "landFactory", -1);
  add(221, "seaFactory", -1);
  add(219, "amphibiousJet", -1);
  add(218, "c_interceptor", -1);
  add(217, "c_helicopter", -1);
  add(215, "c_antiAirTurret", -1);
  add(214, "c_turret_t1", -1);
  add(213, "commandCenter", -1);
  add(212, "hoverTank", -1);
  add(211, "heavyTank", -1);
  add(210, "c_artillery", -1);
  add(209, "c_tank", -1);
  add(208, "builder", -1);
  add(207, "builderShip", -1);
  add(190, "bugMeleeLarge", 1);
  add(189, "bugMeleeSmall", 1);
  add(188, "crystalResource", -1);
  add(184, "bugRanged", 1);
  add(183, "bugNest", 1);
  add(182, "bugSpore", 1);
  add(181, "bugMelee", 1);
  add(180, "ladyBug", 1);
  add(178, "commandCenter", 9);
  add(177, "builder", 9);
  add(175, "bugRanged", 0);
  add(174, "bugNest", 0);
  add(173, "bugSpore", 0);
  add(172, "bugMelee", 0);
  add(171, "ladyBug", 0);
  add(160, "commandCenter", 8);
  add(159, "builder", 8);
  add(158, "commandCenter", 7);
  add(157, "builder", 7);
  add(156, "commandCenter", 6);
  add(155, "builder", 6);
  add(154, "commandCenter", 5);
  add(153, "builder", 5);
  add(148, "bugRanged", 4);
  add(147, "bugNest", 4);
  add(146, "bugSpore", 4);
  add(145, "bugMelee", 4);
  add(144, "ladyBug", 4);
  add(141, "commandCenter", 4);
  add(136, "builder", 4);
  add(125, "antiNukeLauncherC", 3);
  add(124, "nukeLauncherC", 3);
  add(120, "gunBoat", 3);
  add(119, "hovercraft", 3);
//add(118,"scoutShip",3);
  add(109, "c_helicopter", 3);
  add(105, "commandCenter", 3);
  add(102, "heavyHoverTank", 3);
  add(101, "experimentalSpider", 3);
  add(100, "builder", 3);
  add(98, "extractorT1", 2);
  add(97, "airFactory", 2);
  add(96, "landFactory", 2);
  add(93, "combatEngineer", 1);
  add(92, "missileTank", 1);
  add(91, "heavyBattleship", 1);
  add(90, "experimentalDropship", 1);
  add(89, "c_antiAirTurret", 2);
  add(88, "c_turret_t1", 2);
  add(87, "commandCenter", 2);
  add(85, "heavyTank", 2);
  add(84, "heavyHoverTank", 2);
  add(83, "c_tank", 2);
  add(82, "builder", 2);
  add(81, "experimentalSpider", 2);
  add(80, "antiNukeLaucherC", 1);
  add(79, "nukeLauncherC", 1);
  add(78, "heavyHoverTank", 1);
  add(77, "c_laserTank", 1);
  add(76, "gunShip", 1);
  add(75, "dropship", 1);
  add(74, "c_mammothTank", 1);
  add(73, "c_experimentalTank", 1);
  add(72, "experimentalHoverTank", 1);
  add(71, "fabricator", 1);
  add(70, "repairbay", 1);
  add(69, "experimentalLandFactory", 1);
  add(68, "laserDefence", 1);
  add(67, "battleShip", 1);
  add(66, "gunBoat", 1);
  add(65, "hovercraft", 1);
//add(64,"scoutShip",1);
  add(63, "attackSubmarine", 1);
  add(62, "extractorT1", 1);
  add(61, "airFactory", 1);
  add(60, "landFactory", 1);
  add(59, "seaFactory", 1);
  add(58, "mechFactory", 1);
  add(57, "amphibiousJet", 1);
  add(56, "c_interceptor", 1);
  add(55, "c_helicopter", 1);
  add(54, "experimentalSpider", 1);
  add(53, "c_antiAirTurret", 1);
  add(52, "c_turret_t1", 1);
  add(51, "commandCenter", 1);
  add(50, "hoverTank", 1);
  add(49, "heavyTank", 1);
  add(48, "c_artillery", 1);
  add(47, "c_tank", 1);
  add(46, "builder", 1);
  add(45, "builderShip", 1);
  add(42, "mechBunker", 0);
  add(41, "mechMinigun", 0);
  add(40, "bomber", 0);
  add(39, "combatEngineer", 0);
  add(38, "missileTank", 0);
  add(37, "heavyBattleship", 0);
  add(36, "experimentalDropship", 0);
  add(35, "antiNukeLaucherC", 0);
  add(34, "nukeLauncherC", 0);
  add(33, "heavyHoverTank", 0);
  add(32, "c_laserTank", 0);
  add(31, "gunShip", 0);
  add(30, "dropship", 0);
  add(29, "c_mammothTank", 0);
  add(28, "c_experimentalTank", 0);
  add(27, "experimentalHoverTank", 0);
  add(26, "fabricator", 0);
  add(25, "repairbay", 0);
  add(24, "experimentalLandFactory", 0);
  add(23, "laserDefence", 0);
  add(22, "battleShip", 0);
  add(21, "gunBoat", 0);
  add(20, "hovercraft", 0);
//add(19,"scoutShip",0);
  add(18, "attackSubmarine", 0);
  add(17, "extractorT1", 0);
  add(16, "airFactory", 0);
  add(15, "landFactory", 0);
  add(14, "seaFactory", 0);
  add(13, "mechFactory", 0);
  add(12, "amphibiousJet", 0);
  add(11, "c_interceptor", 0);
  add(10, "c_helicopter", 0);
  add(9, "experimentalSpider", 0);
  add(8, "c_antiAirTurret", 0);
  add(7, "c_turret_t1", 0);
  add(6, "commandCenter", 0);
  add(5, "hoverTank", 0);
  add(4, "heavyTank", 0);
  add(3, "c_artillery", 0);
  add(2, "c_tank", 0);
  add(1, "builder", 0);
  add(0, "builderShip", 0);
 } 
 public static void add(int id, String str, int team) {
  key key=new key(str, team);
  key.id = id;
  units.putIfAbsent(key, key);
 }
 public rwmap(File i, File u, ui uo) {
  in = i;
  ou = u;
  ui = uo;
 }
 public static Node getFirst(int i, Node map) {
  NodeList list=map.getChildNodes();  
  for (int l=list.getLength();i < l;++i) {
   Node  item=list.item(i);
   if (item.getNodeType() == Node.ELEMENT_NODE)return item;
  }
  return null;
 }
 public static int Ipare(NamedNodeMap attr, String str) {
  Node  node= attr.getNamedItem(str);
  if (node == null)return -1;
  return Integer.parseInt(node.getNodeValue());
 }
 public static void remove(NamedNodeMap attr, String str) {
  if (attr.getNamedItem(str) != null)
   attr.removeNamedItem(str);
 }
 public static void addAttr(Document doc, NamedNodeMap attr, String name, String value) {
  Node result= doc.createAttribute(name);
  result.setNodeValue(value);
  attr.setNamedItem(result);
 }
 public static key getKey(Node node) {
  node = getFirst(0, node);
  if (node == null)return null;
  String unit=null;
  int team=-1;
  // String type=null;  
  NodeList list=node.getChildNodes();
  for (int i=list.getLength();--i >= 0;) {
   node = list.item(i);
   if (node.getNodeType() != Node.ELEMENT_NODE)continue;
   NamedNodeMap attr=node.getAttributes();
   String name=attr.getNamedItem("name").getNodeValue();
   Node   valuenode=attr.getNamedItem("value");
   String value=valuenode.getNodeValue();
   if (name.equals("unit")) {
    unit = value;
   } else if (name.equals("team")) {
    if (value.equals("none")) {
     valuenode.setNodeValue("-1") ;   
    } else team = Integer.parseInt(value);
   }/*else if(name.equals("type")){
    type=value;
    }*/
  }
  if (unit == null)return null;
  return new key(unit, team);
 }
 public static ByteBuffer getBuf(Node item, byte[] buf) throws Exception {
  NamedNodeMap attr=item.getAttributes();
  int w= Ipare(attr, "width");
  int h= Ipare(attr, "height");
  ByteBuffer buffer= ByteBuffer.allocateDirect(w * h << 2);
  Node data = getFirst(0, item);
  String dataValue = data.getTextContent().trim();
  //这个部分没有必要使用libDeflate加速，因为区块太小，所以不启用解码模块
  InputStream in =new ByteArrayInputStream(Base64.getDecoder().decode(dataValue));
  if (data.getAttributes().getNamedItem("compression").getNodeValue().equals("gzip"))in = new GZIPInputStream(in);
  else in = new InflaterInputStream(in);
  int len;  
  while ((len = in.read(buf)) > 0)buffer.put(buf, 0, len);
  in.close();
  buffer.flip();
  return buffer;
 }
 //压缩等级12，什么时候把图片搞搞？
 public static ByteBuffer deflate(ByteBuffer buf) {
  LibdeflateCompressor def=new LibdeflateCompressor(12);
  ByteBuffer ebuf=ByteBuffer.allocate(LibdeflateJavaUtils.getBufSize(buf.limit(), true));
  def.compress(buf, ebuf, 1);
  def.close();
  ebuf.flip();
  return ebuf;
 }
 //懒得搞并行放这里了
 //https://github.com/Timeree/RwMapCompressor
 public void run() {
  Throwable ex=null;
  try {
   DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
   Document document = docBuilder. parse(in);
   HashMap tiles = new HashMap();
   Node map= document.getFirstChild();
   Node pr= getFirst(0, map);
   int max=0;
   int unitid=0;   
   ByteOut out=new ByteOut();
   ArrayList layer=new ArrayList();
   Node unitnode=null;
   byte[] buf=new byte[8192];
   if ("properties".equals(pr.getNodeName()))map.removeChild(pr);
   NodeList nodeList =map.getChildNodes();
   PngOptimizer opt=new PngOptimizer();
   for (int i =nodeList.getLength();--i >= 0;) {
	Node item = nodeList.item(i);
    String type=item.getNodeName(); 
    if (type == null)continue; 
    String name; 
    if ("objectgroup".equals(type)) {
     if (item.getChildNodes().getLength() == 0) {
      map.removeChild(item);
      continue;        
     }       
     NodeList objlist=item.getChildNodes()  ;    
     if ((name = item.getAttributes().getNamedItem("name").getNodeValue()).equalsIgnoreCase("unitobjects") || name.equalsIgnoreCase("unitsobject")) {
      unitnode = item;       
     } else {
      HashSet pot=remove.get("point");   
      HashSet no=remove.get("none");            
      for (int i2=objlist.getLength();--i2 >= 0;) {
       Node next=objlist.item(i2);  
       if (next.getNodeType() == Node.ELEMENT_NODE) {                      
        NamedNodeMap  node=  next.getAttributes();
        Node nodetype=node.getNamedItem("type");
        String ltype=nodetype == null ?null: nodetype.getNodeValue().toLowerCase();         
        boolean none=nodetype == null || no.contains(ltype);
        if (none || pot.contains(ltype))  {       
         remove(node, "width") ;   
         remove(node, "height") ;           
        }
        if (none) {
         node.getNamedItem("x").setNodeValue("0");
         node.getNamedItem("y").setNodeValue("0");             
        }             
        if (nodetype == null) remove(node, "id") ;
       }
      }}
     continue;       
    } else if ("layer".equals(type)) {
     if ((name = item.getAttributes().getNamedItem("name").getNodeValue()).equalsIgnoreCase("set")) {
      map.removeChild(item);
      continue;
     }
     pr = item;
     ByteBuffer buffer=getBuf(item, buf);
     layer.add(item);                      
     layer.add(buffer);
     buffer.order(ByteOrder.LITTLE_ENDIAN);
     while (buffer.hasRemaining()) {
      int n=buffer.getInt() & 536870911;
      if (n > max)max = n;                
      tiles.putIfAbsent(n, name);
     }
     buffer.flip();
    } else if ("tileset".equals(type)) { 
     NodeList list=item.getChildNodes(); 
     NamedNodeMap attr=item.getAttributes();
     int first=Ipare(attr, "firstgid"); 
     Node src=item.getAttributes().getNamedItem("source");   
     if (src != null && src.getNodeValue().equals("units.tsx")) {
      unitid = first;  
      continue;
     }                        
     int c= Ipare(attr, "tilecount");
     Node node= getFirst(i + 1, map); 
     boolean hasnext;
     if (hasnext = node.getNodeName().equals("tileset"));
     if (c < 0) {
      if (hasnext)c = Ipare(node.getAttributes(), "firstgid");         
      else c = max;          
     } else c += first; 
     tag:         
     if (c >= 0) {
      if (first <= max) { 
       for (;--c >= first;)
        if (tiles.containsKey(c)) break tag;    
      }             
      map.removeChild(item);
      if (!hasnext)max = first;
      continue;        
     }
     for (int l=list.getLength();--l >= 0;) {
      Node next=list.item(l);
      if ("tile".equals(next.getNodeName())) {
       key key=getKey(next);
       if (key != null) {
        key.id = Ipare(next.getAttributes(), "id") + first;   
        tiles.putIfAbsent(key, key);
       }      
      }   
     }
     int tileWidth = Ipare(attr, "tilewidth");
     if (tileWidth == 20) attr.removeNamedItem("tilewidth") ;           
     int tileHeight = Ipare(attr, "tileheight");
     if (tileHeight == 20) attr.removeNamedItem("tileheight") ;                       
     Node tname=attr.getNamedItem("name");
     if (tname == null) continue;    
     NodeList tilesetList = item.getChildNodes();
     for (int i2= tilesetList.getLength();--i2 >= 0;) {
      Node child = tilesetList.item(i2);
      String childName = child.getNodeName();
      if ("terraintypes".equals(childName)) {
       item.removeChild(child);
       continue;            
      }              
      if ("properties".equals(childName)) {
       list = child.getChildNodes();    
       for (int i3=list.getLength();--i3 >= 0;) {                
        Node property = list.item(i3);
        if (property.getNodeType() == Node.ELEMENT_NODE) {  
         name = property.getAttributes().getNamedItem("name").getNodeValue();         
         if (name.equals("layer") || name.equals("forced_autotile")) {
          child.removeChild(property);
          continue;                  
         }                                     
         if ("embedded_png".equals(name)) {
          Node img= getFirst(i2 + 1, item);  
          if (img.getNodeName().equals("image"))item.removeChild(img);  
          int tilew=Ipare(attr, "columns");          
          int tilec=Ipare(attr, "tilecount"); 
          String tlname=null;
          int size=0;
          for (c = first + tilec;--c >= first;) {
           Object o;              
           if ((o = tiles.get(c)) != null) {
            if (tlname == null)tlname = (String)o;              
            ++size;
           }              
          } 
          if (size == 0) {
           map.removeChild(item);              
           continue;
          }
          if (tlname.equalsIgnoreCase("units")) {
           item.removeChild(property);
           continue;              
          }                  
          byte imgarr[] = Base64.getDecoder().decode(property.getTextContent().replaceAll("\\s", ""));
          Bitmap.Config cf=tlname.equalsIgnoreCase("items") ?Bitmap.Config.ARGB_8888: Bitmap.Config.RGB_565;
          BitmapFactory.Options options = new BitmapFactory.Options();
          options.inPreferredConfig = cf;
          Bitmap bmp=BitmapFactory.decodeByteArray(imgarr, 0, imgarr.length, options);
          Bitmap bm2= Bitmap.createBitmap(tileWidth, tileHeight * size, cf);                               
          Canvas cv= new Canvas(bm2);
          Paint pt= new Paint();
          pt.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));      
          int v=0,j=0;                   
          for (c = tilec;--c >= 0;) {
           Integer key=c + first;               
           if (tiles.containsKey(key)) {      
            int left=c % tilew * tileWidth;   
            int top=c / tilew * tileHeight;
            int n=v + tileHeight;
            tiles.put(key, j++ + first);                                                         
            cv.drawBitmap(bmp, new Rect(left, top, left + tileWidth, top + tileHeight), new Rect(0, v, tileWidth, n), pt);
            v = n;  
           }                           
          }
          out.reset();                      
          bm2.compress(Bitmap.CompressFormat.PNG, 100, out);
          bmp.recycle();
          bm2.recycle();                      
          PngImage png= opt.optimize(new PngImage(new ByteArrayInputStream(out.get(), 0, out.size())), false, 9); 
          out.reset();
          png.writeDataOutputStream(new DataOutputStream(out));
          ByteBuffer buff=Base64.getEncoder().encode(ByteBuffer.wrap(out.get(), 0, out.size()));
          property.setTextContent(new String(buff.array()));
         }
        }                     
       }
      }
     }
    }
   }
   if (unitnode != null) {
    ByteBuffer unitlayer=null;
    NamedNodeMap attr= map.getAttributes(); 
    int lh=Ipare(attr, "height");
    int lw=Ipare(attr, "width");        
    int h=Ipare(attr, "tileheight"); 
    int w=Ipare(attr, "tilewidth");        
    for (int i=0,l=layer.size();i < l;i += 2) {
     Node o=(Node)layer.get(i);
     if (o.getAttributes().getNamedItem("name").getNodeValue().equalsIgnoreCase("units")) {
      unitlayer = (ByteBuffer)layer.get(i + 1);
     }   
    }
    if (unitlayer == null) {
     unitlayer = ByteBuffer.allocate(lh * lw << 2);
     unitlayer.order(ByteOrder.LITTLE_ENDIAN);   
     Node addlayer=document.createElement("layer");   
     NamedNodeMap addattr= addlayer.getAttributes();    
     addAttr(document, addattr, "name", "units");  
     addAttr(document, addattr, "width", String.valueOf(lw));  
     addAttr(document, addattr, "height", String.valueOf(lh));
     Node data=document.createElement("data");   
     addattr = data.getAttributes();     
     addAttr(document, addattr, "encoding", "base64");
     addAttr(document, addattr, "compression", "zlib");  
     addlayer.appendChild(data); 
     map.appendChild(addlayer);                       
     layer.add(addlayer);
     layer.add(unitlayer);      
    }
    int first=max + 1;    
    int id=0;  
    if (unitid == 0) {
     Node addunits= document.createElement("tileset"); 
     map.insertBefore(addunits, pr);      
     NamedNodeMap addattr=addunits.getAttributes();   
     addAttr(document, addattr, "source", "units.tsx"); 
     addAttr(document, addattr, "firstgid", String.valueOf(first)); 
     unitid = first;
     first += 270;                 
    }
    Node unittiles=document.createElement("tileset");  
    NamedNodeMap  unitat=unittiles.getAttributes();    
    addAttr(document, unitat, "firstgid", String.valueOf(first));
    unittiles.appendChild(document.createElement("image"));         
    NodeList objunits=unitnode.getChildNodes();  
    for (int i2=objunits.getLength();--i2 >= 0;) {
     Node next=objunits.item(i2);
     remove:     
     if (next.getNodeType() == Node.ELEMENT_NODE) {
      NamedNodeMap nextattr=next.getAttributes();
      tag: {   
       int x=(Ipare(nextattr, "width") >>> 1) + (int)Float.parseFloat(nextattr.getNamedItem("x").getNodeValue()); 
       int y=(Ipare(nextattr, "height") >>> 1) + (int)Float.parseFloat(nextattr.getNamedItem("y").getNodeValue()); 
       if (x < 0 || y < 0 || x > w * lw || y > h * lh)break tag;   
       int index=(x / w + y / h * lw) << 2;
       if (unitlayer.getInt(index) != 0)break tag;
       key find=getKey(next);       
       key key=units.get(find); 
       boolean nounits;         
       if (nounits = key == null) {
        key = (key)tiles.putIfAbsent(find, find);
        if (key == null)key = find;          
        if (key.id == 0) {
         Node tile=document.createElement("tile"); 
         addAttr(document, tile.getAttributes(), "id", String.valueOf(id)); 
         Node posadd=document.createElement("properties");   
         Node por= document.createElement("property");
         NamedNodeMap poattr= por.getAttributes(); 
         addAttr(document, poattr, "name", "team");                 
         addAttr(document, poattr, "value", String.valueOf(key.team)); 
         posadd.appendChild(por);
         por = document.createElement("property");
         poattr = por.getAttributes();            
         addAttr(document, poattr, "name", "unit"); 
         String str=oldunits.get(key.str);          
         addAttr(document, poattr, "value", str == null ?key.str: str);          
         posadd.appendChild(por);
         tile.appendChild(posadd);         
         unittiles.appendChild(tile);                   
         int ids= key.id = id++ + first;     
         tiles.putIfAbsent(ids, "");      
        }}
       int ids=key.id; 
       if (!nounits)ids += unitid; 
       unitlayer.putInt(index, ids);          
       unitnode.removeChild(next);
       break remove;         
      }
      remove(nextattr, "name") ;     
      remove(nextattr, "width");
      remove(nextattr, "height");
      remove(nextattr, "rotation");    
      remove(nextattr, "gid");                 
     }               
    }
    if (unittiles.getChildNodes().getLength() > 1)map.insertBefore(unittiles, pr); 
    if (getFirst(0, unitnode) == null)map.removeChild(unitnode);    
   }  
   int i=layer.size();
   while (--i > 0) {
    ByteBuffer warp= (ByteBuffer)layer.get(i);   
    Node data=(Node)layer.get(--i);    
    if (!data.getAttributes().getNamedItem("name").getNodeValue().equalsIgnoreCase("units")) {  
     int j=warp.limit();
     while ((j -= 4) >= 0) {
      int rt=warp.getInt(j);
      Object to= tiles.get(rt & 536870911);    
      if (to instanceof Integer) {
       warp.putInt(j, (rt & -536870912) | (Integer)to);
      }  
     }}
    data = getFirst(0, data);
    ByteBuffer result=deflate(warp);
    data.getAttributes().getNamedItem("compression").setNodeValue("zlib");
    data.setTextContent(new String(Base64.getEncoder().encode(result).array()));
   }
   for (i = nodeList.getLength(); --i >= 0;) {
	Node item = nodeList.item(i);
    if ("tileset".equals(item.getNodeName())) {
     NamedNodeMap attr=item.getAttributes();
     int firstgId = Ipare(attr, "firstgid");
     NodeList tilesetList = item.getChildNodes();
     for (int i2=tilesetList.getLength();--i2 >= 0;) {
      Node child = tilesetList.item(i2);
      if ("tile".equals(child.getNodeName())) {
       NamedNodeMap childAttr = child.getAttributes();
       Node idn=  childAttr.getNamedItem("id"); 
       int id = Integer.parseInt(idn.getNodeValue());
       Object key= tiles.get(firstgId + id);
       if (key == null) item.removeChild(child);
       else if (key instanceof Integer) {
        idn.setNodeValue(String.valueOf((Integer)key - firstgId));
       }               
      }
     }
    }
   }
   BufferedWriter buff=new BufferedWriter(new FileWriter(ou));
   try {
	outxml(document, buff);
   } finally {
	buff.close();
   }
  } catch (Throwable e) {
   log.e(this, e);
   ex = e;
  }
  if (ex != null)ou.delete();
  ui.end(ex);
 }
 public static void outxml(Node map, BufferedWriter out) throws Exception {
  NodeList list=map.getChildNodes();
  for (int i=0,l=list.getLength();i < l;++i) {
   Node item=list.item(i);
   if (item.getNodeType() == Node.TEXT_NODE) {
	out.write(item.getNodeValue().replaceAll("\\s", ""));
	continue;
   }
   out.write('<');
   String name=item.getNodeName();
   out.write(name);
   NamedNodeMap maps=item.getAttributes();
   if (maps != null) {
	int j=maps.getLength();
	boolean st=true;
    HashSet all=remove.get(name);     
	for (;--j >= 0;) {
	 Node kv=maps.item(j);
     name = kv.getNodeName();
     if (all != null && all.contains(name))continue;    
     if (st) {
      out.write(' ');   
      st = false;
     }             
     out.write(name);
     out.write("=\"");
     out.write(kv.getNodeValue());
	 out.write('\"');
	}
   }
   NodeList ch=item.getChildNodes();
   if (ch.getLength() == 0)out.write('/');
   out.write('>');
   if (ch.getLength() > 0) {
	outxml(item, out);
	out.write("</");
	out.write(item.getNodeName());
	out.write('>');
   }
  }
 }
}
