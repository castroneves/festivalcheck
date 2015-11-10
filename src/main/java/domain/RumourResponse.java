package domain;

import efestivals.domain.Act;

import java.util.List;

/**
 * Created by Adam on 10/11/2015.
 */
public class RumourResponse {
    private List<Act> acts;

    public RumourResponse() {}

    public RumourResponse(List<Act> acts) {
        this.acts = acts;
    }

    public List<Act> getActs() {
        return acts;
    }

    public void setActs(List<Act> acts) {
        this.acts = acts;
    }
}
