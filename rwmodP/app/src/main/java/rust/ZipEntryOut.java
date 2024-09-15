package rust;
import java.io.File;
import org.libDeflate.ZipEntryOutput;
import org.libDeflate.ZipEntryM;
import java.io.IOException;
import org.libDeflate.ZipUtil;

public class ZipEntryOut extends ZipEntryOutput {
 public ZipEntryOut(File f)throws Exception {
  super(f);
  AsInput = false;
  int[] head=zippack.head;
  if (head != null)ZipUtil.addRandomHead(this, head);
 }
 public void finish(ZipEntryM bad[]) throws IOException {
  if (!zippack.keepUnSize) {
   for (ZipEntryM zip:list) {
    /*
     if (zip.mode > 0)zip.size=BufOutput.tableSizeFor(zip.size);
     混淆大小，解压性能影响建议禁用
     */
    zip.csize = -1;
    //使原始解压块损害，影响性能，建议禁用
   }
  }
  super.finish(bad);
 }
}
