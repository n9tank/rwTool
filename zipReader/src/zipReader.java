
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import zipReader.zipNode;
public class zipReader implements AutoCloseable {
 public ZipFile file;
 public zipReader.zipNode root;
 public zipReader(String file) throws IOException {
  ZipFile zf= new ZipFile(file);
  try {
   Enumeration<? extends ZipEntry> ens=zf.entries();
   while (ens.hasMoreElements()) {
    ens.nextElement().getName();
   }
  } catch (Exception e) {
   zf.close();
   zf = new ZipFile(file, StandardCharsets.ISO_8859_1);
  }
  ZipEntry root=zf.getEntry("");
  this.root = new zipReader.zipNode(root == null ?"": root);
  this.file = zf;
  Enumeration<? extends ZipEntry> ens=zf.entries();
  while (ens.hasMoreElements()) {
   add(ens.nextElement());
  }
 }
 public InputStream openStream(zipNode node) throws IOException {
  return file.getInputStream(node.getEntry());
 }
 public void close() throws IOException {
  file.close();
 }
 public zipNode getEntry(zipNode root, String str) {
  zipReader.zipNode node=find(root, str, false, true);
  if (node != null)return node;
  return find(root, str, true, true);
 }
 public zipNode getEntryRoot(zipNode root, String str) {
  zipReader.zipNode node=findRoot(root, str, false, true);
  if (node != null)return node;
  return findRoot(root, str, true, true);
 }
 public zipNode getParent(zipNode node) {
  String str=node.getName();
  int i= str.lastIndexOf('/', str.length() - 2);
  if (i >= 0)
   return find(root, str.substring(0, i + 1), false, false);
  return root;
 }
 public zipNode add(ZipEntry ze) {
  zipNode last=root;
  String str=ze.getName();
  int lastIn=0;
  int len=str.length();
  while (lastIn < len) {
   int i = str.indexOf('/', lastIn) + 1;
   String zname=null;
   Object zfe=i == 0 ?ze: file.getEntry(zname = str.substring(0, i));
   if (zfe == null)zfe = zname;
   last = last.addFile(zfe, str.substring(lastIn, i == 0 ?len: i));
   if (i == 0)break;
   lastIn = i;
  }
  return last;
 }
 public zipNode findRoot(zipNode last, String str, boolean igronCase, boolean tryFolder) {
  String prefix=last.getName();
  int len=prefix.length();
  if (str.regionMatches(igronCase, 0, prefix, 0, len))
   str = str.substring(len);
  else last = root;
  return find(last, str, igronCase, tryFolder);
 }
 public zipNode find(zipNode last, String str, boolean igronCase, boolean tryFolder) {
  int lastIn=0;
  int len=str.length();
  while (lastIn < len) {
   int i = str.indexOf('/', lastIn) + 1;
   String path=str.substring(lastIn, i == 0 ?len: i);
   last = last.find(path, igronCase, i == 0);
   if (last == null)return null;
   if (i == 0)break;
   lastIn = i ;
  }
  return last;
 }
 public static class zipNode {
  public Object name;
  public HashMap<String,zipNode> list;
  public HashMap<String,zipNode> caselist;
  public zipNode(Object name) {
   this.name = name;
  }
  public String getName() {
   Object name=this.name;
   if (name instanceof ZipEntry)
    return ((ZipEntry)name).getName();
   return (String)name;
  }
  public final boolean isEntry() {
   return name instanceof ZipEntry;
  }
  public ZipEntry getEntry() {
   Object name=this.name;
   if (name instanceof ZipEntry)
    return (ZipEntry)name;
   return null;
  }
  public zipNode addFile(Object en, String str) {
   HashMap<String,zipNode> list=this.list;
   if (list == null)
    this.list = list = new HashMap();
   zipReader.zipNode node=new zipNode(en);
   zipNode nx=list.putIfAbsent(str, node);
   String icase=str.toLowerCase();
   if (icase != str) {
    HashMap<String,zipNode> caselist=this.caselist;
    if (caselist == null)
     this.caselist = caselist = new HashMap();
    caselist.putIfAbsent(icase, node);
   }
   if (nx != null)return nx;
   return node;
  }
  public zipNode find(String str, boolean igroncase, boolean tryFolder) {
   String icase=igroncase ?str: str.toLowerCase();
   HashMap<String,zipNode> list=igroncase && icase != str ?this.caselist: this.list;
   if (list == null)return null;
   zipReader.zipNode node= list.get(icase);
   if (node != null || !tryFolder)return node;
   return list.get(icase.concat("/"));
  }
  public final boolean hasNodes() {
   return list != null;
  }
  public Map.Entry<String,zipNode>[] sort(Comparator cp) {
   HashMap<String,zipNode> list=this.list;
   if (list == null)return null;
   int cou=0;
   Map.Entry[] nodes=new Map.Entry[list.size()];
   for (Map.Entry node:list.entrySet())
    nodes[cou++] = node;
   Arrays.sort(nodes, cp);
   return nodes;
  }
  public void loopMatcher(Pattern pt, ArrayList list, int off) {
   ZipEntry ze=getEntry();
   if (ze != null && pt.matcher(ze.getName().substring(off)).find())
    list.add(this);
   HashMap<String,zipNode> map=this.list;
   if (map != null) {
    for (zipNode node:map.values())
     node.loopMatcher(pt, list, off);
   }
  }
  public ArrayList<zipNode> nodeMatcher(Pattern pt) {
   ArrayList<zipReader.zipNode> list=new ArrayList<zipNode>();
   loopMatcher(pt, list, getName().length());
   return list;
  }
  public ArrayList<zipNode> nodeMatcherSafe(Pattern pt) {
   ArrayList<zipReader.zipNode> list=new ArrayList<zipNode>();
   ArrayDeque<Iterator<zipNode>> que=new ArrayDeque();
   zipNode node=this;
   HashMap<String,zipNode> map=node.list;
   int off=getName().length();
   if (map != null) {
    que.add(map.values().iterator());
    Iterator<zipNode> ite;
    while ((ite = que.poll()) != null) {
     while (ite.hasNext()) {
      node = ite.next();
      ZipEntry ze=getEntry();
      if (ze != null && pt.matcher(ze.getName().substring(off)).find())
       list.add(node);
      map = node.list;
      if (map != null) {
       que.add(ite);
       ite = map.values().iterator();
      }
     }
    }
   }
   return list;
  }
  public zipNode autogoto() {
   zipReader.zipNode node=this;
   while (true) {
    HashMap<String,zipNode> list=node.list;
    if (list == null || list.size() > 1)return node;
    node = list.values().iterator().next();
   }
  }
 }
 public static void unzip(zipNode node, zipReader read, Path ou) {
  try {
   InputStream io=read.openStream(node);
   try {
    Files.copy(io, ou, new CopyOption[]{StandardCopyOption.REPLACE_EXISTING});
   } finally {
    io.close();
   }
  } catch (Exception e) {
   e.printStackTrace();
  }
 }
 public static void cat(zipNode node, zipReader read, Path ou) {
  if (ou != null) {
   unzip(node, read, ou);
   return;
  }
  noline = false;
  try {
   InputStream input=read.openStream(node);
   try {
    byte buf[]=new byte[8192];
    int i;
    while ((i = input.read(buf)) >= 0) {
     sysprint.write(buf, 0, i);
    }
    System.out.println();
   } finally {
    input.close();
   }
  } catch (Exception e) {
   e.printStackTrace();
  }
  noline = true;
 }
 public static boolean noline=true;
 public static final PrintStream sysprint=new PrintStream(new FilterOutputStream(System.out) {
   public void write(int b) throws IOException {
    super.write(b == 27 || (b == '\n' && noline) ?0: b);
   }
   public void write(byte[] b, int off, int len) throws IOException {
    for (int i = off; i < len; ++i) {
     byte bi=b[i];
     if (bi == 27 || (bi == '\n' && noline))
      b[i] = 0;
    }
    super.write(b, off, len);
   }
  }, false);
 public static void main(String[] args) throws IOException {
  Scanner sc=new Scanner(System.in);
  zipReader read=null;
  PrintStream out=System.out;
  while (true) {
   try {
    read = new zipReader(sc.nextLine());
    break;
   } catch (Exception e) {
    e.printStackTrace();
   }
  }
  zipNode root=read.root;
  Map.Entry<String,zipNode>[] nodes=null;
  zipNode lastroot=null;
  String user=System.getProperty("user.dir");
  while (true) {
   if (lastroot != root) {
    out.print("\033[H\033[2J");
    if (nodes == null || lastroot != null)
     nodes = root.sort(Map.Entry.comparingByKey());
    lastroot = root;
    sysprint.print(root.getName());
    out.println();
    if (nodes != null) {
     for (int i=0,len=nodes.length;i < len;++i) {
      out.print(i);
      out.print(' ');
      Map.Entry<String,zipNode> node=nodes[i];
      sysprint.print(node.getKey());
      out.println();
     }
    }
   }
   String cmd=sc.next();
   switch (cmd) {
    case "cd":
     {
      String str=sc.nextLine().substring(1).replace("\\", "/");
      String name=root != read.root ?(root.getName().concat(str)): str;
      if (name.contains("..")) {
       String[] list=name.split("/");
       String[] join=new String[list.length];
       int cou=0;
       int c=join.length;
       for (int len=list.length;--len >= 0;) {
        String tok=list[len];
        if ("..".equals(tok)) {
         ++cou;
        } else if (cou > 0)--cou;
        else
         join[--c] = tok;
       }
       name = String.join("/", Arrays.copyOfRange(join, c, join.length));
      }
      zipReader.zipNode node= read.getEntryRoot(root, name);
      if (node != null)root = node;
     }
     break;
    case ".":
     lastroot = null;
     break;
    case "~":
     root = read.root;
     break;
    case "cat":
     {
      String id=sc.next();
      zipNode node=null;
      if (".".equals(id))
       node = root;
      else {
       try {
        node = nodes[Integer.parseInt(id)].getValue();
       } catch (Exception e) {
        out.println("Unknown command");
       }
      }
      if (node != null) {
       String str= sc.nextLine();
       cat(node, read, str.length() > 0 ?Paths.get(user, str): null);
      }
     }
     break;
    case "..":
     root = read.getParent(root);
     break;
    case "goto":
     root = root.autogoto();
     break;
    case "find":
     ArrayList<zipNode> list=root.nodeMatcherSafe(Pattern.compile(sc.next(), Pattern.CASE_INSENSITIVE));
     for (int i=0,len=list.size();i < len;++i) {
      out.print(i);
      out.print(' ');
      sysprint.print(list.get(i).getName());
      out.println();
     }
     loop:
     while (true) {
      String str=sc.next();
      try {
       switch (str) {
        case "x":
         break loop;
        case "cat":
         int i=Integer.parseInt(sc.next());
         String path= sc.nextLine();      
         cat(list.get(i), read, path.length() > 0 ?Paths.get(user, path): null);
         break;
        default:
         root = list.get(Integer.parseInt(str));
         break loop;
       }
      } catch (Exception e) {
       out.println("Unknown command");
      }
     }
     break;
    case "x":
     read.close();
     System.exit(0);
     break;
    default:
     try {
      root = nodes[Integer.parseInt(cmd)].getValue();
     } catch (Exception e) {
      out.println("Unknown command");
     }
     break;
   }
  }
 }
}
