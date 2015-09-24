package spotify.domain;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * Created by Adam on 08/09/2015.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpotifyArtist {

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
