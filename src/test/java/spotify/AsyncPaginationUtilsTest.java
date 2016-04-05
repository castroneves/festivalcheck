package spotify;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import spotify.domain.SpotifyDetails;
import spotify.domain.SpotifyPlaylist;
import spotify.domain.SpotifyPlaylistResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Created by Adam on 29/02/2016.
 */
public class AsyncPaginationUtilsTest {

    @Mock
    private Future<SpotifyPlaylistResponse> future;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    public void paginatesCorrectly() throws Exception {
        BiFunction<Integer, SpotifyDetails, Future<SpotifyPlaylistResponse>> func = (x,y) -> future;
        SpotifyPlaylistResponse initialResponse = new SpotifyPlaylistResponse();
        initialResponse.setTotal(200);
        List<SpotifyPlaylist> initialList = new ArrayList<>(Collections.nCopies(50, new SpotifyPlaylist()));
        initialResponse.setItems(initialList);
        when(future.get(2000, TimeUnit.MILLISECONDS)).thenReturn(initialResponse);
        when(future.get(1500, TimeUnit.MILLISECONDS)).thenReturn(new SpotifyPlaylistResponse());

        List<SpotifyPlaylistResponse> result = AsyncPaginationUtils.paginateAsync(func, new SpotifyDetails("ac"), 50);

        assertEquals(4, result.size());
    }

    @Test
    public void paginatesCorrectlyUnevenCount() throws Exception {
        BiFunction<Integer, SpotifyDetails, Future<SpotifyPlaylistResponse>> func = (x,y) -> future;
        SpotifyPlaylistResponse initialResponse = new SpotifyPlaylistResponse();
        initialResponse.setTotal(200);
        List<SpotifyPlaylist> initialList = new ArrayList<>(Collections.nCopies(90, new SpotifyPlaylist()));
        initialResponse.setItems(initialList);
        when(future.get(2000, TimeUnit.MILLISECONDS)).thenReturn(initialResponse);
        when(future.get(1500, TimeUnit.MILLISECONDS)).thenReturn(new SpotifyPlaylistResponse());

        List<SpotifyPlaylistResponse> result = AsyncPaginationUtils.paginateAsync(func, new SpotifyDetails("ac"), 90);

        assertEquals(3, result.size());
    }

    @Test
    public void returnsEmptyWhenNullInitialResponse() throws Exception {
        BiFunction<Integer, SpotifyDetails, Future<SpotifyPlaylistResponse>> func = (x,y) -> future;
        when(future.get(2000, TimeUnit.MILLISECONDS)).thenReturn(null);
        when(future.get(1500, TimeUnit.MILLISECONDS)).thenReturn(new SpotifyPlaylistResponse());

        List<SpotifyPlaylistResponse> result = AsyncPaginationUtils.paginateAsync(func, new SpotifyDetails("ac"), 90);

        assertEquals(0, result.size());
    }

    @Test
    public void timedoutGetsExcludedFromResults() throws Exception {
        BiFunction<Integer, SpotifyDetails, Future<SpotifyPlaylistResponse>> func = (x,y) -> future;
        SpotifyPlaylistResponse initialResponse = new SpotifyPlaylistResponse();
        initialResponse.setTotal(200);
        List<SpotifyPlaylist> initialList = new ArrayList<>(Collections.nCopies(90, new SpotifyPlaylist()));
        initialResponse.setItems(initialList);
        when(future.get(2000, TimeUnit.MILLISECONDS)).thenReturn(initialResponse);
        when(future.get(1500, TimeUnit.MILLISECONDS)).thenReturn(new SpotifyPlaylistResponse(),null, new SpotifyPlaylistResponse());

        List<SpotifyPlaylistResponse> result = AsyncPaginationUtils.paginateAsync(func, new SpotifyDetails("ac"), 90);

        assertEquals(2, result.size());
    }

}