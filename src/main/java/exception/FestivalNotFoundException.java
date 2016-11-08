package exception;

/**
 * Created by castroneves on 08/11/2016.
 */
public class FestivalNotFoundException extends RuntimeException {

    private String festival;

    public FestivalNotFoundException(String festival) {
        super("Festival unavailable");
        this.festival = festival;
    }

    public String getFestival() {
        return festival;
    }

    public void setFestival(String festival) {
        this.festival = festival;
    }
}
