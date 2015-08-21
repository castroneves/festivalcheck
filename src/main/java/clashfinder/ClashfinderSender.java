package clashfinder;

import com.codahale.metrics.annotation.Timed;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;

import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class ClashfinderSender {

    private static final String baseUrl = "http://clashfinder.com/data/event/";
    private Client client;


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

    @Timed
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

    public static void main(String[] args) {
        ClashfinderSender sender = new ClashfinderSender();
        Set<Event> events = sender.fetchData("g", "2015");
        Set<String> set = events.stream().map(e -> e.getStage()).collect(toSet());
        set.stream().forEach(System.out::println);
//        System.out.println(events);
    }
}
