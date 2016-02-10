package clashfinder;

import clashfinder.domain.ClashfinderData;
import clashfinder.domain.ClashfinderResponse;
import com.google.inject.Inject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import service.config.MappingConfig;

import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

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

    @Inject
    private MappingConfig mappingConfig;


    static {
        // TODO Move to comfig
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
        clashfinderFestivalMap.put("lat2015","lat2015");
        clashfinderFestivalMap.put("lat2014","lat2014");
        clashfinderFestivalMap.put("lat2013","lat2013");
        clashfinderFestivalMap.put("lat2012","lat12");
        clashfinderFestivalMap.put("lat2011","lat2011");
        clashfinderFestivalMap.put("lat2010","latitude2010");
        clashfinderFestivalMap.put("download2015","downloadfest2015");
        clashfinderFestivalMap.put("download2014","dl2014");
        clashfinderFestivalMap.put("download2013","download2013");
        clashfinderFestivalMap.put("download2011","dl2011");
        clashfinderFestivalMap.put("download2010","dl10");
        clashfinderFestivalMap.put("download2009","dl09");
        clashfinderFestivalMap.put("reading2015","r2015");
        clashfinderFestivalMap.put("reading2014","read2014");
        clashfinderFestivalMap.put("reading2013","rdgfest2013");
        clashfinderFestivalMap.put("reading2012","r2012");
        clashfinderFestivalMap.put("reading2011","reading11");
        clashfinderFestivalMap.put("reading2010","rf2010");
        clashfinderFestivalMap.put("vvvstafford2015","vfestivallweston2015");
        clashfinderFestivalMap.put("vvvstafford2014","vnorth2014");
        clashfinderFestivalMap.put("vvvstafford2013","vs2013");
        clashfinderFestivalMap.put("vvvstafford2012","vs2012");
        clashfinderFestivalMap.put("vvvstafford2011","vs2011");
        clashfinderFestivalMap.put("vvvstafford2010","vfestival2010");
        clashfinderFestivalMap.put("vvvchelmsford2015","vfestivalhylands2015");
        clashfinderFestivalMap.put("vvvchelmsford2014","vfest2014chelmsford");
        clashfinderFestivalMap.put("vvvchelmsford2013","vc2013");
        clashfinderFestivalMap.put("vvvchelmsford2012","vc2012");
        clashfinderFestivalMap.put("vvvchelmsford2011","vc2011");
        clashfinderFestivalMap.put("vvvchelmsford2010","v2010chelmsford");
    }

    public ClashfinderData fetchData(String festival, String year) {
        String actualYear = year==null ? "2015" : year;
        String suffix = clashfinderFestivalMap.get(festival + actualYear);
        String actualSuffix = suffix == null ? festival + actualYear :suffix;
        ClashfinderResponse response = fetchRawResponse(actualSuffix);
        response.getLocations().stream().forEach(l -> l.getEvents().stream().forEach(e -> e.setStage(l.getName())));
        return new ClashfinderData(response.getLocations().stream().flatMap(l -> l.getEvents().stream()).collect(toSet()));
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
