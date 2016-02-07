package intersection;

import domain.BasicArtist;
import lastfm.domain.Artist;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * Created by Adam on 24/09/2015.
 */
public class SpotifyOrderingCreator {

    public List<Artist> artistListByFrequency(List<? extends BasicArtist> collect) {
        Map<String, Integer> frequencyMap = collect.stream().map(x -> x.getName()).collect(Collectors.toConcurrentMap(w -> w, w -> 1, Integer::sum));
        return frequencyMap.keySet().stream().sorted((x, y) -> frequencyMap.get(y).compareTo(frequencyMap.get(x)))
                .map(x -> new Artist(x, frequencyMap.get(x).toString(), 0)).collect(toList());
    }
}
