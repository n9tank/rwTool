package rust;

import java.io.*;
import java.util.zip.*;
import org.libDeflate.*;

public class savedump implements Runnable {
 File in;
 File ou;
 UIPost ui;
 public static String readIsString(DataInput input) throws IOException{
  return input.readBoolean() ?input.readUTF(): null;
 } 
 public static void saveDump(File in, File ou) throws Exception{
  BufferedInputStream input= new BufferedInputStream(new FileInputStream(in));
  DataInputStream data=new DataInputStream(input);
  try{
   String str=data.readUTF();
   if (!"rustedWarfareSave".equals(str)){
	if (!"rustedWarfareReplay".equals(str)){
	 throw new DataFormatException();
	}
	//rustedWarfareReplay
	data.readInt();
	data.readInt();
	data.readUTF();
	data.readBoolean();
	data.readUTF();
	//gamesave
	data.readInt();
	data.readUTF();
   }
   //rustedWarfareSave
   data.readInt();
   data.readInt();
   data.readBoolean();
   data.readUTF();
   //saveCompression
   int gzipsize=data.readInt();
   DataInputStream datagzip=new DataInputStream(new BufferedInputStream(new GZIPInputStream(input, 1024)));
   try{
	datagzip.readUTF();
	//customUnitsBlock
	int customUnitsBlocklen=datagzip.readInt();
	datagzip.skip(customUnitsBlocklen);
	/*
	 datagzip.readUTF();
	 //customUnits
	 datagzip.readInt();
	 int modunitlen= datagzip.readInt();
	 for(int i=0;i<modunitlen;i++){
	 datagzip.readUTF();
	 datagzip.readInt();
	 datagzip.readBoolean();
	 readIsString(datagzip);
	 datagzip.readLong();
	 datagzip.readLong();
	 }*/
	datagzip.readUTF();
	int gameSetuplen=datagzip.readInt();
	datagzip.skip(gameSetuplen);
	//gameSetup
	/*
	 boolean isStarting= datagzip.readBoolean();
	 boolean isHaveAi= datagzip.readBoolean();
	 if(data.readBoolean()){
	 datagzip.readByte();
	 datagzip.readByte();
	 int fog=datagzip.readInt();
	 int credits=datagzip.readInt();
	 data.readBoolean();
	 int aidiff= datagzip.readInt();
	 int initunit=datagzip.readInt();
	 float income=datagzip.readFloat();
	 boolean nukes=datagzip.readBoolean();
	 datagzip.readBoolean();
	 boolean sharedControl=datagzip.readBoolean();
	 datagzip.readBoolean();
	 datagzip.readBoolean();
	 boolean spectators=datagzip.readBoolean();
	 datagzip.readBoolean();
	 int randomSeed=datagzip.readInt();
	 datagzip.readInt();
	 datagzip.readInt();
	 }*/
	String map=datagzip.readUTF();
	if (datagzip.readBoolean()){
	 int mapDataSize=datagzip.readInt();
	 FileOutputStream out=new FileOutputStream(ou);
	 try{
	  int len=Math.min(65536, mapDataSize);
	  byte mapData[]=new byte[len];
	  while (true){
	   datagzip.read(mapData, 0, len);
	   out.write(mapData, 0, len);
	   if ((mapDataSize -= len) <= 0)
		break;
	   len = Math.min(65536, mapDataSize);
	  }
	 }finally{
	  out.close();
	 }
	}
   }finally{
	datagzip.close();
   }
  }finally{
   data.close();
  }
 }
 public savedump(File i, File o, UIPost u) {
  in = i;
  ou = o;
  ui = u;
 }
 public void run() {
  Throwable ex=null;
  try {
   saveDump(in, ou);
  } catch (Throwable e) {
   ex = e;
  }
  if (ex != null)ou.delete();
  ui.accept(UiHandler.toList(ex));
 }
}
