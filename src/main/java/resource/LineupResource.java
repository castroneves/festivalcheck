package resource;

import cache.CheckerCache;
import com.codahale.metrics.annotation.Metered;
import com.google.inject.Inject;
import domain.RumourResponse;
import efestivals.domain.Act;
import intersection.RumourIntersectionFinder;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

import static cache.CacheKeyPrefix.RUMOUR;

/**
 * Created by Adam on 20/02/2016.
 */
@Path("/")
@Produces({"application/json"})
public class LineupResource {
    @Inject
    private RumourIntersectionFinder rumourIntersectionFinder;
    @Inject
    private CheckerCache cache;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{festival}/{username}")
    @Metered
    public List<Act> getActsForUsername(@PathParam("username") String username, @PathParam("festival") String festival, @QueryParam("year") String year) {
        RumourResponse response = cache.getOrLookup(username + festival + year, () -> rumourIntersectionFinder.findIntersection(username, festival, year), RUMOUR, RumourResponse.class);
        return response.getActs();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/rec/{festival}/{username}")
    @Metered
    public List<Act> getRecommendedActsForUsername(@PathParam("username") String username, @PathParam("festival") String festival, @QueryParam("year") String year) {
        return rumourIntersectionFinder.computeRecommendedIntersection(username, festival, year);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/spotify/{festival}/{code}/{redirectUrl}")
    @Metered
    public List<Act> getActsForSpotify(@PathParam("code") String code, @PathParam("festival") String festival, @QueryParam("year") String year, @PathParam("redirectUrl") String redirectUrl) {
        String cleanedCode = code.endsWith("#_=_") ? code.replaceAll("#_=_", "") : code;
        return rumourIntersectionFinder.findSpotifyIntersection(cleanedCode, festival, year, redirectUrl);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/spotify/rec/{festival}/{code}/{redirectUrl}")
    @Metered
    public List<Act> getRecommendedActsForSpotify(@PathParam("code") String code, @PathParam("festival") String festival, @QueryParam("year") String year, @PathParam("redirectUrl") String redirectUrl) {
        String cleanedCode = code.endsWith("#_=_") ? code.replaceAll("#_=_", "") : code;
        return rumourIntersectionFinder.findSpotifyRecommendedIntersection(cleanedCode, festival, year, redirectUrl);
    }

}
