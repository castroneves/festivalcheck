package intersection;

import cache.CheckerCache;
import clashfinder.ClashfinderSender;
import clashfinder.Event;
import exception.FestivalConnectionException;
import glasto.GlastoRequestSender;
import lastfm.LastFmSender;
import pojo.Act;
import pojo.Artist;
import pojo.Response;
import spotify.AccessToken;
import spotify.SpotifyArtist;
import spotify.SpotifySender;
import spotify.SpotifyTracksResponse;
import strategy.PreferenceStrategy;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cache.CacheKeyPrefix.LISTENED;
import static cache.CacheKeyPrefix.RECCOMENDED;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * Created by Adam on 27/04/2015.
 */
public class IntersectionFinder {

    private GlastoRequestSender efestivalSender;
    private LastFmSender lastFmSender;
    private SpotifySender spotifySender;
    private ClashfinderSender clashFinderSender;
    private CheckerCache cache;
    private ArtistMapGenerator artistMapGenerator;


    public IntersectionFinder(GlastoRequestSender efestivalSender, LastFmSender lastFmSender, CheckerCache cache, ClashfinderSender clashFinderSender, SpotifySender spotifySender, ArtistMapGenerator artistMapGenerator) {
        this.efestivalSender = efestivalSender;
        this.lastFmSender = lastFmSender;
        this.cache = cache;
        this.clashFinderSender = clashFinderSender;
        this.spotifySender = spotifySender;
        this.artistMapGenerator = artistMapGenerator;
    }

    public List<Act> findIntersection(String username, String festival,String year) throws FestivalConnectionException {
        //cache this
        Set<Act> glastoData = efestivalSender.getFestivalData(festival, year);
        Response lastFmData = cache.getOrLookupLastFm(username, () -> lastFmSender.simpleRequest(username), LISTENED);
        List<Artist> artists = lastFmData.getTopartists().getArtist();

        Map<String, Artist> lastFmMap = artistMapGenerator.generateLastFmMap(glastoData,artists);

        return glastoData.stream().filter(g -> lastFmMap.containsKey(g.getName().toLowerCase()))
                .sorted((x, y) -> Integer.compare(lastFmMap.get(x.getName().toLowerCase()).getRankValue(), lastFmMap.get(y.getName().toLowerCase()).getRankValue()))
                .collect(toList());
    }

    public List<Act> findRIntersection(String token, String festival, String year) throws FestivalConnectionException {
        Set<Act> glastoData = efestivalSender.getFestivalData(festival, year);
        Response lastFmResponse = cache.getOrLookupLastFm(token, () -> lastFmSender.recommendedRequest(token), RECCOMENDED);
        List<Artist> artists = lastFmResponse.getRecommendations().getArtist();
        Map<String, Artist> lastFmMap = artists.stream().collect(toMap(a -> a.getName().toLowerCase(), Function.identity()));
        Map<String, Artist> additionalMap = artistMapGenerator.generateLikeMap(artists, glastoData);
        lastFmMap.putAll(additionalMap);

        return glastoData.stream().filter(g -> lastFmMap.containsKey(g.getName().toLowerCase()))
                .collect(toList());
    }

    public List<Event> findSIntersection(String username, String festival, String year) {
        Set<Event> clashfinderData = clashFinderSender.fetchData(festival,year);
        Response response = cache.getOrLookupLastFm(username, () -> lastFmSender.simpleRequest(username), LISTENED);
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
        Response response = cache.getOrLookupLastFm(token, () -> lastFmSender.recommendedRequest(token), RECCOMENDED);
        List<Artist> artists = response.getRecommendations().getArtist();
        Map<String, Artist> lastFmMap = artistMapGenerator.generateLastFmMap(clashfinderData, artists);

        return clashfinderData.stream().filter(g -> lastFmMap.containsKey(g.getName().toLowerCase()))
                .map(e -> new Event(e, 0, lastFmMap.get(e.getName().toLowerCase()).getRankValue()))
                .sorted((x, y) -> Integer.compare(x.getReccorank(), y.getReccorank()))
                .collect(toList());

    }

    public List<Event> findHybridScheduleIntersection(String token, String festival, String year, PreferenceStrategy strategy) {
        Set<Event> clashfinderData = clashFinderSender.fetchData(festival, year);
        Response response = cache.getOrLookupLastFm(token, () -> lastFmSender.recommendedRequest(token), RECCOMENDED);
        Response listened = cache.getOrLookupLastFm(response.getSession().getName(), () -> lastFmSender.simpleRequest(response.getSession().getName()), LISTENED);

        Map<String, Artist> reccoArtists = artistMapGenerator.generateLastFmMap(clashfinderData, response.getRecommendations().getArtist());
        Map<String, Artist> listenedArtists = artistMapGenerator.generateLastFmMap(clashfinderData, listened.getTopartists().getArtist());

        return strategy.findOrderedInterection(clashfinderData, listenedArtists, reccoArtists);
    }

    public List<Event> findSpotifyScheduleIntersection(String authCode, String festival, String year) {
        AccessToken token = cache.getOrFetchSpotifyToken(authCode, () -> spotifySender.getAuthToken(authCode));
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
