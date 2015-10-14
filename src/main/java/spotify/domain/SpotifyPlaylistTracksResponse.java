package spotify.domain;


import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.List;

/**
 * Created by Adam on 13/10/2015.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class SpotifyPlaylistTracksResponse implements SpotifyResponse {

    private List<SpotifyTracksItem> items;
    private Integer total;

    @Override
    public Integer getTotal() {
        return total;
    }

    public List<SpotifyTracksItem> getItems() {
        return items;
    }

    public void setItems(List<SpotifyTracksItem> items) {
        this.items = items;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }
}
