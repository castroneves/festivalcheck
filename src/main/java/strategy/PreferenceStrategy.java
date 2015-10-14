package strategy;

import clashfinder.domain.Event;
import lastfm.domain.Artist;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Adam on 04/08/2015.
 */
public interface PreferenceStrategy {


    List<Event> findOrderedInterection(Set<Event> clashfinderData, Map<String, Artist> listenedArtists, Map<String, Artist> reccoArtists);
}
