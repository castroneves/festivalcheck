package intersection;

import cache.CheckerCache;
import com.google.inject.Inject;
import domain.RumourResponse;
import exception.FestivalConnectionException;
import efestivals.GlastoRequestSender;
import lastfm.LastFmSender;
import efestivals.domain.Act;
import lastfm.domain.Artist;
import lastfm.domain.Response;
import spotify.SpotifyDataGrabber;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

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
    private CheckerCache cache;
    @Inject
    private ArtistMapGenerator artistMapGenerator;

    public RumourResponse findIntersection(String username, String festival,String year) throws FestivalConnectionException {
        Response lastFmData = cache.getOrLookup(username, () -> lastFmSender.simpleRequest(username), LISTENED, Response.class);
        List<Artist> artists = lastFmData.getTopartists().getArtist();

        List<Act> acts = computeIntersection(artists, festival, year, Artist::getRankValue);
        return new RumourResponse(acts);
    }

    public List<Act> findRecommendedIntersection(String token, String festival, String year) throws FestivalConnectionException {
        Response lastFmResponse = cache.getOrLookup(token, () -> lastFmSender.recommendedRequest(token), RECCOMENDED, Response.class);
        List<Artist> artists = lastFmResponse.getRecommendations().getArtist();

        return computeIntersection(artists,festival,year,Artist::getRankValue);
    }

    public List<Act> findSpotifyIntersection(String authCode, String festival, String year, String redirectUrl) throws FestivalConnectionException {
        List<Artist> artists = spotifyDataGrabber.fetchSpotifyArtists(authCode, redirectUrl);

        return computeIntersection(artists,festival,year,x -> -1 * x.getPlaycountInt());
    }

    private List<Act> computeIntersection(List<Artist> artists, String festival, String year, Function<Artist,Integer> func) throws FestivalConnectionException {
        Set<Act> glastoData = efestivalSender.getFestivalData(festival, year);
        Map<String, Artist> lastFmMap = artistMapGenerator.generateLastFmMap(glastoData,artists);

        return glastoData.stream().filter(g -> lastFmMap.containsKey(g.getName().toLowerCase()))
                .sorted((x, y) -> Integer.compare(func.apply(lastFmMap.get(x.getName().toLowerCase())),
                        func.apply(lastFmMap.get(y.getName().toLowerCase()))))
                .collect(toList());

    }

}
