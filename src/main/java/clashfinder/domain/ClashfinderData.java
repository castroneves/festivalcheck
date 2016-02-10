package clashfinder.domain;

import java.util.Set;

/**
 * Created by Adam on 10/02/2016.
 */
public class ClashfinderData {
    private Set<Event> events;

    public ClashfinderData() {
    }

    public ClashfinderData(Set<Event> events) {
        this.events = events;
    }

    public Set<Event> getEvents() {
        return events;
    }

    public void setEvents(Set<Event> events) {
        this.events = events;
    }
}
