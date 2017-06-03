package spotify;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import spotify.domain.SpotifyDetails;
import spotify.domain.SpotifyPlaylist;
import spotify.domain.SpotifyPlaylistResponse;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * Created by Adam on 29/02/2016.
 */
public class AsyncPaginationUtilsTest {

    @Mock
    private Future<Response> future;

    @Mock
    private Response response1;

    @Mock
    private Response response2;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    public void paginatesCorrectly() throws Exception {
        BiFunction<Integer, SpotifyDetails, Future<Response>> func = (x, y) -> future;
        SpotifyPlaylistResponse initialResponse = new SpotifyPlaylistResponse();
        initialResponse.setTotal(200);
        List<SpotifyPlaylist> initialList = new ArrayList<>(Collections.nCopies(50, new SpotifyPlaylist()));
        initialResponse.setItems(initialList);

        when(future.get(1500, TimeUnit.MILLISECONDS)).thenReturn(response1, response2);

        when(response1.readEntity(any(Class.class))).thenReturn(initialResponse);
        when(response2.readEntity(any(Class.class))).thenReturn(new SpotifyPlaylistResponse());

        List<SpotifyPlaylistResponse> result = AsyncPaginationUtils.paginateAsync(func, SpotifyPlaylistResponse.class, new SpotifyDetails("ac"), 50);

        assertEquals(4, result.size());
    }

    @Test
    public void paginatesCorrectlyUnevenCount() throws Exception {
        BiFunction<Integer, SpotifyDetails, Future<Response>> func = (x, y) -> future;
        SpotifyPlaylistResponse initialResponse = new SpotifyPlaylistResponse();
        initialResponse.setTotal(200);
        List<SpotifyPlaylist> initialList = new ArrayList<>(Collections.nCopies(90, new SpotifyPlaylist()));
        initialResponse.setItems(initialList);
        when(future.get(1500, TimeUnit.MILLISECONDS)).thenReturn(response1, response2);

        when(response1.readEntity(any(Class.class))).thenReturn(initialResponse);
        when(response2.readEntity(any(Class.class))).thenReturn(new SpotifyPlaylistResponse());

        List<SpotifyPlaylistResponse> result = AsyncPaginationUtils.paginateAsync(func, SpotifyPlaylistResponse.class, new SpotifyDetails("ac"), 90);

        assertEquals(3, result.size());
    }

    @Test
    public void returnsEmptyWhenNullInitialResponse() throws Exception {
        BiFunction<Integer, SpotifyDetails, Future<Response>> func = (x, y) -> future;
        when(future.get(1500, TimeUnit.MILLISECONDS)).thenReturn(response1, response2);

        when(response1.readEntity(any(Class.class))).thenReturn(null);
        when(response2.readEntity(any(Class.class))).thenReturn(new SpotifyPlaylistResponse());

        List<SpotifyPlaylistResponse> result = AsyncPaginationUtils.paginateAsync(func, SpotifyPlaylistResponse.class, new SpotifyDetails("ac"), 90);

        assertEquals(0, result.size());
    }


}