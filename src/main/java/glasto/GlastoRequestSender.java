package glasto; /**
 * Created by Adam on 23/04/2015.
 */
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import exception.FestivalConnectionException;
import pojo.Act;

import javax.ws.rs.core.MediaType;
import java.util.List;


public class GlastoRequestSender {

    private static final String urlPrefix = "http://www.efestivals.co.uk/festivals/";
    private static final String urlSuffix = "/lineup.shtml";
    private final Client client;

    public GlastoRequestSender() {
        ClientConfig cc = new DefaultClientConfig();
        client = Client.create(cc);
    }

    public String getRawResponse(String festival, String inputYear) throws FestivalConnectionException {
        String year = inputYear == null ? "2015" : inputYear;
        if(festival.startsWith("vvv")) {
            String venue = festival.replaceAll("vvv", "");
            festival = "v/" + venue;
        }
        try {
            WebResource resource = client.resource(urlPrefix + festival + "/" + year + urlSuffix);
            return resource.type(MediaType.TEXT_HTML).get(String.class);
        } catch (Exception e) {
            throw new FestivalConnectionException();
        }
    }

    public static void main(String[] args) throws Exception {
        GlastoRequestSender sender = new GlastoRequestSender();
        String rawResponse = sender.getRawResponse("vvvstafford", "2015");
        System.out.println(rawResponse);
        GlastoResponseParser parser = new GlastoResponseParser();
        List<Act> acts = parser.parseRawResponse(rawResponse);
        System.out.println(acts.size());
    }
}
