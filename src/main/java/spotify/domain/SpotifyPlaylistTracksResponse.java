package spotify.domain;



import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Created by Adam on 13/10/2015.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class SpotifyPlaylistTracksResponse implements SpotifyResponse, PagableResult {

    private List<SpotifyTracksItem> items;
    private Integer total;

    @Override
    public Integer getTotal() {
        return total;
    }

    @Override
    public Integer getResults() {
        return items.size();
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
