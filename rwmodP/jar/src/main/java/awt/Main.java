package awt;

import awt.StringUi;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;
import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import rust.UiHandler;
import rust.loader;
import rust.loaderManager;
import rust.pngOpt;
import rust.rwlib;
import rust.rwmapOpt;
import rust.rwmodProtect;
import rust.savedump;
import rust.UiHandler;
import rust.zipunpack;
import rust.zippack;

public class Main {
 public static DefaultListModel list;
 public static JFileChooser files() {
  JFileChooser file= new JFileChooser();
  file.setFileSelectionMode(JFileChooser.FILES_ONLY);
  file.setMultiSelectionEnabled(true);
  return file;
 }
 public static void showUi() {
  /*
   这是用于桌面平台，不过由于依赖库没有提供对应的二进制编译，暂时不处理。
   */
  JFrame hws = new JFrame("rwTool");
  hws.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  hws.setLayout(new BoxLayout(hws.getContentPane(), BoxLayout.Y_AXIS));
  JPanel pan=new JPanel(); 
  pan.setLayout(new FlowLayout(FlowLayout.LEFT));
  final JCheckBox lib=new JCheckBox("lib");
  lib.setSelected(uselib);
  pan.add(lib);
  lib.addActionListener(new ActionListener(){
    public void actionPerformed(ActionEvent actionEvent) {
     if (!uselib) {
      lib();
      uselib = true;
     }
     saveCof(lib.isSelected());
    }
   });
  final JCheckBox raw=new JCheckBox("raw");
  pan.add(raw);
  hws.add(pan);
  pan = new JPanel(); 
  JButton getfile=new JButton("start");
  hws.setVisible(true);
  hws.setSize(400, 400);
  pan.add(getfile);
  final ButtonGroup rb=new ButtonGroup();
  JRadioButton jb,jb2,jb3;
  rb.add(jb = new JRadioButton("protect"));
  jb.setSelected(true);
  rb.add(jb2 = new JRadioButton("pack"));
  rb.add(jb3 = new JRadioButton("unpack"));
  pan.add(jb);
  pan.add(jb2);
  pan.add(jb3);
  hws.add(pan);
  JPanel p=new JPanel();
  p.setLayout(new FlowLayout(FlowLayout.LEFT));
  DefaultListModel mode=new DefaultListModel();
  list = mode;
  p.add(new JScrollPane(new JList(mode)));
  hws.add(p);
  hws.pack();
  getfile.addActionListener(new ActionListener(){
    public void actionPerformed(ActionEvent actionEvent) {
     JFileChooser c=files();
     if (c.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
      File[] list=c.getSelectedFiles();
      for (File f:list) {
       if (f.exists()) {
        StringUi StringUi=new StringUi(f.getPath());
        int id=0;
        Enumeration<AbstractButton> ems=rb.getElements();
        while (ems.hasMoreElements()) {
         if (ems.nextElement().isSelected())break;
         id++;
        }
        if (UiHandler.DefaultRunTask(f,id, 0, 1, raw.isSelected(), new File(userdir),StringUi))
         Main.list.addElement(StringUi);
       }
      }
     }
    }
   });
 }
 public static boolean uselib;
 public static String userdir;
 public static void saveCof(boolean uselib) {
  File cof = new File(userdir, ".obj");
  cof.mkdirs();
  try {
   DataOutputStream obj=new DataOutputStream(new BufferedOutputStream(new FileOutputStream(cof)));
   try {
    obj.writeBoolean(uselib);
   } finally {
    obj.close();
   }
  } catch (Exception e) {
   error(UiHandler.toList(e));
  }
 }
 public static void readCof() throws IOException {
  File cof = new File(userdir, ".obj");
  if (cof.exists()) {
   DataInputStream obj=new DataInputStream(new BufferedInputStream(new FileInputStream(cof)));
   try {
    uselib = obj.readBoolean();
   } finally {
    obj.close();
   }
  }
 }
 public static void lib() {
  File lib = new File(userdir, "lib.zip");
  if (lib.exists()) {
   StringUi ui=new StringUi("lib");
   rwlib rwlib= new rwlib(lib, null, null, ui);
   list.addElement(ui);
   rwlib.init();
  }
 }
 public static void error(List<Throwable> err) {
  if (list != null && list.size() > 0) {
   //to do
   for (Throwable e:err) {
    e.printStackTrace();
   }
  }
 }
 public static void main(String arg[]) {
  String dir= System.getProperty("user.dir");
  userdir = dir;
  File cof=new File(dir, ".ini");
  try {
   rwmodProtect.init(loader.load(new BufferedReader(new FileReader(cof), Math.min(8192, (int)cof.length()))));
   readCof();
   if (uselib)lib();
   showUi();
  } catch (Exception e) {
   error(UiHandler.toList(e));
  }
  Scanner in=new Scanner(System.in);
  while (true) {
   if (in.nextLine().equals("exit"))
    System.exit(0);
  }
 }
}
