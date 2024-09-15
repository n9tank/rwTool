package java.nio.charset;
import java.nio.charset.CharsetEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.CoderResult;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;

public class Iso extends CharsetEncoder {
 CharsetEncoder en;
 public Iso() {
  super(StandardCharsets.ISO_8859_1, 1.0f, 1.0f);
  en = StandardCharsets.ISO_8859_1.newEncoder();
 }
 //我不知道为什么这个方法对于一些字符返回true，我试着重载它
 public boolean canEncode(CharSequence cs) {
  int i=cs.length();
  while (-- i >= 0) {
   if (cs.charAt(i) > 0xff)return false;
  }
  return true;
 }
 protected CoderResult encodeLoop(CharBuffer in, ByteBuffer out) {
  return en.encodeLoop(in,out);
 }
}
