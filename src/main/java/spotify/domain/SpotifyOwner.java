package spotify.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by castroneves on 31/05/2017.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpotifyOwner {
    private String id;
    private String displayName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
