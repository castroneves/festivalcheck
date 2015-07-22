package pojo;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * Created by Adam on 27/04/2015.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Response {

    private TopArtists topartists;
    private Recommendations recommendations;
    private String error;
    private String message;


    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public TopArtists getTopartists() {
        return topartists;
    }

    public void setTopartists(TopArtists topartists) {
        this.topartists = topartists;
    }

    public Recommendations getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(Recommendations recommendations) {
        this.recommendations = recommendations;
    }
}
