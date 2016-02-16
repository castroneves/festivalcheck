package domain;

import lastfm.domain.Artist;

import java.util.Map;

/**
 * Created by Adam on 16/02/2016.
 */
public class ArtistMap {

    private Map<String,Artist> artistMap;


    public ArtistMap() {
    }

    public ArtistMap(Map<String, Artist> artistMap) {
        this.artistMap = artistMap;
    }

    public Map<String, Artist> getArtistMap() {
        return artistMap;
    }

    public void setArtistMap(Map<String, Artist> artistMap) {
        this.artistMap = artistMap;
    }
}
