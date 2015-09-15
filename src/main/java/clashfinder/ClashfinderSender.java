package clashfinder;

import clashfinder.domain.ClashfinderResponse;
import clashfinder.domain.Event;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;

import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

public class ClashfinderSender {

    private static final String baseUrl = "http://clashfinder.com/data/event/";
    private final Client client;


    public ClashfinderSender() {
        ClientConfig cc = new DefaultClientConfig();
        cc.getClasses().add(JacksonJsonProvider.class);
        client = Client.create(cc);
    }

    private static final Map<String,String> clashfinderFestivalMap = new HashMap<>();

    static {
        clashfinderFestivalMap.put("glastonbury", "g");
        clashfinderFestivalMap.put("iow","iow");
    }

    public Set<Event> fetchData(String festival, String year) {
        ClashfinderResponse response = fetchRawResponse(festival,year==null ? "2015" : year);
        response.getLocations().stream().forEach(l -> l.getEvents().stream().forEach(e -> e.setStage(l.getName())));
        return response.getLocations().stream().flatMap(l -> l.getEvents().stream()).collect(toSet());
    }

    private ClashfinderResponse fetchRawResponse(String festival, String year) {
        WebResource resource = client.resource(buildUrl(festival,year));
        return resource.accept(MediaType.APPLICATION_JSON_TYPE).
                type(MediaType.APPLICATION_JSON_TYPE).get(ClashfinderResponse.class);
    }

    private String buildUrl(String festival, String year) {
        return baseUrl + festival + year + ".json";
    }
}
