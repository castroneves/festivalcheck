package intersection;

import cache.CheckerCache;
import clashfinder.ClashfinderSender;
import clashfinder.domain.ClashfinderData;
import clashfinder.domain.Event;
import com.google.inject.Inject;
import lastfm.LastFmSender;
import lastfm.domain.Artist;
import lastfm.domain.Recommendations;
import lastfm.domain.Response;
import spotify.SpotifyDataGrabber;
import strategy.PreferenceStrategy;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static cache.CacheKeyPrefix.CLASHFINDER;
import static cache.CacheKeyPrefix.LISTENED;
import static cache.CacheKeyPrefix.RECCOMENDED;
import static java.util.stream.Collectors.toList;

/**
 * Created by Adam on 24/09/2015.
 */
public class ScheduleIntersectionFinder {

    @Inject
    private LastFmSender lastFmSender;
    @Inject
    private CheckerCache cache;
    @Inject
    private ArtistMapGenerator artistMapGenerator;
    @Inject
    private ClashfinderSender clashFinderSender;
    @Inject
    private SpotifyDataGrabber spotifyDataGrabber;
    @Inject
    private RecommendedArtistGenerator recommendedArtistGenerator;


    public List<Event> findSIntersection(String username, String festival, String year) {
        ClashfinderData clashfinderData =
                cache.getOrLookup(festival + year, () -> clashFinderSender.fetchData(festival,year),CLASHFINDER,ClashfinderData.class);
        Response response = cache.getOrLookup(username, () -> lastFmSender.simpleRequest(username), LISTENED, Response.class);
        List<Artist> artists = response.getTopartists().getArtist();

        return matchingEventsByPlays(clashfinderData.getEvents(), artists);
    }

    public List<Event> findLfmReccoScheduleIntersection(String token, String festival, String year) {
        ClashfinderData clashfinderData =
                cache.getOrLookup(festival + year, () -> clashFinderSender.fetchData(festival,year),CLASHFINDER,ClashfinderData.class);
        Response response = cache.getOrLookup(token, () -> lastFmSender.recommendedRequest(token), RECCOMENDED, Response.class);
        List<Artist> artists = response.getRecommendations().getArtist();
        return matchingEventsByRank(clashfinderData.getEvents(), artists);
    }

    public List<Event> findReccoScheduleIntersection(String username, String festival, String year) {
        long l = System.currentTimeMillis();
        ClashfinderData clashfinderData =
                cache.getOrLookup(festival + year, () -> clashFinderSender.fetchData(festival,year),CLASHFINDER,ClashfinderData.class);
        System.out.println("clashfinder retrieve time " + (System.currentTimeMillis() - l));
        Response response = cache.getOrLookup(username, () -> lastFmSender.simpleRequest(username), LISTENED, Response.class);
        List<Artist> artists = response.getTopartists().getArtist();
        Recommendations recArtists = cache.getOrLookup(username, () -> recommendedArtistGenerator.fetchRecommendations(artists), RECCOMENDED, Recommendations.class);
        System.out.println("All data in hand " + (System.currentTimeMillis() - l));
        return matchingEventsByRank(clashfinderData.getEvents(), recArtists.getArtist());
    }

    public List<Event> findLfmHybridScheduleIntersection(String token, String festival, String year, PreferenceStrategy strategy) {
        ClashfinderData clashfinderData =
                cache.getOrLookup(festival + year, () -> clashFinderSender.fetchData(festival,year),CLASHFINDER,ClashfinderData.class);
        Response response = cache.getOrLookup(token, () -> lastFmSender.recommendedRequest(token), RECCOMENDED, Response.class);
        Response listened = cache.getOrLookup(response.getSession().getName(), () -> lastFmSender.simpleRequest(response.getSession().getName()), LISTENED, Response.class);

        Map<String, Artist> reccoArtists = artistMapGenerator.generateLastFmMap(clashfinderData.getEvents(), response.getRecommendations().getArtist());
        Map<String, Artist> listenedArtists = artistMapGenerator.generateLastFmMap(clashfinderData.getEvents(), listened.getTopartists().getArtist());

        return strategy.findOrderedInterection(clashfinderData.getEvents(), listenedArtists, reccoArtists);
    }

    public List<Event> findHybridScheduleIntersection(String username, String festival, String year, PreferenceStrategy strategy) {
        ClashfinderData clashfinderData =
                cache.getOrLookup(festival + year, () -> clashFinderSender.fetchData(festival,year),CLASHFINDER,ClashfinderData.class);
        Response response = cache.getOrLookup(username, () -> lastFmSender.simpleRequest(username), LISTENED, Response.class);
        List<Artist> artists = response.getTopartists().getArtist();
        Recommendations recArtists = cache.getOrLookup(username, () -> recommendedArtistGenerator.fetchRecommendations(artists), RECCOMENDED, Recommendations.class);

        Map<String, Artist> reccoArtists = artistMapGenerator.generateLastFmMap(clashfinderData.getEvents(), recArtists.getArtist());
        Map<String, Artist> listenedArtists = artistMapGenerator.generateLastFmMap(clashfinderData.getEvents(), artists);

        return strategy.findOrderedInterection(clashfinderData.getEvents(), listenedArtists, reccoArtists);
    }

    public List<Event> findSpotifyScheduleIntersection(String authCode, String festival, String year, String redirectUrl) {
        List<Artist> result = spotifyDataGrabber.fetchSpotifyArtists(authCode, redirectUrl);
        ClashfinderData clashfinderData =
                cache.getOrLookup(festival + year, () -> clashFinderSender.fetchData(festival,year),CLASHFINDER,ClashfinderData.class);
        return matchingEventsByPlays(clashfinderData.getEvents(),result);
    }

    private List<Event> matchingEventsByPlays(Set<Event> clashfinderData, List<Artist> artists) {
        Map<String,Artist> lastFmMap = artistMapGenerator.generateLastFmMap(clashfinderData, artists);
        return clashfinderData.stream().filter(g -> lastFmMap.containsKey(g.getName().toLowerCase()))
                .map(e -> new Event(e, Integer.parseInt(lastFmMap.get(e.getName().toLowerCase()).getPlaycount())))
                .sorted((x, y) -> Integer.compare(y.getScrobs(), x.getScrobs()))
                .collect(toList());
    }

    private List<Event> matchingEventsByRank(Set<Event> clashfinderData, List<Artist> artists) {
        long l = System.currentTimeMillis();
        Map<String, Artist> lastFmMap = artistMapGenerator.generateLastFmMap(clashfinderData, artists);
        System.out.println("Data generation time " + (System.currentTimeMillis() - l));

        return clashfinderData.stream().filter(g -> lastFmMap.containsKey(g.getName().toLowerCase()))
                .map(e -> new Event(e, 0, lastFmMap.get(e.getName().toLowerCase()).getRankValue()))
                .sorted((x, y) -> Integer.compare(x.getReccorank(), y.getReccorank()))
                .collect(toList());
    }
}
