package com.cache;

import java.util.*;

//TreeMap based LFU cache, linked list could be used for faster implementations with O(1)
public class LFUCache<K, V>  {

    private int cacheCapacity;
    private Map<K, CacheEntry<K, V>> cacheMap = new HashMap<K, CacheEntry<K, V>>();
    private NavigableMap<Long, K> evictionQueue = new TreeMap<Long, K>();

    public LFUCache(int cacheCapacity) {
        this.cacheCapacity = cacheCapacity;
    }

    public V put(K key, V value) {
        if (cacheCapacity == cacheMap.size()) {
            K keyToRemove = evictionQueue.firstEntry().getValue();
            cacheMap.remove(keyToRemove);
        }
        return cacheMap.put(key, new CacheEntry<K, V>(key, value, 0)).getValue();
    }

    public V get(K key) {
        CacheEntry<K, V> cacheEntry = cacheMap.get(key);
        if (cacheEntry != null) {
            Long usageRate = cacheEntry.getUsageRate();
            evictionQueue.remove(usageRate);
            cacheEntry.setUsageRate(usageRate + 1);
            evictionQueue.put(cacheEntry.getUsageRate(), cacheEntry.getKey());
            return cacheEntry.getValue();
        } else {
            return null;
        }
    }

    private static class CacheEntry<K, V> {
        private K key;
        private V value;
        private long usageRate;

        public CacheEntry(K key, V value, long usageRate) {
            this.key = key;
            this.value = value;
            this.usageRate = usageRate;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public long getUsageRate() {
            return usageRate;
        }

        public void setUsageRate(long usageRate) {
            this.usageRate = usageRate;
        }
    }
}
