package intersection;

import com.google.common.base.Splitter;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.inject.Singleton;
import domain.ArtistMap;
import domain.Show;
import lastfm.domain.Artist;

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

    public ArtistMapGenerator() {
        aliases.put("omd", "orchestral manoeuvres in the dark");
        aliases.put("orchestral manoeuvres in the dark", "o.m.d.");
        aliases.put("o.m.d.", "omd");
        aliases.put("elo", "electric light orchestra");
        aliases.put("electric light orchestra", "e.l.o.");
        aliases.put("e.l.o", "elo");
    }

    public ArtistMap generateLastFmMap(Set<? extends Show> festivalData, List<Artist> artists) {
        Set<Artist> artistList = new HashSet<>(artists);
        Map<String, Artist> lastFmMap = artists.stream().collect(toMap(a -> a.getName().toLowerCase(), Function.identity()));

        List<Artist> knownAliases = fetchKnownAliases(artists);
        artistList.addAll(knownAliases);
        lastFmMap.putAll(generateArtistVariantMap(knownAliases));

        List<Artist> variantArtists = fetchVariantArtists(artists);
        artistList.addAll(variantArtists);
        lastFmMap.putAll(generateArtistVariantMap(variantArtists));

        Map<String, Artist> additionalMap = generatePartialMatchMap(new ArrayList<>(artistList), festivalData);
        additionalMap.putAll(lastFmMap);
        return new ArtistMap(additionalMap);
    }

    private List<Artist> fetchKnownAliases(List<Artist> artists) {
        List<Artist> aliasedForward = artists.stream().filter(x -> aliases.containsKey(x.getName().toLowerCase()))
                .map(x -> new Artist(aliases.get(x.getName().toLowerCase()), x.getPlaycount(), x.getRankValue()))
                .collect(toList());
        List<Artist> aliasedBack = artists.stream().filter(x -> aliases.inverse().containsKey(x.getName().toLowerCase()))
                .map(x -> new Artist(aliases.inverse().get(x.getName().toLowerCase()), x.getPlaycount(), x.getRankValue()))
                .collect(toList());

        return Stream.concat(aliasedForward.stream(), aliasedBack.stream()).distinct().collect(toList());
    }

    public Map<String, Artist> generatePartialMatchMap(List<Artist> artists, Set<? extends Show> festivalData) {
        List<Artist> partialMatchArtists = fetchPartialMatches(artists, festivalData);
        // To bypass case inconsistancy in Clashfinder data
        Set<Artist> shortArtists = partialMatchArtists.stream()
                .map(a -> new Artist(a.getName().toLowerCase(), a.getPlaycount(), a.getRankValue(), a.getMatch()))
                .collect(Collectors.toSet());
        return shortArtists.stream().collect(toMap(a -> a.getName().toLowerCase(), Function.identity()));
    }

    private Map<String, Artist> generateArtistVariantMap(List<Artist> variantArtists) {
        return variantArtists.stream().collect(toMap(a -> a.getName().toLowerCase(), Function.identity()));
    }

    private List<Artist> fetchPartialMatches(List<Artist> artists, Set<? extends Show> festivalData) {
        List<Show> filteredFestivalData = festivalData.stream().filter(g -> artists.stream().anyMatch(a -> isPartialMatch(a, g))).collect(toList());
        List<Artist> matches = artists.stream()
                .filter(a -> (
                        filteredFestivalData.stream().anyMatch(
                                g -> isPartialMatch(a, g)
                        )))
                .collect(toList());
        return matches.stream().flatMap(a ->
                        filteredFestivalData.stream()
                                .filter(g -> isPartialMatch(a, g))
                                .map(n -> new Artist(n.getName(), a.getPlaycount(), a.getRankValue(), a.getName()))
        ).collect(toList());
    }

    private boolean isPartialMatch(Artist artist, Show show) {
        return containsMatch(artist, show)
                || isBandMatch(artist.getName(), show.getName())
                || isTLABandMatch(artist.getName(), show.getName());
    }

    private boolean containsMatch(Artist artist, Show show) {
        String g = show.getName().replaceAll(",", " ");
        return artist.getName().contains(" ") &&
                ((g.toLowerCase().contains(artist.getName().toLowerCase())) && (
                        g.toLowerCase().contains(" " + artist.getName().toLowerCase() + " ")
                                || g.toLowerCase().startsWith(artist.getName().toLowerCase() + " ")
                                || g.toLowerCase().endsWith(" " + artist.getName().toLowerCase())
                ));
    }

    private boolean isTLABandMatch(String listened, String showArtist) {
        if (listened.length() == 3) {
            return showArtist.contains(listened.toUpperCase());
        }
        return false;
    }

    private boolean isBandMatch(String listened, String showArtist) {
        if (listened.contains(" ") && listened.contains("&")) {
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
