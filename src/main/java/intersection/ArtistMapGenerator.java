package intersection;

import com.google.inject.Singleton;
import pojo.Artist;
import pojo.Show;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * Created by Adam on 12/09/2015.
 */
@Singleton
public class ArtistMapGenerator {

    public Map<String, Artist> generateLastFmMap(Set<? extends Show> clashfinderData, List<Artist> artists) {
        Map<String, Artist> lastFmMap = artists.stream().collect(toMap(a -> a.getName().toLowerCase(), Function.identity()));
        List<Artist> variantArtists = fetchVariantArtists(artists);
        Map<String, Artist> variants = generateArtistVariantMap(variantArtists);
        lastFmMap.putAll(variants);
        Map<String, Artist> additionalMap = generateLikeMap(artists, clashfinderData);
        lastFmMap.putAll(additionalMap);
        return lastFmMap;
    }

    public Map<String, Artist> generateLikeMap(List<Artist> artists, Set<? extends Show> glastoData) {
        Set<Artist> shortArtists = new HashSet<>(fetchShortArtists(artists,glastoData));
        return shortArtists.stream().collect(toMap(a -> a.getName().toLowerCase(), Function.identity()));
    }

    private Map<String, Artist> generateArtistVariantMap(List<Artist> variantArtists) {
        return variantArtists.stream().collect(toMap(a -> a.getName().toLowerCase(), Function.identity()));
    }

    private List<Artist> fetchShortArtists(List<Artist> artists, Set<? extends Show> glastoData) {

        List<Artist> matches = artists.stream().filter(a -> a.getName().contains(" ") && glastoData.stream().anyMatch(g -> g.getName().toLowerCase().contains(a.getName().toLowerCase()))).collect(toList());
        return matches.stream().map(a -> {
            String adjustedName = glastoData.stream()
                    .filter(g -> g.getName().toLowerCase().contains(a.getName().toLowerCase())).findFirst().get().getName();
            return new Artist(adjustedName,a.getPlaycount(),a.getRankValue());
        }).collect(toList());

    }

    private List<Artist> fetchVariantArtists(List<Artist> artists) {
        List<Artist> andToAmp = artists.stream()
                .filter(a -> a.getName().contains(" "))
                .filter(a -> a.getName().toLowerCase().contains(" and "))
                .map(a -> new Artist(a.getName().replaceAll("(?i) and ", " & "),a.getPlaycount(),a.getRankValue()))
                .collect(toList());
        List<Artist> ampToAnd = artists.stream()
                .filter(a -> a.getName().contains(" "))
                .filter(a -> a.getName().toLowerCase().contains(" & "))
                .map(a -> new Artist(a.getName().replaceAll("(?i) & ", " and "),a.getPlaycount(),a.getRankValue()))
                .collect(toList());
        return Stream.concat(andToAmp.stream(), ampToAnd.stream()).collect(toList());
    }
}
