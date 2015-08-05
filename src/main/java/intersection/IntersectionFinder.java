package intersection;

import cache.CheckerCache;
import clashfinder.ClashfinderSender;
import clashfinder.Event;
import exception.FestivalConnectionException;
import glasto.GlastoRequestSender;
import glasto.GlastoResponseParser;
import lastfm.LastFmSender;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import pojo.*;
import service.JedisConfig;
import service.LastFmConfig;
import strategy.ListenedFirstPreferenceStrategy;
import strategy.PreferenceStrategy;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * Created by Adam on 27/04/2015.
 */
public class IntersectionFinder {

    private GlastoRequestSender sender;
    private GlastoResponseParser parser;
    private LastFmSender lastFmSender;
    private ClashfinderSender clashFinderSender;
    private CheckerCache cache;

    public IntersectionFinder(GlastoRequestSender sender, GlastoResponseParser parser, LastFmSender lastFmSender, CheckerCache cache, ClashfinderSender clashFinderSender) {
        this.sender = sender;
        this.parser = parser;
        this.lastFmSender = lastFmSender;
        this.cache = cache;
        this.clashFinderSender = clashFinderSender;
    }

    public List<Act> findIntersection(String username, String festival,String year) throws FestivalConnectionException {
        //cache this
        Set<Act> glastoData = getFestivalData(festival, year);
        TopArtists lastFmData = cache.getOrLookupLastFm(username, () -> lastFmSender.simpleRequest(username));
        List<Artist> artists = lastFmData.getArtist();

        Map<String,Artist> lastFmMap = artists.stream().collect(toMap(a -> a.getName().toLowerCase(), Function.identity()));
        Map<String,Artist> additionalMap = generateLikeMap(artists, glastoData);
        lastFmMap.putAll(additionalMap);

        return glastoData.stream().filter(g -> lastFmMap.containsKey(g.getName().toLowerCase()))
                .sorted((x, y) -> Integer.compare(lastFmMap.get(x.getName().toLowerCase()).getRankValue(), lastFmMap.get(y.getName().toLowerCase()).getRankValue()))
                .collect(toList());
    }

    public List<Act> findRIntersection(String token, String festival, String year) throws FestivalConnectionException {
        Set<Act> glastoData = getFestivalData(festival, year);
        Response lastFmResponse = cache.getOrLookupRecLastFm(token, () -> lastFmSender.recommendedRequest(token));
        List<Artist> artists = lastFmResponse.getRecommendations().getArtist();
        Map<String,Artist> lastFmMap = artists.stream().collect(toMap(a -> a.getName().toLowerCase(), Function.identity()));
        Map<String,Artist> additionalMap = generateLikeMap(artists, glastoData);
        lastFmMap.putAll(additionalMap);

        return glastoData.stream().filter(g -> lastFmMap.containsKey(g.getName().toLowerCase()))
                .collect(toList());
    }

    public List<Event> findSIntersection(String username, String festival, String year) {
        Set<Event> clashfinderData = clashFinderSender.fetchData(festival,year);
        TopArtists response = cache.getOrLookupLastFm(username, () -> lastFmSender.simpleRequest(username));
        List<Artist> artists = response.getArtist();
        Map<String, Artist> lastFmMap = generateLastFmMap(clashfinderData, artists);

        return clashfinderData.stream().filter(g -> lastFmMap.containsKey(g.getName().toLowerCase()))
                .map(e -> new Event(e, Integer.parseInt(lastFmMap.get(e.getName().toLowerCase()).getPlaycount())))
                .sorted((x, y) -> Integer.compare(y.getScrobs(), x.getScrobs()))
                .collect(toList());

    }

    public List<Event> findReccoScheduleIntersection(String token, String festival, String year) {
        Set<Event> clashfinderData = clashFinderSender.fetchData(festival,year);
        Response response = cache.getOrLookupRecLastFm(token, () -> lastFmSender.recommendedRequest(token));
        List<Artist> artists = response.getRecommendations().getArtist();
        Map<String, Artist> lastFmMap = generateLastFmMap(clashfinderData, artists);

        return clashfinderData.stream().filter(g -> lastFmMap.containsKey(g.getName().toLowerCase()))
                .map(e -> new Event(e, 0, lastFmMap.get(e.getName().toLowerCase()).getRankValue()))
                .sorted((x, y) -> Integer.compare(x.getReccorank(), y.getReccorank()))
                .collect(toList());

    }

    public List<Event> findHybridScheduleIntersection(String token, String festival, String year, PreferenceStrategy strategy) {
        Set<Event> clashfinderData = clashFinderSender.fetchData(festival, year);
        Response response = cache.getOrLookupRecLastFm(token, () -> lastFmSender.recommendedRequest(token));
        TopArtists listened = cache.getOrLookupLastFm(response.getSession().getName(), () -> lastFmSender.simpleRequest(response.getSession().getName()));

        Map<String, Artist> reccoArtists = generateLastFmMap(clashfinderData, response.getRecommendations().getArtist());
        Map<String, Artist> listenedArtists = generateLastFmMap(clashfinderData, listened.getArtist());

        return strategy.findOrderedInterection(clashfinderData, listenedArtists, reccoArtists);
//        return clashfinderData.stream().filter(g -> lastFmMap.containsKey(g.getName().toLowerCase()))
//                .map(e -> new Event(e, Integer.parseInt(lastFmMap.get(e.getName().toLowerCase()).getPlaycount())))
//                .sorted((x, y) -> Integer.compare(y.getScrobs(), x.getScrobs()))
//                .collect(toList());

    }

