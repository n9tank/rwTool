package rust.rwTool;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.libDeflate.NioReader;
import org.libDeflate.ParallelDeflate;
import org.libDeflate.RC;
import rust.UiHandler;
import rust.loader;
import rust.loaderManager;
import rust.rwlib;
import rust.rwmodProtect;
public class Main extends Activity {
 boolean uselib;
 RadioGroup bu;
 CheckBox raw;
 static Activity show;
 static ArrayAdapter arr;
 public void finish() {
  moveTaskToBack(true);
 }
 public static void save(String str) throws IOException {
  File file=Files.createTempFile(Main.show.getExternalCacheDir().toPath(), "", ".log").toFile();
  FileWriter io=new FileWriter(file);
  io.write(str);
  io.close();
 }
 public static void error(List<Throwable> e, String where) {
  Activity ct=Main.show;
  AlertDialog.Builder show=new AlertDialog.Builder(ct);
  show.setTitle(where);
  CharArrayWriter cr=new CharArrayWriter();
  PrintWriter out=new PrintWriter(cr);
  for (Throwable err:e)
   err.printStackTrace(out);
  final String str=cr.toString();
  ScrollView scr=new ScrollView(ct); 
  TextView text=new TextView(ct);
  scr.addView(text);
  text.setText(str);
  text.setTextIsSelectable(true);
  show.setView(scr);
  show.setCancelable(false);
  show.setNegativeButton("save", new Dialog.OnClickListener(){
    public void onClick(DialogInterface dialog, int which) {
     StringUi ui=new StringUi("log");
     arr.add(ui);
     ParallelDeflate.pool.execute(new log(str, ui));
    }
   });
  show.setPositiveButton("ok", null);
  show.show();
 }
 protected void onCreate(Bundle savedInstanceState) {
  super.onCreate(savedInstanceState);
  show = this;
  SharedPreferences sh=getSharedPreferences("", MODE_PRIVATE);
  setContentView(R.layout.activity_main);
  bu = findViewById(R.id.rw);
  raw = findViewById(R.id.raw);
  ListView list=findViewById(R.id.list);
  ArrayAdapter ar=new ArrayAdapter(this, android.R.layout.test_list_item, new ArrayList());
  list.setAdapter(ar);
  arr = ar;
  boolean def=sh.getBoolean("", false);
  if (def) {
   CheckBox checkbox=findViewById(R.id.ch);
   checkbox.setChecked(def);
   uselib = def;
  }
  Intent i=getIntent();
  if (i != null)st(i);
  String s;
  if (checkSelfPermission(s = "android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) {
   String per[]=new String[]{s};
   requestPermissions(per, 0);
  } else init();
 }  
 public void lib() {
  File li=new File(getExternalFilesDir(null), "lib.zip");
  StringUi ui=new StringUi("lib");
  loaderManager run;  
  if (!li.exists())run = new rwlib(null, getResources().openRawResource(R.raw.lib), li, ui);
  else run = new rwlib(li, null, null, ui);
  run.init();
  arr.add(ui);
 }
 //实际上可以在权限前加载，不过可以兼容配置文件在外部的情况
 public void init() {
  if (uselib)lib();
  try {
   File su=getExternalFilesDir(null);
   HashMap io;
   File ini = new File(su, ".ini");
   if (ini.exists())
    io = loader.load(new BufferedReader(new NioReader(FileChannel.open(ini.toPath(), StandardOpenOption.READ),RC.IOSIZE, StandardCharsets.UTF_8)));
   else 
    io = loader.load(getResources().openRawResource(R.raw.def));
   rwmodProtect.init(io);
  } catch (Throwable e) {
   error(UiHandler.toList(e), "init");
  }
 }
 public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
  super.onRequestPermissionsResult(requestCode, permissions, grantResults);
  if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
   init();
  } else if (shouldShowRequestPermissionRationale("android.permission.WRITE_EXTERNAL_STORAGE")) {
   requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 0);
  } else toset();
 }
 public void toset() {
  Intent intent=new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
  intent.setData(Uri.parse("package:rust.rwTool"));
  startActivityForResult(intent, 0);
 }
 protected void onNewIntent(Intent intent) {
  super.onNewIntent(intent);
  st(intent);
 }
 public void st(Intent intent) {
  String type=intent.getAction();
  Uri o;
  if (type.equals(Intent.ACTION_SEND))o = intent.getParcelableExtra(Intent.EXTRA_STREAM);
  else if (type.equals(intent.ACTION_SEND_MULTIPLE)) {
   ArrayList<Uri> arr = intent.getParcelableArrayListExtra(intent.EXTRA_STREAM);
   for (int i=0,l=arr.size();i < l;++i)
    add(arr.get(i));
   return;
  } else o = intent.getData();
  if (o != null)add(o);
 }
 public void add(Uri uri) {
  String type=uri.getScheme();
  String path=uri.getPath();
  if (type.charAt(0) == 'c') {
   String ab=uri.getAuthority();
   if (ab.startsWith("com.android.externalstorage")) {
    path = "sdcard/".concat(path.substring(18));
   } else if (ab.startsWith("com.android.providers")) {
    int i=path.indexOf(':');
    String ids=path.substring(i + 1);
    //raw: msf:
    if (path.charAt(i - 1) != 'w') {
     ContentResolver contentResolver = getContentResolver();
     Cursor cursor = contentResolver.query(MediaStore.Files.getContentUri("external"), new String[]{"_data"}, "_id=?", new String[]{ids}, null);
     if (cursor != null) {
      cursor.moveToFirst();
      try {
       int idx = cursor.getColumnIndex("_data");
       path = cursor.getString(idx);
      } catch (Throwable e) {
      } finally {
       cursor.close();
      }
     }
    } else path = ids;
   }
  }
  File f=new File(path);
  if (f.exists()) {
   StringUi StringUi=new StringUi(path);
   if (UiHandler.DefaultRunTask(f, this.bu.getCheckedRadioButtonId(), R.id.pr, R.id.pack, raw.isChecked(), getExternalFilesDir(null), StringUi))
    arr.add(StringUi);
  }
 }
 public void onActivityResult(int requestCode, int resultCode, Intent data) {
  super.onActivityResult(requestCode, resultCode, data);
  if (requestCode == 0) {
   if (checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") == PackageManager.PERMISSION_GRANTED) {
    init();
   } else toset();
  } else if (resultCode == RESULT_OK) {
   Uri uri=data.getData();
   if (uri != null) {
    add(uri);
   } else {
    ClipData datas=data.getClipData();
    for (int t=0,i=datas.getItemCount();t < i;++t)
     add(datas.getItemAt(t).getUri());
   }
  }
 }
 public void sw(View v) {
  Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
  intent.addCategory(Intent.CATEGORY_OPENABLE);
  intent.setType("*/*");
  intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
  intent.createChooser(intent, "");
  startActivityForResult(intent, 1);
 }
 public void ch(View v) {
  CheckBox ch=(CheckBox)v;
  boolean is=ch.isChecked();
  uselib = is;
  if (is && rwlib.libMap == null)lib();
 }
 public void onSaveInstanceState(Bundle outState) {
  super.onSaveInstanceState(outState);
  SharedPreferences sha=getSharedPreferences("", MODE_PRIVATE);
  if (sha.getBoolean("", false) != uselib) {
   SharedPreferences.Editor ed=sha.edit();
   ed.putBoolean("", uselib);
   ed.apply();
  }
 }
}
