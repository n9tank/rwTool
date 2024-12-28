
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.HashSet;
import org.json.JSONArray;
import org.json.JSONObject;

public class test2 {

 static File outPath=new File("sdcard/b");
 public static void main(String[] args) throws Exception {
  outfromdata2();
  System.out.println();
 }
 /*
  https://products.aspose.app/cells/zh/conversion/xlsx-to-json
  NDT-1.15.json xlxs2json
  */
 public static void outfromdata2() throws Exception {
  JSONObject js=pare(new File("sdcard/a"));
  JSONArray list= js.getJSONArray("单位代码");
  HashSet set=new HashSet();
  BufferedWriter buff=new BufferedWriter(new FileWriter(outPath));
  boolean isclose=false;
  for (int i=0;i < list.length();++i) {
   JSONObject obj=list.optJSONObject(i);
   if (obj == null) {
    isclose = true;
    continue;
   }
   String str=obj.optString("key翻译");
   if (str == null)continue;
   String key=obj.optString("key描述解释");
   boolean isSection=key == null ?false: key.startsWith("[") && key.endsWith("]");
   if (isSection || str.matches("[a-zA-Z]{2,}")) {
    if (isSection) {
     int j=key.indexOf('_');
     if (j < 0)j = key.length() - 1;
     str = key.substring(1, j);
    }
    isclose = "Logic".equals(str);
   } else if (!isclose) {
    key = obj.optString("key代码");
    if (key == null || key.length() == 0)continue;
    char c=key.charAt(0);
    if (Character.isLowerCase(c) || Character.isUpperCase(c) || c == '@') {
     String type=obj.optString("key值类型", "string");
     if (!"dont_load".equals(key) && "bool".equals(type)) {
      String[] lv=key.split("_");
      if (lv.length > 1 && ("#".equals(lv[1]) || "TYPE".equals(lv[1]))) {
       continue;
      }
      if (set.add(key)) {
       buff.write(key);
       buff.write(',');
      }
     }
    }
   }
  }
  buff.write("action_,");
  buff.write("builtFrom_,");
  buff.write("canBuild_,");
  buff.write("animation_");
  buff.close();
 }
 public static JSONObject pare(File f) throws Exception {
  FileInputStream fi=new FileInputStream(f);
  byte[] by=new byte[fi.available()];
  fi.read(by);
  return new JSONObject(new String(by));
 }
}

