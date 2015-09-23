package spotify.domain;


import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.List;

/**
 * Created by Adam on 08/09/2015.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpotifyTracksResponse {

    private List<SpotifyTracksItem> items;

    public List<SpotifyTracksItem> getItems() {
        return items;
    }

    public void setItems(List<SpotifyTracksItem> items) {
        this.items = items;
    }
}
