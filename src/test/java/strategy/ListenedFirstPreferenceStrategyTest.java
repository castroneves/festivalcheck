package strategy;

import clashfinder.domain.Event;
import org.testng.annotations.Test;
import lastfm.domain.Artist;

import java.util.*;

import static org.testng.Assert.*;

public class ListenedFirstPreferenceStrategyTest {

    private ListenedFirstPreferenceStrategy strategy = new ListenedFirstPreferenceStrategy();

    @Test
    public void listenedArtistsFavouredOverRecommeneded() {
        Set<Event> events = generateEvents();
        Map<String, Artist> listened = generateListened();
        Map<String, Artist> recommended = generateRecommended();

        List<Event> result = strategy.findOrderedInterection(events, listened, recommended);

        assertListened(result);
        assertRecommended(result);
    }

    private void assertRecommended(List<Event> result) {
        Event snow = result.get(3);
        assertEquals(snow.getName(), "Snow Patrol");
        assertEquals(snow.getScrobs(), 0);
        assertEquals(snow.getReccorank(), 1);

        Event hack = result.get(4);
        assertEquals(hack.getName(), "Mike Hackett");
        assertEquals(hack.getScrobs(), 0);
        assertEquals(hack.getReccorank(), 3);
    }

    private void assertListened(List<Event> result) {
        assertEquals(result.size(), 5);
        Event bo = result.get(0);
        assertEquals(bo.getName(), "Blue October");
        assertEquals(bo.getScrobs(), 50);

        Event mike = result.get(1);
        assertEquals(mike.getName(), "Mike and the Mechanics");
        assertEquals(mike.getScrobs(), 10);

        Event genesis = result.get(2);
        assertEquals(genesis.getName(), "Genesis");
        assertEquals(genesis.getScrobs(), 9);
    }

    private Map<String, Artist> generateListened() {
        Map<String,Artist> result = new HashMap<>();
        result.put("mike and the mechanics", new Artist("Mike and the Mechanics", "10", 0));
        result.put("genesis", new Artist("Genesis", "9", 0));
        result.put("blue october", new Artist("Blue October", "50", 0));
        result.put("naked and famous", new Artist("Naked and Famous", "100", 0));
        return result;
    }

    private Map<String, Artist> generateRecommended() {
        Map<String,Artist> result = new HashMap<>();
        result.put("snow patrol", new Artist("Snow Patrol", "0", 1));
        result.put("big country", new Artist("Big Country", "0", 2));
        result.put("mike hackett", new Artist("Mike Hackett", "0", 3));
        return result;
    }

    private Set<Event> generateEvents() {
        Event e1 = new Event();
        e1.setName("Mike and the Mechanics");
        Event e2 = new Event();
        e2.setName("Genesis");
        Event e3 = new Event();
        e3.setName("Snow Patrol");
        Event e4 = new Event();
        e4.setName("Lady Gaga");
        Event e5 = new Event();
        e5.setName("Blue October");
        Event e6 = new Event();
        e6.setName("Mike Hackett");
        return new HashSet<>(Arrays.asList(e1,e2,e3,e4,e5,e6));
    }

}