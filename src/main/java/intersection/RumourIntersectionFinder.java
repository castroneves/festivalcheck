package intersection;

import cache.CheckerCache;
import com.google.inject.Inject;
import domain.RumourResponse;
import exception.FestivalConnectionException;
import efestivals.GlastoRequestSender;
import lastfm.LastFmSender;
import efestivals.domain.Act;
import lastfm.domain.Artist;
import lastfm.domain.Recommendations;
import lastfm.domain.Response;
import spotify.SpotifyDataGrabber;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cache.CacheKeyPrefix.LISTENED;
import static cache.CacheKeyPrefix.RECCOMENDED;
import static java.util.stream.Collectors.toList;

/**
 * Created by Adam on 24/09/2015.
 */
public class RumourIntersectionFinder {
    @Inject
    private GlastoRequestSender efestivalSender;
    @Inject
    private LastFmSender lastFmSender;
    @Inject
    private SpotifyDataGrabber spotifyDataGrabber;
    @Inject
    private SpotifyOrderingCreator orderingCreator;
    @Inject
    private CheckerCache cache;
    @Inject
    private ArtistMapGenerator artistMapGenerator;
    @Inject
    private RecommendedArtistGenerator recommendedArtistGenerator;

    public RumourResponse findIntersection(String username, String festival,String year) throws FestivalConnectionException {
        Response lastFmData = cache.getOrLookup(username, () -> lastFmSender.simpleRequest(username), LISTENED, Response.class);
        List<Artist> artists = lastFmData.getTopartists().getArtist();

        List<Act> acts = computeIntersection(artists, festival, year, Artist::getRankValue);
        return new RumourResponse(acts);
    }

    public List<Act> findRecommendedIntersection(String token, String festival, String year) throws FestivalConnectionException {
        Response lastFmResponse = cache.getOrLookup(token, () -> lastFmSender.recommendedRequest(token), RECCOMENDED, Response.class);
        List<Artist> artists = lastFmResponse.getRecommendations().getArtist();

        return computeIntersection(artists,festival,year, Artist::getRankValue);
    }

    public List<Act> computeRecommendedIntersection(String username, String festival,String year) {
        long x = System.currentTimeMillis();
        Response lastFmData = cache.getOrLookup(username, () -> lastFmSender.simpleRequest(username), LISTENED, Response.class);
        List<Artist> artists = lastFmData.getTopartists().getArtist();
        Recommendations recArtists = cache.getOrLookup(username, () -> recommendedArtistGenerator.fetchRecommendations(artists), RECCOMENDED, Recommendations.class);
        System.out.println(System.currentTimeMillis() - x);
        List<Act> acts = computeIntersection(recArtists.getArtist(), festival, year, Artist::getRankValue);
        System.out.println(System.currentTimeMillis() - x);
        return acts;

    }

    public List<Act> findSpotifyIntersection(String authCode, String festival, String year, String redirectUrl) throws FestivalConnectionException {
        List<Artist> artists = spotifyDataGrabber.fetchSpotifyArtists(authCode, redirectUrl);

        return computeIntersection(artists,festival,year,x -> -1 * x.getPlaycountInt());
    }

    private List<Act> computeIntersection(List<Artist> artists, String festival, String year, Function<Artist,Integer> func) throws FestivalConnectionException {
        Set<Act> glastoData = efestivalSender.getFestivalData(festival, year);
        Map<String, Artist> lastFmMap = artistMapGenerator.generateLastFmMap(glastoData,artists);

        return glastoData.stream().filter(g -> lastFmMap.containsKey(g.getName().toLowerCase()))
                .map(g -> new Act(g,lastFmMap.get(g.getName().toLowerCase()).getPlaycount(),lastFmMap.get(g.getName().toLowerCase()).getRankValue()))
                .map(g -> new Act(g,lastFmMap.get(g.getName().toLowerCase()).getMatch()))
                .sorted((x, y) -> Integer.compare(func.apply(lastFmMap.get(x.getName().toLowerCase())),
                        func.apply(lastFmMap.get(y.getName().toLowerCase()))))
                .collect(toList());

    }

}
