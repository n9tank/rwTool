package rust;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class ByteOut extends ByteArrayOutputStream {
 public ByteOut() {
  super(256);
 }
 public byte[] get() {
  return buf;
 }
}
