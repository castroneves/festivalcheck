package spotify.domain;



import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Created by Adam on 08/09/2015.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpotifyTracksResponse implements SpotifyResponse {

    private List<SpotifyTracksItem> items;
    private Integer total;

    public List<SpotifyTracksItem> getItems() {
        return items;
    }

    public void setItems(List<SpotifyTracksItem> items) {
        this.items = items;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }
}
