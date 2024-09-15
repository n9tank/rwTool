package rust.rwTool;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import carsh.log;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import rust.lib;
import rust.png;
import rust.rwmap;
import rust.rwmodProtect;
import rust.savedump;
import rust.zippack;
import rust.zipunpack;
public class Main extends Activity {
 boolean uselib;
 RadioGroup bu;
 CheckBox raw;
 static ArrayAdapter arr;
 public static TextView bar;
 public void finish() {
  moveTaskToBack(true);
 }
 public static void error(Throwable e, String where, Context c) {
  AlertDialog.Builder show=new AlertDialog.Builder(c);
  show.setTitle(where);
  show.setMessage(e.toString());
  show.show();
 }
 protected void onCreate(Bundle savedInstanceState) {
  super.onCreate(savedInstanceState);
  Thread.setDefaultUncaughtExceptionHandler(new log(this));
  SharedPreferences sh=getSharedPreferences("", MODE_PRIVATE);
  setContentView(R.layout.activity_main);
  bar = findViewById(R.id.lib);
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
  int sdk=Build.VERSION.SDK_INT;
  String s;
  if (sdk >= 23 && checkSelfPermission(s = "android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) {
   String per[]=new String[]{s};
   requestPermissions(per, 0);
  } else init();
 }  
 public void lib() {
  bar.setVisibility(0);
  File li=new File(getExternalFilesDir(null), "lib.zip");
  cui ui=new cui("lib");
  Runnable run;  
  if (!li.exists())run = new lib(null, getResources().openRawResource(R.raw.lib), li, ui);
  else run = new lib(li, null, null, ui);
  ui.pool.execute(run);
 }
 public void init() {
  if (uselib)lib();
  try {
   File su=getExternalFilesDir(null);
   InputStream io;
   File ini = new File(su, ".ini");
   if (ini.exists())io = new FileInputStream(ini);
   else io = getResources().openRawResource(R.raw.def);
   rwmodProtect.init(io);
  } catch (Throwable e) {
   log.e(this, e);
   error(e, "init", this);
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
   int i=0,l=arr.size();
   while (i < l) {
    add(arr.get(i++));
   }
   return;
  } else o = intent.getData();
  if (o != null)add(o);
 }
 public static File out(File path, int i, String end) {
  String name=path.getName();
  return new File(path.getParent(), name.substring(0, name.length() - i).concat(end));
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
   cui cui=new cui(path);
   cui.ui = true;
   Runnable run=null;
   if (path.endsWith(".rwmod")) {
    boolean rab=raw.isChecked();
	int id=bu.getCheckedRadioButtonId();
	if (id == R.id.pr) {
	 run = new rwmodProtect(f, out(f, 6, "_r.rwmod"), cui, rab);
	} else if (id == R.id.pack)run = new zippack(f, out(f, 6, "_p.rwmod"), rab, cui);
	else run = new zipunpack(f, out(f, 6, "_u.rwmod"), rab, cui);
   } else if (path.endsWith(".apk")) {
	run = new lib(f, null, new File(getExternalFilesDir(null), "lib.zip"), cui);
   } else if (path.endsWith(".rwsave") || path.endsWith(".replay")) {
	run = new savedump(f,  out(f, 6, "tmx"), cui);
   } else if (path.endsWith(".tmx")) {
	run = new rwmap(f, out(f, 4, "_r.tmx"), cui);
   } else if (path.endsWith(".png")) {
    run = new png(f, out(f, 4, "_r.png"), cui);
   }
   if (run != null) {
    rust.ui.pool.execute(run);
    arr.add(cui);
   }
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
    int i=datas.getItemCount(),t=0;
    while (t < i) {
     uri = datas.getItemAt(t).getUri();
     add(uri);
     ++t;
    }
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
  if (is && lib.libMap == null)lib();
 }
 public void log(View v) {
  log.debug = ((CheckBox)v).isChecked();
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