    private Map<String, Artist> generateLastFmMap(Set<Event> clashfinderData, List<Artist> agg) {
        Map<String,Artist> lastFmMap = agg.stream().collect(toMap(a -> a.getName().toLowerCase(), Function.identity()));
        Map<String,Artist> additionalMap = generateLikeMapC(agg, clashfinderData);
        lastFmMap.putAll(additionalMap);
        Map<String,Artist> variants = generateArtistVariantMap(agg,clashfinderData);
        lastFmMap.putAll(variants);
        return lastFmMap;
    }


    private Set<Act> getFestivalData(String festival, String year) throws FestivalConnectionException {
        String rawGlastoData = sender.getRawResponse(festival,year);
        return new HashSet<>(parser.parseRawResponse(rawGlastoData));
    }

    private Map<String, Artist> generateLikeMap(List<Artist> artists, Set<Act> glastoData) {
        Set<Artist> shortArtists = new HashSet<>(fetchShortArtists(artists,glastoData));
        return shortArtists.stream().collect(toMap(a -> a.getName().toLowerCase(), Function.identity()));
    }

    private Map<String, Artist> generateLikeMapC(List<Artist> artists, Set<Event> glastoData) {
        Set<Artist> shortArtists = new HashSet<>(fetchShortArtistsC(artists, glastoData));
        return shortArtists.stream().collect(toMap(a -> a.getName().toLowerCase(), Function.identity()));
    }

    private Map<String,Artist> generateArtistVariantMap(List<Artist> artists, Set<Event> glastoData) {
        Set<Artist> shortArtists = new HashSet<>(fetchVariantArtistsC(artists, glastoData));
        return shortArtists.stream().collect(toMap(a -> a.getName().toLowerCase(), Function.identity()));
    }

    private List<Artist> fetchShortArtists(List<Artist> artists, Set<Act> glastoData) {

        List<Artist> collect = artists.stream().filter(a -> a.getName().contains(" ") && glastoData.stream().anyMatch(g -> g.getName().toLowerCase().contains(a.getName().toLowerCase()))).collect(toList());
        return collect.stream().map(a -> {
            String adjustedName = glastoData.stream()
                    .filter(g -> g.getName().toLowerCase().contains(a.getName().toLowerCase())).findFirst().get().getName();
            return new Artist(adjustedName,a.getPlaycount(),a.getRankValue());
        }).collect(toList());

    }

    private List<Artist> fetchShortArtistsC(List<Artist> artists, Set<Event> glastoData) {

        List<Artist> collect = artists.stream().filter(a -> a.getName().contains(" ") && glastoData.stream().anyMatch(g -> g.getName().toLowerCase().contains(a.getName().toLowerCase() + " "))).collect(toList());
        return collect.stream().map(a -> {
            String adjustedName = glastoData.stream()
                    .filter(g -> g.getName().toLowerCase().contains(a.getName().toLowerCase() + " ")).findFirst().get().getName();
            return new Artist(adjustedName,a.getPlaycount(),a.getRankValue());
        }).collect(toList());
    }

    private List<Artist> fetchVariantArtistsC(List<Artist> artists, Set<Event> glastoData) {
        List<Artist> andToAmp = artists.stream()
                .filter(a -> a.getName().contains(" "))
                .filter(a -> a.getName().toLowerCase().contains(" and "))
                .map(a -> new Artist(a.getName().replaceAll("(?i) and ", " & "),a.getPlaycount(),a.getRankValue()))
                .filter(a -> glastoData.stream().anyMatch(g -> g.getName().toLowerCase().contains(a.getName().toLowerCase())))
                .collect(toList());
        List<Artist> ampToAnd = artists.stream()
                .filter(a -> a.getName().contains(" "))
                .filter(a -> a.getName().toLowerCase().contains(" & "))
                .map(a -> new Artist(a.getName().replaceAll("(?i) & ", " and "),a.getPlaycount(),a.getRankValue()))
                .filter(a -> glastoData.stream().anyMatch(g -> g.getName().toLowerCase().contains(a.getName().toLowerCase())))
                .collect(toList());
        List<Artist> collect = Stream.concat(andToAmp.stream(), ampToAnd.stream()).collect(toList());
        return collect.stream().map(a -> {
            String adjustedName = glastoData.stream()
                    .filter(g -> g.getName().toLowerCase().contains(a.getName().toLowerCase())).findFirst().get().getName();
            return new Artist(adjustedName,a.getPlaycount(),a.getRankValue());
        }).collect(toList());


    }

    public static void main(String[] args) throws FestivalConnectionException {
        LastFmConfig config = new LastFmConfig();
        config.setApiKey("0ba3650498bb88d7328c97b461fc3636");
        config.setSecret("15d49ba610f2c6ec4e884dacec4e4021");
        JedisConfig jedis = new JedisConfig();
        jedis.setHost("glasto.redis.cache.windows.net");
        jedis.setPort(6379);
        jedis.setPassword("68v5ZsW+S6+YUE1A+S/k6plja2oS/PU4JJGLvtlXEJE=");
        IntersectionFinder finder = new IntersectionFinder(
                new GlastoRequestSender(),
                new GlastoResponseParser(),
                new LastFmSender(config), new CheckerCache(jedis), new ClashfinderSender());
//        List<Act> acts = finder.findRIntersection("0a88c1504ae95faaa2a96053a42bbeec", "glastonbury", "2015");
        List<Event> events = finder.findHybridScheduleIntersection("5056ea99986842056f5e333162a7a39c", "g", "2015", new ListenedFirstPreferenceStrategy());
        events.stream().forEach(System.out::println);
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");
        events.stream().map(x -> formatter.print(x.getStart())).forEach(System.out::println);
    }
}
