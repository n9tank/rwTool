package rust;
import carsh.log;
import java.util.zip.ZipFile;
import org.libDeflate.ErrorHandler;
import org.libDeflate.ParallelDeflate;

public class UiHandler extends ErrorHandler {
 ui ui;
 ZipFile file;
 TaskWait task;
 public UiHandler(ParallelDeflate para, ZipFile zip, TaskWait task) {
  super(para);
  file = zip;
  this.task = task;
  ui = task.back;
 }
 public UiHandler(ParallelDeflate para, ZipFile zip, ui ui) {
  super(para);
  file = zip;
  this.ui = ui;
 }
 public boolean onError(Exception err) {
  log.e(this, err);
  if (super.onError(err)) {
   if (task != null)task.down(ex);
   close();
  }
  return true;
 }
 public void close() {
  ZipFile zip=file;
  file = null;
  if (zip != null) {
   try {
    zip.close();
   } catch (Exception e) {}
   ui.end(ex);
  } 
 }
 public void onClose() {
  close();
 }
}
