package spotify.domain;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.List;

/**
 * Created by Adam on 08/09/2015.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpotifyTrack {
    private String name;
    private List<SpotifyArtist> artists;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<SpotifyArtist> getArtists() {
        return artists;
    }

    public void setArtists(List<SpotifyArtist> artists) {
        this.artists = artists;
    }
}
