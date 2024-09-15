package rust;
import carsh.log;
import com.googlecode.pngtastic.core.PngImage;
import com.googlecode.pngtastic.core.PngOptimizer;
import java.io.File;

public class png implements Runnable {
  File in;
  File ou;
  ui ui;
  public png(File f,File o,ui u){
    in=f;
    ou=o;
    ui=u;
  }
  public void run(){
    Throwable ex=null;
    try{
    new PngOptimizer().optimize(new PngImage(in.getPath(),null),ou.getPath(),false,9);
    }catch(Throwable e){
      log.e(this,e);
      ex=e;
    }
    if(ex!=null)ou.delete();
    ui.end(ex);
  }
}
