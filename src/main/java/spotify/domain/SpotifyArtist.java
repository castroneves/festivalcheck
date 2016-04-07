package spotify.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import domain.BasicArtist;

/**
 * Created by Adam on 08/09/2015.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpotifyArtist implements BasicArtist {

    public SpotifyArtist() {}

    public SpotifyArtist(String name) {
        this.name = name;
    }

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
