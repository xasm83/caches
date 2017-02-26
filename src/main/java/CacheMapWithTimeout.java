public class CacheMapWithTimeout<K, V> {

    private long timeToLiveMs = 60000L;
    private int capacity = 8;
    private float loadFactor = 0.75F;

    @SuppressWarnings("unchecked")
    private Entry<K, V>[] entries = (Entry<K, V>[]) new Entry[capacity];

    public CacheMapWithTimeout() {
    }

    public CacheMapWithTimeout(long timeToLiveMs, int capacity, int loadFactor) {
        this.timeToLiveMs = timeToLiveMs;
        this.capacity = capacity;
        this.loadFactor = loadFactor;
    }

    public long getTimeToLiveMs() {
        return timeToLiveMs;
    }

    public void setTimeToLiveMs(long timeToLiveMs) {
        this.timeToLiveMs = timeToLiveMs;
    }

    public V put(K key, V value) {
        return putEntry(key, value, entries);
    }

    private V putEntry(K key, V value, Entry<K, V>[] entriesToUse) {
        clearExpiredInternal();
        resize();
        int i = getEntryIndex(key);
        if (entriesToUse[i] == null) {
            entriesToUse[i] = new Entry<K, V>(key, value, System.nanoTime());
        } else {
            Entry<K, V> current = entriesToUse[i];
            while (current != null) {
                if (areKeysEqual(key, current)) {
                    V oldValue = current.value;
                    current.value = value;
                    return oldValue;
                }
                if (current.next == null) {
                    current.next = new Entry<K, V>(key, value, System.nanoTime());
                    return null;
                }
                current = current.next;
            }
        }
        return null;
    }


    public void clearExpired() {
        clearExpiredInternal();
        resize();
    }

    @SuppressWarnings("unchecked")
    public void clear() {
        entries = (Entry<K, V>[]) new Entry[capacity];
    }


    public boolean containsKey(K key) {
        return get(key) != null;
    }


    public boolean containsValue(V value) {
        clearExpiredInternal();
        resize();
        for (int i = 0; i < entries.length; i++) {
            if (entries[i] != null) {
                Entry<K, V> current = entries[i];
                while (current != null) {
                    V v;
                    if ((v = current.value) == value || (v != null && value.equals(v))) {
                        return true;
                    }
                    current = current.next;
                }
            }
        }
        return false;
    }


    public V get(K key) {
        clearExpiredInternal();
        resize();
        int i = getEntryIndex(key);
        if (entries[i] != null) {
            Entry<K, V> current = entries[i];
            while (current != null) {
                K k;
                if (areKeysEqual(key, current)) {
                    return current.value;
                }
                current = current.next;
            }
        }
        return null;
    }


    public boolean isEmpty() {
        clearExpiredInternal();
        resize();
        return size() == 0;
    }

    public V remove(K key) {
        clearExpiredInternal();
        resize();
        int i = getEntryIndex(key);
        if (entries[i] != null) {
            Entry<K, V> current = entries[i];
            if (areKeysEqual(key, current)) {
                entries[i] = current.next;
                return current.value;
            }
            Entry<K, V> previous = current;
            current = current.next;
            while (current != null) {
                if (areKeysEqual(key, current)) {
                    previous.next = current.next;
                    return current.value;
                } else {
                    current = current.next;
                }
            }
        }
        return null;
    }

    public int size() {
        clearExpiredInternal();
        int size = 0;
        for (int i = 0; i < entries.length; i++) {
            Entry<K, V> current = entries[i];
            while (current != null) {
                current = current.next;
                size++;
            }
        }
        return size;
    }

    private int getEntryIndex(Object key) {
        int h;
        return (key == null) ? 0 : ((h = key.hashCode()) ^ (h >>> 16)) & (entries.length - 1);
    }

    private void clearExpiredInternal() {
        for (int i = 0; i < entries.length; i++) {
            Entry<K, V> previous = null;
            Entry<K, V> current = entries[i];
            while (current != null) {
                if (current.timestamp < System.nanoTime() - timeToLiveMs) {
                    if (previous == null) {
                        entries[i] = current.next;
                    } else {
                        previous.next = current.next;
                    }
                } else {
                    previous = current;
                }
                current = current.next;
            }
        }
    }

    //it could be inlined for performance reason
    private boolean areKeysEqual(K key, Entry<K, V> entry) {
        K k;
        return (k = entry.key) == key || (key != null && key.equals(k));
    }

    private void resize() {
        clearExpiredInternal();
        int size = size();
        if (size / capacity < loadFactor) {
            return;
        }

        //it could be improved using the way JDK 8 does resize
        @SuppressWarnings("unchecked")
        Entry<K, V>[] newEntries = (Entry<K, V>[]) new Entry[capacity << 1];
        for (int i = 0; i < entries.length; i++) {
            Entry<K, V> entry = entries[i];
            while (entry != null) {
                putEntry(entry.key, entry.value, newEntries);
                entry = entry.next;
            }
            entries = newEntries;
        }
    }

    private static class Entry<K, V> {
        K key;
        V value;
        long timestamp;
        Entry<K, V> next;

        Entry(K key, V value, long timestamp) {
            this.key = key;
            this.value = value;
            this.timestamp = timestamp;
        }
    }
}
