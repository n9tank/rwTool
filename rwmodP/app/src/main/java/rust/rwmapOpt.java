package rust;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import com.nicdahlquist.pngquant.LibPngQuant;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;
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
import org.libDeflate.UIPost;
import java.nio.file.Files;
import java.io.IOException;
import java.nio.file.Path;

public class rwmapOpt implements Runnable {
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
 UIPost ui;
 File in;
 File ou;
 static HashMap<String,HashSet> remove;
 static HashMap<key,key> units;
 static HashMap<String,String> oldunits;
 public rwmapOpt(File i, File u, UIPost uo) {
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
 public static ByteBuffer deflate(ByteBuffer buf) {
  LibdeflateCompressor def=new LibdeflateCompressor(12, 1);
  ByteBuffer ebuf=ByteBuffer.allocate(LibdeflateJavaUtils.getBufSize(buf.limit(), 1));
  def.compress(buf, ebuf);
  def.close();
  ebuf.flip();
  return ebuf;
 }
 public static File openTmp() throws IOException {
  Path tmp=Files.createTempFile("", "");
  Files.deleteIfExists(tmp);
  return tmp.toFile();
 }
 public static byte[] optpng(Bitmap bm2) throws Exception {
  File tmp=openTmp();
  //这是pngQuant-Android的屎优化不动
  FileOutputStream out=new FileOutputStream(tmp);
  bm2.compress(Bitmap.CompressFormat.PNG, 100, out);
  bm2.recycle();
  out.close();
  File optmp=openTmp();
  //optmp.createNewFile();
  LibPngQuant.pngQuantFile(tmp, optmp, 65, 80, 1, 0.5f);
  tmp.delete();
  FileInputStream in = new FileInputStream(optmp);
  byte[] brr=new byte[in.available()];
  in.read(brr);
  in.close();
  optmp.delete();
  return brr;
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
   ArrayList layer=new ArrayList();
   Node unitnode=null;
   byte[] buf=new byte[8192];
   if ("properties".equals(pr.getNodeName()))map.removeChild(pr);
   NodeList nodeList =map.getChildNodes();
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
          bmp.recycle();
          property.setTextContent(Base64.getEncoder().encodeToString(optpng(bm2)));
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
   ex = e;
  }
  if (ex != null)ou.delete();
  ui.accept(zipunpack.toList(ex));
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
