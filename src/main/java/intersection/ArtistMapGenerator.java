package intersection;

import com.google.common.base.Splitter;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.inject.Singleton;
import lastfm.domain.Artist;
import domain.Show;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * Created by Adam on 12/09/2015.
 */
@Singleton
public class ArtistMapGenerator {

    private BiMap<String, String> aliases = HashBiMap.create();
    private List<String> acceptedChars = new ArrayList<>();


    public ArtistMapGenerator() {
        aliases.put("omd", "orchestral Manoeuvres in the dark");
        aliases.put("orchestral manoeuvres in the dark", "o.m.d.");
        aliases.put("o.m.d.", "omd");
        aliases.put("elo", "electric light orchestra");
        aliases.put("electric light orchestra", "e.l.o.");
        aliases.put("e.l.o", "elo");

        acceptedChars.add(",");
        acceptedChars.add(" ");
    }

    public Map<String, Artist> generateLastFmMap(Set<? extends Show> clashfinderData, List<Artist> artists) {
        List<Artist> artistList = new ArrayList<>(artists);

        Map<String, Artist> lastFmMap = artists.stream().collect(toMap(a -> a.getName().toLowerCase(), Function.identity()));
        List<Artist> knownAliases = fetchKnownAliases(artists);
        artistList.addAll(knownAliases);
        lastFmMap.putAll(generateArtistVariantMap(knownAliases));
        List<Artist> variantArtists = fetchVariantArtists(artists);
        artistList.addAll(variantArtists);
        Map<String, Artist> variants = generateArtistVariantMap(variantArtists);
        lastFmMap.putAll(variants);
        Map<String, Artist> additionalMap = generatePartialMatchMap(artistList, clashfinderData);
        lastFmMap.putAll(additionalMap);
        return lastFmMap;
    }

    private List<Artist> fetchKnownAliases(List<Artist> artists) {
        List<Artist> aliasedForward = artists.stream().filter(x -> aliases.containsKey(x.getName().toLowerCase()))
                .map(x -> new Artist(aliases.get(x.getName().toLowerCase()), x.getPlaycount(), x.getRankValue()))
                .collect(toList());
        List<Artist> aliasedBack = artists.stream().filter(x -> aliases.inverse().containsKey(x.getName().toLowerCase()))
                .map(x -> new Artist(aliases.inverse().get(x.getName().toLowerCase()), x.getPlaycount(), x.getRankValue()))
                .collect(toList());

        return Stream.concat(aliasedForward.stream(), aliasedBack.stream()).collect(toList());
    }

    public Map<String, Artist> generatePartialMatchMap(List<Artist> artists, Set<? extends Show> glastoData) {
        List<Artist> artists1 = fetchPartialMatches(artists, glastoData);
        // To bypass case inconsistancy in Clashfinder data
        Set<Artist> shortArtists = artists1.stream().map(a -> new Artist(a.getName().toLowerCase(), a.getPlaycount(), a.getRankValue(), a.getMatch())).collect(Collectors.toSet());
        return shortArtists.stream().collect(toMap(a -> a.getName().toLowerCase(), Function.identity()));
    }

    private Map<String, Artist> generateArtistVariantMap(List<Artist> variantArtists) {
        return variantArtists.stream().collect(toMap(a -> a.getName().toLowerCase(), Function.identity()));
    }

    // Possibly flag match as partial?
    private List<Artist> fetchPartialMatches(List<Artist> artists, Set<? extends Show> glastoData) {

        List<Artist> matches = artists.stream()
                .filter(a -> (a.getName().contains(" ") &&
                        glastoData.stream().anyMatch(
                                g -> containsMatch(a, g)
                                        || isBandMatch(a.getName(), g.getName())
                        )) || glastoData.stream().anyMatch(g -> isTLABandMatch(a.getName(), g.getName())))
                .collect(toList());
        return matches.stream().map(a ->
                glastoData.stream()
                        .filter(g -> containsMatch(a,g) || isBandMatch(a.getName(), g.getName()) || isTLABandMatch(a.getName(), g.getName()))
                        .map(n -> new Artist(n.getName(), a.getPlaycount(), a.getRankValue(), a.getName()))
        ).flatMap(s -> s).collect(toList());
    }

    private boolean containsMatch(Artist a, Show g) {
        return acceptedChars.stream().anyMatch(
                x -> acceptedChars.stream().anyMatch(
                        y -> g.getName().toLowerCase().contains(x + a.getName().toLowerCase() + y)))
                || acceptedChars.stream().anyMatch(x -> g.getName().toLowerCase().startsWith(a.getName().toLowerCase() + x))
                || acceptedChars.stream().anyMatch(x -> g.getName().toLowerCase().endsWith(x + a.getName().toLowerCase()));
    }

    private boolean isTLABandMatch(String listened, String showArtist) {
        if (listened.length() == 3) {
            return showArtist.contains(listened.toUpperCase());
        }
        return false;
    }

    private boolean isBandMatch(String listened, String showArtist) {
        if (listened.contains("&")) {
            if (showArtist.contains(" and ") || showArtist.contains(" & ")) {
                Splitter splitter = Splitter.on("&").omitEmptyStrings();
                List<String> entries = splitter.splitToList(listened);
                return entries.stream().allMatch(x -> showArtist.contains(x));
            }
        }
        return false;
    }

    private List<Artist> fetchVariantArtists(List<Artist> artists) {
        List<Artist> andToAmp = artists.stream()
                .filter(a -> a.getName().contains(" "))
                .filter(a -> a.getName().toLowerCase().contains(" and "))
                .map(a -> new Artist(a.getName().replaceAll("(?i) and ", " & "), a.getPlaycount(), a.getRankValue()))
                .collect(toList());
        List<Artist> ampToAnd = artists.stream()
                .filter(a -> a.getName().contains(" "))
                .filter(a -> a.getName().toLowerCase().contains(" & "))
                .map(a -> new Artist(a.getName().replaceAll("(?i) & ", " and "), a.getPlaycount(), a.getRankValue()))
                .collect(toList());
        return Stream.concat(andToAmp.stream(), ampToAnd.stream()).collect(toList());
    }
}
