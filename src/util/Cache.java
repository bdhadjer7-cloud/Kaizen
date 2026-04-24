package util;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
public class Cache<K,V> {
    private final Map<K,V>    store  = new ConcurrentHashMap<>();
    private final Map<K,Long> expiry = new ConcurrentHashMap<>();
    private final long ttlMs;
    public Cache(long ttlMs) { this.ttlMs=ttlMs; }
    public void put(K k, V v) { store.put(k,v); expiry.put(k,System.currentTimeMillis()+ttlMs); }
    public Optional<V> get(K k) {
        Long e=expiry.get(k);
        if(e==null||System.currentTimeMillis()>e){store.remove(k);expiry.remove(k);return Optional.empty();}
        return Optional.ofNullable(store.get(k));
    }
    public boolean has(K k)       { return get(k).isPresent(); }
    public int size()              { return store.size(); }
    public void invalidate(K k)   { store.remove(k); expiry.remove(k); }
}
