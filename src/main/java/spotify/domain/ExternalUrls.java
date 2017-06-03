package spotify.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by castroneves on 03/06/2017.
 */
public class ExternalUrls {
    @JsonProperty("spotify")
    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
