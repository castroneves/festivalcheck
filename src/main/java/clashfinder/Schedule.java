package clashfinder;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

/**
 * Created by Adam on 10/07/2015.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Schedule {
    private Map<String,List<Event>> sched;
    private List<Event> schedule;
    private List<Event> clashes;

    public Schedule(List<Event> schedule, List<Event> clashes) {
        this.schedule = schedule;
        this.clashes = clashes;
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
}
