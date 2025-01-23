package rust;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class MapResize implements Map {
 public int size;
 public int size() {
  return size;
 }
 public Set entrySet() {
  return Collections.emptySet();
 }
 public Set keySet() {
  return null;
 }
 public void clear() {
 }
 public boolean containsKey(Object key) {
  return false;
 }
 public boolean containsValue(Object value) {
  return false;
 }
 public Object get(Object key) {
  return null;
 }
 public boolean isEmpty() {
  return true;
 }
 public Object put(Object key, Object value) {
  return null;
 }
 public void putAll(Map m) {
 }
 public Object remove(Object key) {
  return null;
 }
 public Collection values() {
  return null;
 }
}
