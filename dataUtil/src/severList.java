package com.corrodinggames.rts.gameFramework.j;
import java.util.*;
import android.util.*;

public class severList{
 public static final int MAX=4;
 public static String getIp(f room){
  return room.d;
 }
 public static int getPort(f room){
  return room.g;
 }
 public static String getMessage(f room){
  return room.f;
 }
 public static String getUrl(f room){
  return room.e;
 }
 public static String getUser(f room){
  return room.n;
 }
 public static String getMap(f room){
  return room.q;
 }
 public static boolean isY(f room){
  //不需要密码 和 开放端口
  return !room.m && (room.h||room.a);
 }
 public static String getVer(f room){
  return room.k;
 }
 public static String getMod(f room){
  return room.z;
 }
 public static String getType(f room){
  return room.s;
 }
 public static String getUUID(f room){
  return room.b;
 }
 public static void run(){
  filter(null);
 }
// public static final HashSet Whitelist=new HashSet(Arrays.asList(new String[]{"IronCore","RELAY-CN","Unnamed","????????","RW-HPS"}));
 public static void filter(List<f> list){
  for(int i=list.size();--i>=0;){
   f room=list.get(i);
   String mes=getMessage(room);
   if("RELAY-CN (Github)".equals(mes))
   /* if(Whitelist.contains(mes))
	continue;*/
	list.remove(i);
  }
 }
}
