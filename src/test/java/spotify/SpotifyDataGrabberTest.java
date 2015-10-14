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
import java.util.stream.Stream;

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
    private ArrayList<Artist> artistList = new ArrayList<>();

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }


    //TODO Don't replicate logic from impl in test
    @Test
    public void artistsReturnedOrdered() {
        AccessToken accessToken = new AccessToken();
        accessToken.setAccessToken(authCode);
        when(cache.getOrLookup(eq(authCode), any(Supplier.class), eq(CacheKeyPrefix.SPOTIFYACCESSTOKEN), any(Class.class))).thenReturn(accessToken);
        SpotifyTracksResponse savedTracks = createSpotifyResponse();
        List<SpotifyPlaylistTracksResponse> playListTracks = createSpotifyPlaylistTracksResponseList();

        when(spotifySender.getSavedTracks(authCode)).thenReturn(Arrays.asList(savedTracks));
        List<SpotifyArtist> artists = savedTracks.getItems().stream().flatMap(x -> x.getTrack().getArtists().stream()).collect(toList());

        when(spotifySender.getPlayListTracks(authCode)).thenReturn(playListTracks);
        List<SpotifyArtist> playlistArtists = playListTracks.stream().flatMap(x -> x.getItems().stream()).flatMap(x -> x.getTrack().getArtists().stream()).collect(toList());

        List<SpotifyArtist> combined = Stream.concat(artists.stream(), playlistArtists.stream()).collect(toList());

        when(spotifyOrderingCreator.artistListByFrequency(combined)).thenReturn(artistList);

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

    private List<SpotifyPlaylistTracksResponse> createSpotifyPlaylistTracksResponseList() {
        SpotifyPlaylistTracksResponse response = new SpotifyPlaylistTracksResponse();

        SpotifyTracksItem item = new SpotifyTracksItem();
        SpotifyTrack track = new SpotifyTrack();
        track.setArtists(
                Arrays.asList(
                        createArtist("Linkin Park"),
                        createArtist("Clemie Fischer")
                )
        );
        item.setTrack(track);

        SpotifyTracksItem item1 = new SpotifyTracksItem();
        SpotifyTrack track1 = new SpotifyTrack();
        track1.setArtists(
                Arrays.asList(
                        createArtist("The Naked and Famous"),
                        createArtist("Mike and the Mechanics")
                )
        );
        item1.setTrack(track1);
        response.setItems(Arrays.asList(item, item1));

        SpotifyPlaylistTracksResponse response2 = new SpotifyPlaylistTracksResponse();
        SpotifyTracksItem item2 = new SpotifyTracksItem();
        SpotifyTrack track2 = new SpotifyTrack();
        track2.setArtists(
                Arrays.asList(
                        createArtist("Die Prinzen")
                )
        );
        item2.setTrack(track);
        response2.setItems(Arrays.asList(item2));

        return Arrays.asList(response,response2);
    }

    private SpotifyArtist createArtist(String name) {
        SpotifyArtist artist = new SpotifyArtist();
        artist.setName(name);
        return artist;
    }
}