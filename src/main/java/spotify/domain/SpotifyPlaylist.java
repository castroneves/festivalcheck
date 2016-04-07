package spotify.domain;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by Adam on 13/10/2015.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class SpotifyPlaylist {

    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // Add owner
}
