package spotify.domain;



import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Created by Adam on 13/10/2015.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpotifyPlaylistResponse implements SpotifyResponse {

    private List<SpotifyPlaylist> items;
    private Integer total;

    @Override
    public Integer getTotal() {
        return total;
    }

    public List<SpotifyPlaylist> getItems() {
        return items;
    }

    public void setItems(List<SpotifyPlaylist> items) {
        this.items = items;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }
}
