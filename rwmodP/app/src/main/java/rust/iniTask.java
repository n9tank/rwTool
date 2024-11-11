package rust;
import java.io.IOException;
import java.util.concurrent.Callable;
import org.libDeflate.ErrorHandler;

public class iniTask implements Callable {
 loader lod;
 rwmodProtect pot;
 ErrorHandler err;
 public iniTask(rwmodProtect pot, loader lod, ErrorHandler e) throws IOException {
  this.pot = pot;
  this.lod = lod;
  err = e;
  e.add(this);
 }
 public Object call() throws Exception {
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
