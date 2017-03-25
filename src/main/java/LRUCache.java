import java.util.HashMap;
import java.util.Map;


//O(1) for put and get
//https://leetcode.com/problems/lru-cache/#/description
public class LRUCache {
    private Map<Integer, LinkedEntry> cache = new HashMap<>();
    private int capacity;
    private LinkedEntry leastRecentlyUsedEntry;
    private LinkedEntry mostRecentlyUsedEntry;

    public LRUCache(int capacity) {
        this.capacity = capacity;
    }

    public int get(int key) {
        LinkedEntry entry = cache.get(key);
        if (entry == null) {
            return -1;
        } else if (capacity > 1) {
            makeMostRecentlyEntry(entry);
        }
        return entry.value;
    }

    public void put(int key, int value) {
        LinkedEntry entry = cache.get(key);
        if (entry != null) {
            entry.value = value;
        } else {
            if (cache.size() == capacity) {
                cache.remove(leastRecentlyUsedEntry.key);
                leastRecentlyUsedEntry = leastRecentlyUsedEntry.previous;
                if (leastRecentlyUsedEntry != null) {
                    removeNodeFromList(leastRecentlyUsedEntry.next);
                }
            }
            entry = new LinkedEntry(key, value);
            if (cache.size() == 0) {
                leastRecentlyUsedEntry = entry;
                mostRecentlyUsedEntry = entry;
            }
            cache.put(key, entry);
        }
        makeMostRecentlyEntry(entry);
    }

    private void makeMostRecentlyEntry(LinkedEntry entry) {
        if (mostRecentlyUsedEntry != entry) {
            if (entry == leastRecentlyUsedEntry) {
                leastRecentlyUsedEntry = entry.previous;
            }
            removeNodeFromList(entry);
            entry.next = mostRecentlyUsedEntry;
            mostRecentlyUsedEntry.previous = entry;
            mostRecentlyUsedEntry = entry;
        }
    }

    private void removeNodeFromList(LinkedEntry entry) {
        if (entry.previous != null) {
            entry.previous.next = entry.next;
            if (entry.next != null) {
                entry.next.previous = entry.previous;
            }
        }
    }

    private class LinkedEntry {
        LinkedEntry next;
        LinkedEntry previous;
        int value;
        int key;

        LinkedEntry(int key, int value) {
            this.key = key;
            this.value = value;
        }
    }
}

