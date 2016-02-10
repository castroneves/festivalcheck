package clashfinder.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.joda.ser.DateTimeSerializer;
import domain.Show;
import service.serialise.DateTimeDeserialiser;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.joda.time.DateTime;


/**
 * Created by adam.heinke on 01/07/2015.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Event implements Show {
    private String name;
    private String stage;
    private String day;
    private String startTime;
    private String endTime;

    private DateTime start;

    private DateTime end;
    private int scrobs;

    private String ttStart;
    private String ttDuration;

    private int reccorank = -1;

    private String matchString;

    public Event() {
    }

    public Event(Event e, int scrobs) {
        this.name = e.getName();
        this.stage = e.getStage();
        this.start = e.getStart();
        this.end = e.getEnd();
        this.scrobs = scrobs;
    }

    public Event(Event e, int scrobs, int reccorank) {
        this(e,scrobs);
        this.reccorank = reccorank;
    }

    public Event(Event e, String matchString) {
        this(e,e.getScrobs(),e.getReccorank());
        this.matchString = matchString;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    @JsonSerialize(using = DateTimeSerializer.class)
    public DateTime getStart() {
        return start;
    }

    @JsonDeserialize(using = DateTimeDeserialiser.class)
    public void setStart(DateTime start) {
        this.start = start;
    }

    @JsonSerialize(using = DateTimeSerializer.class)
    public DateTime getEnd() {
        return end;
    }

    @JsonDeserialize(using = DateTimeDeserialiser.class)
    public void setEnd(DateTime end) {
        this.end = end;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public int getScrobs() {
        return scrobs;
    }

    public void setScrobs(int scrobs) {
        this.scrobs = scrobs;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getTtStart() {
        return ttStart;
    }

    public void setTtStart(String ttStart) {
        this.ttStart = ttStart;
    }

    public String getTtDuration() {
        return ttDuration;
    }

    public void setTtDuration(String ttDuration) {
        this.ttDuration = ttDuration;
    }

    public int getReccorank() {
        return reccorank;
    }

    public void setReccorank(int reccorank) {
        this.reccorank = reccorank;
    }

    public String getMatchString() {
        return matchString;
    }

    public void setMatchString(String matchString) {
        this.matchString = matchString;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Event event = (Event) o;

        if (name != null ? !name.equals(event.name) : event.name != null) return false;
        if (stage != null ? !stage.equals(event.stage) : event.stage != null) return false;
        if (day != null ? !day.equals(event.day) : event.day != null) return false;
        if (startTime != null ? !startTime.equals(event.startTime) : event.startTime != null) return false;
        return !(endTime != null ? !endTime.equals(event.endTime) : event.endTime != null);

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (stage != null ? stage.hashCode() : 0);
        result = 31 * result + (day != null ? day.hashCode() : 0);
        result = 31 * result + (startTime != null ? startTime.hashCode() : 0);
        result = 31 * result + (endTime != null ? endTime.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Event{" +
                "name='" + name + '\'' +
                ", stage='" + stage + '\'' +
                ", day='" + day + '\'' +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", scrobs=" + scrobs +
                ", reccorank=" + reccorank +
                '}';
    }
}
