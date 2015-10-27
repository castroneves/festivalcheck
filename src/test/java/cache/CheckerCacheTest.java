package cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import lastfm.domain.Artist;
import lastfm.domain.Response;
import lastfm.domain.TopArtists;
import redis.clients.jedis.Jedis;

import java.util.Arrays;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

public class CheckerCacheTest {
    @Mock
    private JedisFactory jedisFactory;
    @InjectMocks
    private CheckerCache checkerCache;

    private final String key = "key";
    private final CacheKeyPrefix prefix = CacheKeyPrefix.LISTENED;

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        checkerCache.setMapper(new ObjectMapper());
    }

    @Test
    public void returnsCachedValue() {
        Jedis jedis = mock(Jedis.class);
        when(jedisFactory.newJedis()).thenReturn(jedis);
        when(jedis.get(prefix + key)).thenReturn("{\"topartists\":{\"artist\":[{\"name\":\"Peter Gabriel\",\"playcount\":\"10\",\"rank\":{\"rank\":1}},{\"name\":\"Phil Collins\",\"playcount\":\"20\",\"rank\":{\"rank\":2}}]}}");

        Response result = checkerCache.getOrLookup(key, () -> new Response(), prefix, Response.class);

        assertEquals(result.getTopartists().getArtist(), createResponse().getTopartists().getArtist());
    }

    @Test
    public void fallsbackWhenKeyNotFound() {
        Jedis jedis = mock(Jedis.class);
        when(jedisFactory.newJedis()).thenReturn(jedis);
        when(jedis.get(prefix + key)).thenReturn("");
        Response response = new Response();

        Response result = checkerCache.getOrLookup(key, () -> response, prefix, Response.class);

        assertSame(result,response);
    }

    @Test
    public void fallsbackOnException() {
        when(jedisFactory.newJedis()).thenThrow(new RuntimeException());
        Response response = new Response();

        Response result = checkerCache.getOrLookup(key, () -> response, prefix, Response.class);

        assertSame(result, response);
    }

    @Test
    public void fallbackStoresJsonAndSetsExpiry() {
        Jedis jedis = mock(Jedis.class);
        when(jedisFactory.newJedis()).thenReturn(jedis);
        when(jedis.get(prefix + key)).thenReturn("");
        Response response = createResponse();
        Response result = checkerCache.getOrLookup(key, () -> response, prefix, Response.class);

        assertSame(result,response);
        String expectedJson = "{\"topartists\":{\"artist\":[{\"name\":\"Peter Gabriel\",\"playcount\":\"10\",\"rank\":{\"rank\":1}},{\"name\":\"Phil Collins\",\"playcount\":\"20\",\"rank\":{\"rank\":2}}]}}";
        verify(jedis).set(prefix + key, expectedJson);
        verify(jedis).expire(prefix + key, 3000);
    }

    private Response createResponse() {
        Response response = new Response();
        TopArtists topartists = new TopArtists();
        Artist artist = new Artist();
        artist.setName("Peter Gabriel");
        artist.setPlaycount("10");
        artist.setRankValue(1);
        Artist artist1 = new Artist();
        artist1.setName("Phil Collins");
        artist1.setPlaycount("20");
        artist1.setRankValue(2);
        topartists.setArtist(Arrays.asList(artist, artist1));
        response.setTopartists(topartists);
        return response;
    }

}