package rust;
import java.io.IOException;
import java.util.concurrent.Callable;
import org.libDeflate.ErrorHandler;

public class iniTask implements Callable {
 loader lod;
 rwmodProtect pot;
 public iniTask(rwmodProtect pot, loader lod) throws IOException {
  this.pot = pot;
  this.lod = lod;
  pot.uih.add(this);
 }
 public Object call() throws Exception {
  ErrorHandler err=pot.uih;
  if (!err.iscancel()) {
   try {
	if (!pot.write(lod)) {
	 err.addN(this);
	 return null;
	}
   } catch (Throwable e) {
	err.onError(e);
   }
  }
  err.pop();
  return null;
 }
}
