package clashfinder.domain;

import java.util.Set;

/**
 * Created by Adam on 10/02/2016.
 */
public class ClashFinderData {
    private Set<Event> events;

    public ClashFinderData() {
    }

    public ClashFinderData(Set<Event> events) {
        this.events = events;
    }

    public Set<Event> getEvents() {
        return events;
    }

    public void setEvents(Set<Event> events) {
        this.events = events;
    }
}
