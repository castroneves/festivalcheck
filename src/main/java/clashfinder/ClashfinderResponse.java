package clashfinder;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.List;

/**
 * Created by adam.heinke on 01/07/2015.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class ClashfinderResponse {
    private List<Location> locations;
    private String timezone;

    public List<Location> getLocations() {
        return locations;
    }

    public void setLocations(List<Location> locations) {
        this.locations = locations;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
}
