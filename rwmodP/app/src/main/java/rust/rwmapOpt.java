package rust;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import me.steinborn.libdeflate.Libdeflate;
import me.steinborn.libdeflate.LibdeflateCompressor;
import me.steinborn.libdeflate.LibdeflateDecompressor;
import me.steinborn.libdeflate.LibdeflateJavaUtils;
import org.libDeflate.BufWriter;
import org.libDeflate.ByteBufIo;
import org.libDeflate.RC;
import org.libDeflate.UIPost;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.*;
import java.nio.*;


public class rwmapOpt implements Runnable {
 public static class key implements Comparable {
  public int hash;  
  public String str;
  public int team;
  public int id;
  public key(String s, int t) {
   int h=17;   
   h = h * 31 + s.hashCode();
   h = h * 31 + t;
   hash = h;   
   team = t;
   str = s; 
  }
  public int hashCode() {
   return hash;
  }
  public int compareTo(Object o) {
   key cp=(key)o;
   int u=team - cp.team;
   if (u != 0)return u;
   return str.compareTo(cp.str); 
  }
  public boolean equals(Object obj) {
   return compareTo(obj) == 0;
  }
 }
 public static class base64png {
  public NodeList node;
  public Node img;
  public int start;
  public int len;
  public base64png(NodeList list, Node png, int frist, int eop) {
   node = list;
   img = png;
   start = frist;
   len = eop;
  }
 }
 UIPost ui;
 File in;
 File ou;
 static HashMap<String,HashSet> remove;
 static HashMap<key,key> units;
 static HashMap<String,String> fovers;
 static HashSet<String> oldunits;
 public static String makeOld(String str) {
  char[] cr=str.toCharArray();
  char c=cr[0];
  cr[0] = Character.isUpperCase(c) ?Character.toLowerCase(c): Character.toUpperCase(c);
  return new String(cr);
 }
 public rwmapOpt(File i, File u, UIPost uo) {
  in = i;
  ou = u;
  ui = uo;
 }
 public static void init(HashMap<String,section> src) {
  HashMap re=src.get("tmx").m;
  for (Map.Entry<String,Object> en:(Set<Map.Entry>)re.entrySet())
   en.setValue(rwmodProtect.toSet((String)en.getValue()));
  rwmapOpt.remove = re;
  HashMap<String,String> set= src.get("unit").m;
  String list[]=set.get("replace").split(",");
  int i=0,len=list.length;
  int size=(len << 1 / 3) + 1;
  HashSet oldunits=new HashSet(size);
  HashMap fovers=new HashMap(size);
  do {
   String k=list[i++];
   String v=list[i++];
   fovers.put(v, k);
   oldunits.add(k);
  } while (i < len);
  rwmapOpt.oldunits = oldunits;
  rwmapOpt.fovers = fovers;
  list = set.get("unit").split(",");
  len = list.length;
  HashMap units = new HashMap(len << 2 / 9 + 1);
  i = 0;
  do {
   String id=list[i++];
   String v=list[i++];
   String team=list[i++];
   rwmapOpt.key key=new rwmapOpt.key(v, Integer.parseInt(team));
   key.id = Integer.parseInt(id);
   units.put(key, key);
  }while (i < len);
  rwmapOpt.units = units;
 }
 public static int getPngSize(List<rwmapOpt.base64png> list, HashSet tree, HashMap<Integer,Integer> tiles) {
  int listSize=list.size();
  int all=0;
  for (int len=0;len < listSize;++len) {
   int size=0;
   rwmapOpt.base64png png=list.get(len);
   for (int j=png.start,e=j + png.len;j < e;++j) {
    Integer i=j;
    if (!tree.contains(i) && tiles.containsKey(i))
     ++size;
   }
   if (size <= 0)
    list.set(len, null);
   //remove代价比较大
   all += size;
  }
  return all;
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
 public static key getKey(Node node, boolean fromClass) {
  node = getFirst(0, node);
  if (node == null)return null;
  String unit=null;
  int team=-1;
  String type=null;  
  NodeList list=node.getChildNodes();
  for (int i=0,len=list.getLength();i < len;i++) {
   Node item = list.item(i);
   if (item.getNodeType() != Node.ELEMENT_NODE)continue;
   NamedNodeMap attr=item.getAttributes();
   String name=attr.getNamedItem("name").getNodeValue();
   Node valuenode=attr.getNamedItem("value");
   String value=valuenode.getNodeValue();
   if (name.equals("unit")) {
    unit = value;
    if (fromClass && oldunits.contains(unit)) {
     unit = makeOld(unit);
    } else {
     String replce=fovers.get(unit);
     if (replce != null) {
      //减短名称
      valuenode.setNodeValue(replce);
      unit = replce;
     }
    }
   } else if (name.equals("team")) {
    if (value.equals("none")) {
     valuenode.setNodeValue("-1");
    } else team = Integer.parseInt(value);
   } else if (name.equals("type")) {
    type = value;
   }
  }
  if (unit == null)return null;
  if (type != null)unit += "_" + type;
  return new key(unit, team);
 }
 public static key getUnits(Node node, boolean fromClass) {
  key key=getKey(node, fromClass);
  if (key == null)return null;
  key ru=units.get(key);
  if (ru != null)return ru;
  key.id = -1;
  return key;
 }
 public static ByteBuffer getBuf(LibdeflateDecompressor def, Node item) throws Exception {
  NamedNodeMap attr=item.getAttributes();
  int w= Ipare(attr, "width");
  int h= Ipare(attr, "height");
  int size=w * h << 2;
  ByteBuffer buffer=RC.newbuf(size);
  Node data = getFirst(0, item);
  String dataValue = data.getTextContent().trim();
  ByteBuffer basebuf=ByteBuffer.wrap(ImageUtil.base64decode(dataValue));
  def.mode = data.getAttributes().getNamedItem("compression").getNodeValue().equals("gzip") ?Libdeflate.GZIP: Libdeflate.ZLIB;
  int len= def.decompress(basebuf, buffer);
  if (len < 0)throw new IOException();
  buffer.flip();
  return buffer;
 }
 public static Node tile(Document document, int index) {
  Node unittiles=document.createElement("tileset");  
  NamedNodeMap unitat=unittiles.getAttributes();    
  addAttr(document, unitat, "firstgid", String.valueOf(index));
  return unittiles;
 }
 public static ByteBuffer deflate(LibdeflateCompressor def, ByteBuffer buf) {
  ByteBuffer ebuf=ByteBuffer.allocate(LibdeflateJavaUtils.getBufSize(buf.limit(), 1));
  def.compress(buf, ebuf);
  ebuf.flip();
  return ebuf;
 }
 //jdk的base64的标准库不支持直接堆，要优化的话需要重写
 //tmx的不会太大不同于Zip不属于性能瓶颈，不予优化
 public void run() {
  Throwable ex=null;
  try {
   DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
   /*防止XXE没有任何意义
	docBuilder.setEntityResolver(new EntityResolver(){
	public InputSource resolveEntity(String publicId, String systemId) throws IOException{
	throw new IOException();
	}
	});*/
   Document document = docBuilder.parse(in);
   HashMap tiles = new HashMap();
   HashSet treeTile=new HashSet();
   HashMap utiles=new HashMap();
   Node map= document.getFirstChild();
   Node pr= getFirst(0, map);
   int max=0;
   int unitid=0;
   int miscid=0;
   ArrayList layer=new ArrayList();
   HashMap<Short,List<base64png>> pngs=new HashMap();
   Node unitnode=null;
   NodeList nodeList =map.getChildNodes();
   LibdeflateDecompressor def=new LibdeflateDecompressor(0);
   try {
    if ("properties".equals(pr.getNodeName()))
     map.removeChild(pr);
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
      NodeList objlist=item.getChildNodes();
      if ((name = item.getAttributes().getNamedItem("name").getNodeValue()).equalsIgnoreCase("unitobjects") || name.equalsIgnoreCase("unitsobject"))
       unitnode = item;       
      else {
       HashSet pot=remove.get("point");
       HashSet no=remove.get("obj");            
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
      ByteBuffer buffer=getBuf(def, item);
      layer.add(item);                      
      layer.add(buffer);
      buffer.order(ByteOrder.LITTLE_ENDIAN);
      while (buffer.hasRemaining()) {
       int n=buffer.getInt() & 536870911;
       tiles.putIfAbsent(n, "");
      }
      buffer.flip();
     } else if ("tileset".equals(type)) {
      NamedNodeMap attr=item.getAttributes();
      int first=Ipare(attr, "firstgid"); 
      Node src=item.getAttributes().getNamedItem("source");
      boolean check=true;
      if (src != null) {
       String v=src.getNodeValue();
       if ("units.tsx".equals(v)) {
        unitid = first;
        check = false;
       } else if ("misc.tsx".equals(v)) {
        miscid = first;
        check = false;
       }
      }
      int c= Ipare(attr, "tilecount");
      Node node= getFirst(i + 1, map);
      boolean hasnext = node.getNodeName().equals("tileset");
      if (c < 0) {
       if (hasnext)c = Ipare(node.getAttributes(), "firstgid");         
       else if (max > 0) c = max;
       else c = max = first + 270;
       //无法获取到底使用了多少地块
      } else c += first;
      tag:         
      if (check) {
       for (int j=c;--j >= first;) {
        if (tiles.containsKey(j)) {
         break tag;
        }
       }
       if (!hasnext)max = first;
       map.removeChild(item);
       continue;
      }
      if (max < c)max = c;
      NodeList tilesetList = item.getChildNodes();
      for (int i2=tilesetList.getLength();--i2 >= 0;) {
       Node child = tilesetList.item(i2);
       if ("tile".equals(child.getNodeName())) {
        NamedNodeMap childAttr = child.getAttributes();
        Node idn=  childAttr.getNamedItem("id"); 
        Integer id = Integer.parseInt(idn.getNodeValue()) + first;
        if (!tiles.containsKey(id)) 
         item.removeChild(child);
       }
      }
     }
    }
   } finally {
    def.close();
   }
   if (unitid == 0) {
    int first=++max;
    Node addunits= tile(document, first);
    map.insertBefore(addunits, pr);
    addAttr(document, addunits.getAttributes(), "source", "units.tsx");
    unitid = first;
    max += 270;
   }
   if (miscid == 0) {
    int first=++max;
    Node addunits= tile(document, first);
    map.insertBefore(addunits, pr);
    addAttr(document, addunits.getAttributes(), "source", "misc.tsx");
    miscid = first;
    max += 18;
   }
   tag:
   for (int i =nodeList.getLength();--i >= 0;) {
    Node item = nodeList.item(i);
    if ("tileset".equals(item.getNodeName())) {
     NamedNodeMap attr=item.getAttributes();
     int tileWidth = Ipare(attr, "tilewidth");
     if (tileWidth == 20) attr.removeNamedItem("tilewidth") ;           
     int tileHeight = Ipare(attr, "tileheight");
     if (tileHeight == 20) attr.removeNamedItem("tileheight") ;
     boolean unitGrop=false;
     NodeList list=item.getChildNodes(); 
     int first=Ipare(attr, "firstgid");
     for (int l=list.getLength();--l >= 0;) {
      Node next=list.item(l);
      String name=next.getNodeName();
      if ("tile".equals(name)) {
       key key=getUnits(next, false);
       if (key != null) {
        int id=Ipare(next.getAttributes(), "id") + first;   
        String str=key.str;
        if (str.startsWith("tree")) {
         char c=str.charAt(str.length() - 1);
         Integer ids=id;
         treeTile.add(ids);
         tiles.put(ids, miscid + (c == 'e' ?9:  c - 38));
         //'0'-10 =48-10
         item.removeChild(next);
         continue;
        }
        unitGrop = true;
        if (key.id >= 0) {
         tiles.put(id, unitid + key.id);
         item.removeChild(next);
         continue;
        }
        key.id = id;
        utiles.putIfAbsent(key, key);
       }
      } else if ("terraintypes".equals(name)) {
       item.removeChild(next);
      } else if ("properties".equals(name)) {
       NodeList plist = next.getChildNodes();
       for (int i2=plist.getLength();--i2 >= 0;) {             
        Node property = plist.item(i2);
        if (property.getNodeType() == Node.ELEMENT_NODE) {
         name = property.getAttributes().getNamedItem("name").getNodeValue();         
         if (name.equals("layer") || name.equals("forced_autotile")) {
          next.removeChild(property);
          continue;
         }
         if ("embedded_png".equals(name)) {
          int tilec=Ipare(attr, "tilecount");
          Short st=(short)((tileWidth << 8) | tileHeight);
          List<rwmapOpt.base64png> listpng=pngs.get(st);
          if (listpng == null) {
           listpng = new ArrayList();
           pngs.put(st, listpng);
          }
          listpng.add(new base64png(list, getFirst(0, next), first, tilec));
          map.removeChild(item);
          continue tag;
         }
        }
       }
      }
     }
     if (unitGrop && getFirst(0, item) == null)
      map.removeChild(item);
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
    Node unittiles=tile(document, ++max);  
    unittiles.appendChild(document.createElement("image"));         
    NodeList objunits=unitnode.getChildNodes();
    int idIndex=0;
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
       key find=getUnits(next, true);
       boolean nounits;
       if (nounits = find.id < 0) {
        rwmapOpt.key key = (key)utiles.putIfAbsent(find, find);
        if (key == null) {
         Node tile=document.createElement("tile"); 
         addAttr(document, tile.getAttributes(), "id", String.valueOf(idIndex++)); 
         Node posadd=document.createElement("properties");   
         Node por= document.createElement("property");
         NamedNodeMap poattr= por.getAttributes(); 
         addAttr(document, poattr, "name", "team");                 
         addAttr(document, poattr, "value", String.valueOf(find.team)); 
         posadd.appendChild(por);
         por = document.createElement("property");
         poattr = por.getAttributes();            
         addAttr(document, poattr, "name", "unit"); 
         String name=find.str;
         addAttr(document, poattr, "value", name);          
         posadd.appendChild(por);
         tile.appendChild(posadd);         
         unittiles.appendChild(tile);         
         find.id = max++;
        } else find = key;
       }
       int ids=find.id; 
       unitlayer.putInt(index, nounits ?ids: unitid + ids);          
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
   for (Map.Entry<Short,List<base64png>> en:pngs.entrySet()) {
    short s= en.getKey();
    int h=s & 0xff;
    int w=((s & 0xff00) >> 8);
    Node tile=tile(document, ++max);
    NamedNodeMap attr=tile.getAttributes();
    if (h != 20)addAttr(document, attr, "height", String.valueOf(h));
    if (w != 20)addAttr(document, attr, "width", String.valueOf(h));
    Node posadd=document.createElement("properties");   
    Node por= document.createElement("property");
    addAttr(document, por.getAttributes(), "name", "embedded_png");                 
    posadd.appendChild(por);
    tile.appendChild(posadd);
    List<rwmapOpt.base64png> list=en.getValue();
    int size=getPngSize(list, treeTile, tiles);
    byte irr[]=ImageUtil.tmxOpt(list, treeTile, tiles, w, h, max, size);
    por.setTextContent(ImageUtil.base64encode(irr));
    map.insertBefore(tile, pr);
    for (rwmapOpt.base64png png:list) {
     if (png == null)continue;
     NodeList nodelist=png.node;
     for (int j=nodelist.getLength();--j >= 0;) {
      Node node=nodelist.item(j);
      if ("tile".equals(node.getNodeName())) {
       NamedNodeMap nattr=node.getAttributes();
       int id=Ipare(nattr, "id");
       Integer fid=png.start + id;
       if (!treeTile.contains(fid) && (fid = (Integer) tiles.get(fid)) != null) {
        addAttr(document, nattr, "id", String.valueOf(fid - max));
        tile.appendChild(node);
       }
      }
     }
    }
    max += size;
   }
   LibdeflateCompressor defc=new LibdeflateCompressor(12, Libdeflate.ZLIB);
   try {
    int i=layer.size();
    while (--i > 0) {
     ByteBuffer warp= (ByteBuffer)layer.get(i);   
     Node data=(Node)layer.get(--i);    
     int j=warp.limit();
     while ((j -= 4) >= 0) {
      int rt=warp.getInt(j);
      Object to= tiles.get(rt & 536870911);    
      if (to instanceof Integer) {
       warp.putInt(j, (rt & -536870912) | (Integer)to);
      }
     }
     data = getFirst(0, data);
     ByteBuffer result=deflate(defc, warp);
     data.getAttributes().getNamedItem("compression").setNodeValue("zlib");
     data.setTextContent(ImageUtil.base64encode(result));
    }
   } finally {
    defc.close();
   }
   BufferedWriter buff=new BufferedWriter(new BufWriter(new ByteBufIo(FileChannel.open(ou.toPath(), StandardOpenOption.CREATE, StandardOpenOption.WRITE), RC.IOSIZE), StandardCharsets.UTF_8));
   try {
    outxml(document, buff);
   } finally {
    buff.close();
   }
  } catch (Exception e) {
   ex = e;
  }
  if (ex != null)ou.delete();
  ui.accept(UiHandler.toList(ex));
 }
 public static void outxml(Node map, BufferedWriter out) throws Exception {
  NodeList list=map.getChildNodes();
  for (int i=0,l=list.getLength();i < l;++i) {
   Node item=list.item(i);
   if (item.getNodeType() == Node.TEXT_NODE) {
	CharBuffer buf=iniobj.trims(item.getNodeValue());
	out.write(buf.array(), 0, buf.limit());
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
