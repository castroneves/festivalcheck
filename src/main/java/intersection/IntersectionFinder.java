package intersection;

import cache.CheckerCache;
import clashfinder.ClashfinderSender;
import clashfinder.domain.Event;
import com.google.inject.Inject;
import exception.FestivalConnectionException;
import glasto.GlastoRequestSender;
import lastfm.LastFmSender;
import pojo.Act;
import pojo.Artist;
import pojo.Response;
import spotify.domain.AccessToken;
import spotify.domain.SpotifyArtist;
import spotify.SpotifySender;
import spotify.domain.SpotifyTracksResponse;
import strategy.PreferenceStrategy;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cache.CacheKeyPrefix.LISTENED;
import static cache.CacheKeyPrefix.RECCOMENDED;
import static cache.CacheKeyPrefix.SPOTIFYACCESSTOKEN;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * Created by Adam on 27/04/2015.
 */
public class IntersectionFinder {

    @Inject
    private GlastoRequestSender efestivalSender;
    @Inject
    private LastFmSender lastFmSender;
    @Inject
    private SpotifySender spotifySender;
    @Inject
    private ClashfinderSender clashFinderSender;
    @Inject
    private CheckerCache cache;
    @Inject
    private ArtistMapGenerator artistMapGenerator;

    public List<Act> findIntersection(String username, String festival,String year) throws FestivalConnectionException {
        //cache this
        Set<Act> glastoData = efestivalSender.getFestivalData(festival, year);
        Response lastFmData = cache.getOrLookup(username, () -> lastFmSender.simpleRequest(username), LISTENED, Response.class);
        List<Artist> artists = lastFmData.getTopartists().getArtist();

        Map<String, Artist> lastFmMap = artistMapGenerator.generateLastFmMap(glastoData,artists);

        return glastoData.stream().filter(g -> lastFmMap.containsKey(g.getName().toLowerCase()))
                .sorted((x, y) -> Integer.compare(lastFmMap.get(x.getName().toLowerCase()).getRankValue(), lastFmMap.get(y.getName().toLowerCase()).getRankValue()))
                .collect(toList());
    }

    public List<Act> findRIntersection(String token, String festival, String year) throws FestivalConnectionException {
        Set<Act> glastoData = efestivalSender.getFestivalData(festival, year);
        Response lastFmResponse = cache.getOrLookup(token, () -> lastFmSender.recommendedRequest(token), RECCOMENDED, Response.class);
        List<Artist> artists = lastFmResponse.getRecommendations().getArtist();
        Map<String, Artist> lastFmMap = artists.stream().collect(toMap(a -> a.getName().toLowerCase(), Function.identity()));
        Map<String, Artist> additionalMap = artistMapGenerator.generateLikeMap(artists, glastoData);
        lastFmMap.putAll(additionalMap);

        return glastoData.stream().filter(g -> lastFmMap.containsKey(g.getName().toLowerCase()))
                .collect(toList());
    }

    public List<Event> findSIntersection(String username, String festival, String year) {
        Set<Event> clashfinderData = clashFinderSender.fetchData(festival,year);
        Response response = cache.getOrLookup(username, () -> lastFmSender.simpleRequest(username), LISTENED, Response.class);
        List<Artist> artists = response.getTopartists().getArtist();
        Map<String, Artist> lastFmMap = artistMapGenerator.generateLastFmMap(clashfinderData, artists);

        return matchingEventsByPlays(clashfinderData, lastFmMap);

    }

    private List<Event> matchingEventsByPlays(Set<Event> clashfinderData, Map<String, Artist> lastFmMap) {
        return clashfinderData.stream().filter(g -> lastFmMap.containsKey(g.getName().toLowerCase()))
                .map(e -> new Event(e, Integer.parseInt(lastFmMap.get(e.getName().toLowerCase()).getPlaycount())))
                .sorted((x, y) -> Integer.compare(y.getScrobs(), x.getScrobs()))
                .collect(toList());
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
        AccessToken token = cache.getOrLookup(authCode, () -> spotifySender.getAuthToken(authCode), SPOTIFYACCESSTOKEN, AccessToken.class);
        System.out.println(token.getAccessToken());
        SpotifyTracksResponse savedTracks = spotifySender.getSavedTracks(token.getAccessToken());
        List<SpotifyArtist> artists = savedTracks.getItems().stream().flatMap(x -> x.getTrack().getArtists().stream()).collect(toList());

        //Get playlist info here
        List<Artist> result = artistListByFrequency(artists);
        Set<Event> clashfinderData = clashFinderSender.fetchData(festival, year);
        Map<String,Artist> artistMap = artistMapGenerator.generateLastFmMap(clashfinderData, result);
        System.out.println(artistMap);
        System.out.println(result);
        return matchingEventsByPlays(clashfinderData,artistMap);
    }

    private List<Artist> artistListByFrequency(List<SpotifyArtist> collect) {
        Map<String, Integer> frequencyMap = collect.stream().map(x -> x.toString()).collect(Collectors.toConcurrentMap(w -> w, w -> 1, Integer::sum));
        return frequencyMap.keySet().stream().sorted((x, y) -> frequencyMap.get(y).compareTo(frequencyMap.get(x)))
                .map(x -> new Artist(x, frequencyMap.get(x).toString(), 0)).collect(toList());
    }


}
