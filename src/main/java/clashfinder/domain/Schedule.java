package clashfinder.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

/**
 * Created by Adam on 10/07/2015.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Schedule {
    private Map<String,List<Event>> sched;
    private Map<String,List<Event>> clash;

    private List<Event> schedule;
    private List<Event> clashes;

    private String clashfinderUrl;

    public Schedule() {}

    public Schedule(Map<String, List<Event>> sched, Map<String, List<Event>> clash, String clashfinderUrl) {
        this.sched = sched;
        this.clash = clash;
        this.clashfinderUrl = clashfinderUrl;
    }

    public List<Event> getSchedule() {
        return schedule;
    }

    public void setSchedule(List<Event> schedule) {
        this.schedule = schedule;
    }

    public List<Event> getClashes() {
        return clashes;
    }

    public void setClashes(List<Event> clashes) {
        this.clashes = clashes;
    }

    public Map<String, List<Event>> getSched() {
        return sched;
    }

    public void setSched(Map<String, List<Event>> sched) {
        this.sched = sched;
    }

    public Map<String, List<Event>> getClash() {
        return clash;
    }

    public void setClash(Map<String, List<Event>> clash) {
        this.clash = clash;
    }

    public String getClashfinderUrl() {
        return clashfinderUrl;
    }

    public void setClashfinderUrl(String clashfinderUrl) {
        this.clashfinderUrl = clashfinderUrl;
    }
}
