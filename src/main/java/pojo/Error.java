package pojo;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * Created by Adam on 15/06/2015.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Error {

    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
