package intersection;

import cache.CacheKeyPrefix;
import cache.CheckerCache;
import domain.RumourResponse;
import efestivals.domain.Act;
import exception.FestivalConnectionException;
import efestivals.GlastoRequestSender;
import lastfm.LastFmSender;
import lastfm.domain.Artist;
import lastfm.domain.Recommendations;
import lastfm.domain.Response;
import lastfm.domain.TopArtists;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import spotify.SpotifySender;

import java.util.*;
import java.util.function.Supplier;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

public class RumourIntersectionFinderTest {

    @Mock
    private GlastoRequestSender efestivalSender;
    @Mock
    private LastFmSender lastFmSender;
    @Mock
    private SpotifySender spotifySender;
    @Mock
    private CheckerCache cache;
    @Mock
    private ArtistMapGenerator artistMapGenerator;

    @InjectMocks
    private RumourIntersectionFinder rumourIntersectionFinder;
    private final String year = "year";
    private final String festival = "festival";
    private final String username = "username";
    private final String token = "token";

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void correctIntersectionCalculate() throws FestivalConnectionException {
        Set<Act> festivalData = generateFestivalData();
        when(efestivalSender.getFestivalData(festival,year)).thenReturn(festivalData);
        Response response = generateLastFmData();
        when(cache.getOrLookup(eq(username),any(Supplier.class), eq(CacheKeyPrefix.LISTENED),any(Class.class))).thenReturn(response);
        when(artistMapGenerator.generateLastFmMap(festivalData,response.getTopartists().getArtist())).thenReturn(getGenerateLastFmMap());

        RumourResponse intersection = rumourIntersectionFinder.findIntersection(username, festival, year);

        assertEquals(intersection.getActs(), expectedActList());
    }

    @Test
    public void correctRecommendedIntersectionCalculate() throws FestivalConnectionException {
        Set<Act> festivalData = generateFestivalData();
        when(efestivalSender.getFestivalData(festival,year)).thenReturn(festivalData);
        Response response = generateRecommendedLastFmData();
        when(cache.getOrLookup(eq(token),any(Supplier.class), eq(CacheKeyPrefix.RECCOMENDED),any(Class.class))).thenReturn(response);
        when(artistMapGenerator.generateLastFmMap(festivalData,response.getRecommendations().getArtist())).thenReturn(getGenerateLastFmMap());

        List<Act> intersection = rumourIntersectionFinder.findRecommendedIntersection(token, festival, year);

        assertEquals(intersection, expectedActList());
    }

    private List<Act> expectedActList() {
        Act a1 = new Act("Genesis", "Saturday", "Pyramid", "Confirmed");
        Act a2 = new Act("Talking Heads", "Saturday", "Other", "Confirmed");
        return Arrays.asList(a2,a1);
    }

    private Map<String, Artist> getGenerateLastFmMap() {
        Map<String,Artist> map = new HashMap<>();
        map.put("talking heads", new Artist("talking heads", "10", 3));
        map.put("genesis", new Artist("genesis", "5", 15));
        map.put("blue october", new Artist("blue october", "10000", 1));
        return map;
    }

    private Response generateLastFmData() {
        List<Artist> artists = artistList();
        TopArtists topartists = new TopArtists();
        topartists.setArtist(artists);
        Response response = new Response();
        response.setTopartists(topartists);
        return response;
    }

    private Response generateRecommendedLastFmData() {
        List<Artist> artists = artistList();
        Recommendations recommendations = new Recommendations();
        recommendations.setArtist(artists);
        Response response = new Response();
        response.setRecommendations(recommendations);
        return response;
    }

    private List<Artist> artistList() {
        Artist a1 = new Artist("talking heads", "10", 3);
        Artist a2 = new Artist("genesis", "5", 15);
        Artist a3 = new Artist("blue october", "10000", 1);
        return Arrays.asList(a1, a2, a3);
    }

    private Set<Act> generateFestivalData() {
        Act a1 = new Act("Genesis", "Saturday", "Pyramid", "Confirmed");
        Act a2 = new Act("Talking Heads", "Saturday", "Other", "Confirmed");
        Act a3 = new Act("Lady Gaga", "Saturday", "John Peel", "Strong Rumour");
        return new HashSet<>(Arrays.asList(a1,a2,a3));
    }
}