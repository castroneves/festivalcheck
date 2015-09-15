package clashfinder.domain;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.List;

/**
 * Created by adam.heinke on 01/07/2015.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Location {
    private String name;
    private List<Event> events;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }
}
