package intersection;

import cache.CheckerCache;
import clashfinder.ClashfinderSender;
import clashfinder.domain.Event;
import com.google.inject.Inject;
import lastfm.LastFmSender;
import lastfm.domain.Artist;
import lastfm.domain.Response;
import spotify.SpotifyDataGrabber;
import strategy.PreferenceStrategy;

import java.util.List;
import java.util.Map;
import java.util.Set;

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


    public List<Event> findSIntersection(String username, String festival, String year) {
        Set<Event> clashfinderData = clashFinderSender.fetchData(festival,year);
        Response response = cache.getOrLookup(username, () -> lastFmSender.simpleRequest(username), LISTENED, Response.class);
        List<Artist> artists = response.getTopartists().getArtist();
        Map<String, Artist> lastFmMap = artistMapGenerator.generateLastFmMap(clashfinderData, artists);

        return matchingEventsByPlays(clashfinderData, lastFmMap);
    }

    public List<Event> findReccoScheduleIntersection(String token, String festival, String year) {
        Set<Event> clashfinderData = clashFinderSender.fetchData(festival,year);
        Response response = cache.getOrLookup(token, () -> lastFmSender.recommendedRequest(token), RECCOMENDED, Response.class);
        List<Artist> artists = response.getRecommendations().getArtist();
        Map<String, Artist> lastFmMap = artistMapGenerator.generateLastFmMap(clashfinderData, artists);

        return clashfinderData.stream().filter(g -> lastFmMap.containsKey(g.getName().toLowerCase()))
                .map(e -> new Event(e, 0, lastFmMap.get(e.getName().toLowerCase()).getRankValue()))
                .sorted((x, y) -> Integer.compare(x.getReccorank(), y.getReccorank()))
                .collect(toList());
    }

    public List<Event> findHybridScheduleIntersection(String token, String festival, String year, PreferenceStrategy strategy) {
        Set<Event> clashfinderData = clashFinderSender.fetchData(festival, year);
        Response response = cache.getOrLookup(token, () -> lastFmSender.recommendedRequest(token), RECCOMENDED, Response.class);
        Response listened = cache.getOrLookup(response.getSession().getName(), () -> lastFmSender.simpleRequest(response.getSession().getName()), LISTENED, Response.class);

        Map<String, Artist> reccoArtists = artistMapGenerator.generateLastFmMap(clashfinderData, response.getRecommendations().getArtist());
        Map<String, Artist> listenedArtists = artistMapGenerator.generateLastFmMap(clashfinderData, listened.getTopartists().getArtist());

        return strategy.findOrderedInterection(clashfinderData, listenedArtists, reccoArtists);
    }

    public List<Event> findSpotifyScheduleIntersection(String authCode, String festival, String year) {
        List<Artist> result = spotifyDataGrabber.fetchSpotifyArtists(authCode);
        Set<Event> clashfinderData = clashFinderSender.fetchData(festival, year);
        Map<String,Artist> artistMap = artistMapGenerator.generateLastFmMap(clashfinderData, result);
        System.out.println(artistMap);
        System.out.println(result);
        return matchingEventsByPlays(clashfinderData,artistMap);
    }

    private List<Event> matchingEventsByPlays(Set<Event> clashfinderData, Map<String, Artist> lastFmMap) {
        return clashfinderData.stream().filter(g -> lastFmMap.containsKey(g.getName().toLowerCase()))
                .map(e -> new Event(e, Integer.parseInt(lastFmMap.get(e.getName().toLowerCase()).getPlaycount())))
                .sorted((x, y) -> Integer.compare(y.getScrobs(), x.getScrobs()))
                .collect(toList());
    }
}
