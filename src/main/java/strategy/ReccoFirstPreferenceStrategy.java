package strategy;

import clashfinder.Event;
import pojo.Artist;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Adam on 05/08/2015.
 */
public class ReccoFirstPreferenceStrategy implements PreferenceStrategy {
    @Override
    public List<Event> findOrderedInterection(Set<Event> clashfinderData, Map<String, Artist> listenedArtists, Map<String, Artist> reccoArtists) {
        return null;
    }
}
