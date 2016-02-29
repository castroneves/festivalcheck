package spotify.domain;

import java.util.List;

/**
 * Created by Adam on 13/10/2015.
 */
public interface SpotifyResponse<T> {
    Integer getTotal();
    List<T> getItems();
}
