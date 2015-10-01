package spotify;

import cache.CacheKeyPrefix;
import cache.CheckerCache;
import intersection.SpotifyOrderingCreator;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pojo.Artist;
import spotify.domain.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

public class SpotifyDataGrabberTest {

    @Mock
    private SpotifySender spotifySender;

    @Mock
    private CheckerCache cache;

    @Mock
    private SpotifyOrderingCreator spotifyOrderingCreator;

    @InjectMocks
    private SpotifyDataGrabber spotifyDataGrabber;
    private String authCode = "authCode";
    private ArrayList<Artist> artistList;

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void artistsReturnedOrdered() {
        AccessToken accessToken = new AccessToken();
        accessToken.setAccessToken(authCode);
        when(cache.getOrLookup(eq(authCode), any(Supplier.class), eq(CacheKeyPrefix.SPOTIFYACCESSTOKEN), any(Class.class))).thenReturn(accessToken);
        SpotifyTracksResponse savedTracks = createSpotifyResponse();

        when(spotifySender.getSavedTracks(authCode)).thenReturn(savedTracks);
        List<SpotifyArtist> artists = savedTracks.getItems().stream().flatMap(x -> x.getTrack().getArtists().stream()).collect(toList());
        artistList = new ArrayList<>();
        when(spotifyOrderingCreator.artistListByFrequency(artists)).thenReturn(artistList);

        List<Artist> result = spotifyDataGrabber.fetchSpotifyArtists(authCode);

        assertSame(result,artistList);

    }

    private SpotifyTracksResponse createSpotifyResponse() {
        SpotifyTracksResponse savedTracks = new SpotifyTracksResponse();
        SpotifyTracksItem item = new SpotifyTracksItem();
        SpotifyTrack track = new SpotifyTrack();
        track.setArtists(
                Arrays.asList(
                        createArtist("Genesis"),
                        createArtist("Mike and the Mechanics")
                )
        );
        item.setTrack(track);

        SpotifyTracksItem item1 = new SpotifyTracksItem();
        SpotifyTrack track1 = new SpotifyTrack();
        track1.setArtists(
                Arrays.asList(
                        createArtist("Blue October"),
                        createArtist("Mike and the Mechanics")
                )
        );
        item1.setTrack(track1);
        savedTracks.setItems(Arrays.asList(item, item1));
        return savedTracks;
    }

    private SpotifyArtist createArtist(String name) {
        SpotifyArtist artist = new SpotifyArtist();
        artist.setName(name);
        return artist;
    }
}