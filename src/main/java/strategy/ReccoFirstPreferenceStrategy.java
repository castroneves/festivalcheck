package strategy;

import clashfinder.domain.Event;
import lastfm.domain.Artist;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Created by Adam on 05/08/2015.
 */
public class ReccoFirstPreferenceStrategy implements PreferenceStrategy {
    @Override
    public List<Event> findOrderedInterection(Set<Event> clashfinderData, Map<String, Artist> listenedArtists, Map<String, Artist> reccoArtists) {
        List<Event> listened = clashfinderData.stream().filter(g -> listenedArtists.containsKey(g.getName().toLowerCase()))
                .map(e -> new Event(e, Integer.parseInt(listenedArtists.get(e.getName().toLowerCase()).getPlaycount())))
                .sorted((x, y) -> Integer.compare(y.getScrobs(), x.getScrobs()))
                .collect(toList());
        List<Event> recco = clashfinderData.stream().filter(g -> reccoArtists.containsKey(g.getName().toLowerCase()))
                .map(e -> new Event(e, 0, reccoArtists.get(e.getName().toLowerCase()).getRankValue()))
                .sorted((x, y) -> Integer.compare(x.getReccorank(), y.getReccorank()))
                .collect(toList());

        return Stream.concat(recco.stream(), listened.stream()).collect(toList());
    }
}
