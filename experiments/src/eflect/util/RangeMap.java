package eflect.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * A wrapper around a map that allows searching of keys in a range.
 *
 * <p>A key k's bucket is such that bucket_i.value <= k < bucket_i+1.value.
 */
public final class RangeMap<K extends Comparable, V> {
  private final TreeMap<K, V> data = new TreeMap<>();

  public RangeMap(Map<K, V> data) {
    for (K k : data.keySet()) {
      this.data.put(k, data.get(k));
    }
  }

  /** Checks if a key is in the key range of the data. */
  public boolean contains(K k) {
    return !data.isEmpty() && k != null && findKey(k) != null;
  }

  /** Gets the data in the bucket closest to the key. */
  public V get(K k) {
    if (data.isEmpty() || k == null) {
      return null;
    }
    K key = findKey(k);
    if (key == null) {
      return null;
    }
    return data.get(key);
  }

  /** Returns the underlying data. */
  public Collection<V> values() {
    return data.values();
  }

  private K findKey(K k) {
    ArrayList<K> keys = new ArrayList<>(data.keySet());
    Collections.sort(keys);
    if (keys.get(0).compareTo(k) > 0) {
      return null;
    }
    for (int i = 0; i < keys.size(); i++) {
      if (keys.get(i).compareTo(k) <= 0
          && (i + 1 == keys.size() || keys.get(i + 1).compareTo(k) > 0)) {
        return keys.get(i);
      }
    }
    return null;
  }
}
