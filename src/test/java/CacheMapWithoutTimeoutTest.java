import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;


@RunWith(PowerMockRunner.class)
@PrepareForTest(CacheMapWithTimeout.class)
public class CacheMapWithoutTimeoutTest {

    @Before
    public void setUp() {
        mockStatic(System.class);
    }
    
    @Test
    public void testTTLNotEvicted() {
        when(System.nanoTime()).thenReturn(10L);
        CacheMapWithTimeout<Integer, String> map = new CacheMapWithTimeout<>();
        map.put(1, "first");
        when(System.nanoTime()).thenReturn(10L + map.getTimeToLiveMs() - 5L);
        String first = map.get(1);
        assertEquals("first", first);
    }

    @Test
    public void testTTLEvicted() {
        when(System.nanoTime()).thenReturn(10L);
        CacheMapWithTimeout<Integer, String> map = new CacheMapWithTimeout<>();
        map.put(1, "first");
        when(System.nanoTime()).thenReturn(10L + map.getTimeToLiveMs() + 1L);
        String first = map.get(1);
        assertEquals(null, first);
    }


}
