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
        clashfinderFestivalMap.put("g2015", "g2015");
        clashfinderFestivalMap.put("g2014", "g2014");
        clashfinderFestivalMap.put("g2013", "g2013");
        clashfinderFestivalMap.put("g2011", "g2011");
        clashfinderFestivalMap.put("g2010", "g2010");
        clashfinderFestivalMap.put("g2009", "g2009");
        clashfinderFestivalMap.put("iow2015","iow15");
        clashfinderFestivalMap.put("iow2014","iow2014");
        clashfinderFestivalMap.put("iow2013","iowfestival2013");
        clashfinderFestivalMap.put("iow2012","ioow12");
        clashfinderFestivalMap.put("iow2011","iow11");
        clashfinderFestivalMap.put("leeds2015","leeds2011555");
        clashfinderFestivalMap.put("leeds2014","leedsfest14");
        clashfinderFestivalMap.put("leeds2013","leeds2013");
        clashfinderFestivalMap.put("leeds2012","leeds12");
        clashfinderFestivalMap.put("leeds2011","leedsfest2011");
        clashfinderFestivalMap.put("leeds2010","leeds10");
    }

    public Set<Event> fetchData(String festival, String year) {
        String actualYear = year==null ? "2015" : year;
        String suffix = clashfinderFestivalMap.get(festival + actualYear);
        String actualSuffix = suffix == null ? festival + actualYear :suffix;
        ClashfinderResponse response = fetchRawResponse(actualSuffix);
        response.getLocations().stream().forEach(l -> l.getEvents().stream().forEach(e -> e.setStage(l.getName())));
        return response.getLocations().stream().flatMap(l -> l.getEvents().stream()).collect(toSet());
    }

    private ClashfinderResponse fetchRawResponse(String festival) {
        WebResource resource = client.resource(buildUrl(festival));
        return resource.accept(MediaType.APPLICATION_JSON_TYPE).
                type(MediaType.APPLICATION_JSON_TYPE).get(ClashfinderResponse.class);
    }

    private String buildUrl(String festival) {
        return baseUrl + festival + ".json";
    }
}
