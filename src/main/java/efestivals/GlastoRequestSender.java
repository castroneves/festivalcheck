package efestivals;
/**
 * Created by Adam on 23/04/2015.
 */
import com.google.inject.Inject;
import exception.FestivalConnectionException;
import efestivals.domain.Act;
import org.glassfish.jersey.client.JerseyClientBuilder;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.util.HashSet;
import java.util.Set;


public class GlastoRequestSender {

    private static final String urlPrefix = "http://www.efestivals.co.uk/festivals/";
    private static final String urlSuffix = "/lineup.shtml";
    private static final String vFestivalPrefix = "vvv";
    private final Client client;

    @Inject
    private GlastoResponseParser parser;

    public GlastoRequestSender() {
        client = JerseyClientBuilder.createClient();
    }

    public Set<Act> getFestivalData(String festival, String year) throws FestivalConnectionException {
        String rawGlastoData = getRawResponse(festival, year);
        return new HashSet<>(parser.parseRawResponse(rawGlastoData));
    }

    private String getRawResponse(String festival, String inputYear) throws FestivalConnectionException {
        String year = inputYear == null ? "2015" : inputYear;
        if(festival.startsWith(vFestivalPrefix)) {
            String venue = festival.replaceAll("vvv", "");
            festival = "v/" + venue;
        }
        try {
            WebTarget resource = client.target(urlPrefix + festival + "/" + year + urlSuffix);
            return resource.request(MediaType.TEXT_HTML).get(String.class);
        } catch (Exception e) {
            throw new FestivalConnectionException();
        }
    }

    public void setParser(GlastoResponseParser parser) {
        this.parser = parser;
    }
}
