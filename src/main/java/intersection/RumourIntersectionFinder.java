package intersection;

import cache.CheckerCache;
import com.google.inject.Inject;
import domain.RumourResponse;
import efestivals.GlastoRequestSender;
import efestivals.domain.Act;
import exception.FestivalConnectionException;
import lastfm.LastFmSender;
import lastfm.domain.Artist;
import lastfm.domain.Recommendations;
import lastfm.domain.Response;
import spotify.SpotifyDataGrabber;
import spotify.domain.SpotifyArtists;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static cache.CacheKeyPrefix.*;
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
    @Inject
    private RecommendedArtistGenerator recommendedArtistGenerator;

    public RumourResponse findIntersection(String username, String festival, String year) throws FestivalConnectionException {
        Response lastFmData = cache.getOrLookup(username, () -> lastFmSender.simpleRequest(username), LISTENED, Response.class);
        List<Artist> artists = lastFmData.getTopartists().getArtist();
        List<Act> acts = computeIntersection(artists, festival, year, Artist::getRankValue);
        return new RumourResponse(acts);
    }

    public List<Act> findRecommendedIntersection(String username, String festival, String year) {
        Response lastFmData = cache.getOrLookup(username, () -> lastFmSender.simpleRequest(username), LISTENED, Response.class);
        List<Artist> artists = lastFmData.getTopartists().getArtist();
        Recommendations recArtists = cache.getOrLookup(username, () -> recommendedArtistGenerator.fetchRecommendations(artists), RECCOMENDED, Recommendations.class);
        return computeIntersection(recArtists.getArtist(), festival, year, Artist::getRankValue);
    }

    public List<Act> findSpotifyIntersection(String authCode, String festival, String year, String redirectUrl) throws FestivalConnectionException {
        SpotifyArtists artists = cache.getOrLookup(authCode, () -> spotifyDataGrabber.fetchSpotifyArtists(authCode, redirectUrl), SPOTIFYARTISTS, SpotifyArtists.class);
        return computeIntersection(artists.getArtists(),festival,year,x -> -1 * x.getPlaycountInt());
    }

    public List<Act> findSpotifyRecommendedIntersection(String authCode, String festival, String year, String redirectUrl) {
        SpotifyArtists artists = cache.getOrLookup(authCode, () -> spotifyDataGrabber.fetchSpotifyArtists(authCode, redirectUrl), SPOTIFYARTISTS, SpotifyArtists.class);
        Recommendations recArtists = cache.getOrLookup(authCode, () -> recommendedArtistGenerator.fetchRecommendations(artists.getArtists()), RECCOMENDED, Recommendations.class);
        return computeIntersection(recArtists.getArtist(), festival, year, Artist::getRankValue);
    }

    public List<Act> findLastFmRecommendedIntersectionLegacy(String token, String festival, String year) throws FestivalConnectionException {
        Response lastFmResponse = cache.getOrLookup(token, () -> lastFmSender.recommendedRequest(token), RECCOMENDED, Response.class);
        List<Artist> artists = lastFmResponse.getRecommendations().getArtist();
        return computeIntersection(artists, festival, year, Artist::getRankValue);
    }

    private List<Act> computeIntersection(List<Artist> artists, String festival, String year, Function<Artist,Integer> func) throws FestivalConnectionException {
        Set<Act> glastoData = efestivalSender.getFestivalData(festival, year);
        Map<String, Artist> lastFmMap = artistMapGenerator.generateLastFmMap(glastoData, artists).getArtistMap();

        return glastoData.stream().filter(g -> lastFmMap.containsKey(g.getName().toLowerCase()))
                .map(g -> new Act(g,lastFmMap.get(g.getName().toLowerCase()).getPlaycount(),lastFmMap.get(g.getName().toLowerCase()).getRankValue()))
                .map(g -> new Act(g,lastFmMap.get(g.getName().toLowerCase()).getMatch()))
                .sorted((x, y) -> Integer.compare(func.apply(lastFmMap.get(x.getName().toLowerCase())),
                        func.apply(lastFmMap.get(y.getName().toLowerCase()))))
                .collect(toList());

    }
}
