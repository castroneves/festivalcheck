package service;

import cache.CheckerCache;
import clashfinder.ClashfinderSender;
import clashfinder.Event;
import clashfinder.Schedule;
import exception.FestivalConnectionException;
import exception.LastFmException;
import glasto.GlastoRequestSender;
import glasto.GlastoResponseParser;
import intersection.IntersectionFinder;
import lastfm.LastFmSender;
import pojo.Act;
import schedule.ScheduleBuilder;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Created by Adam on 27/04/2015.
 */
@Path("/")
@Produces({"application/json"})
public class GlastoResource {

    private final IntersectionFinder finder;
    private final ScheduleBuilder scheduleBuilder;

    public GlastoResource(GlastoConfiguration config) {
        finder = new IntersectionFinder(new GlastoRequestSender(), new GlastoResponseParser(), new LastFmSender(config.getLastFm()), new CheckerCache(config.getJedis()), new ClashfinderSender());
        scheduleBuilder = new ScheduleBuilder();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{festival}/{username}")
    public List<Act> getActsForUsername(@PathParam("username") String username, @PathParam("festival") String festival, @QueryParam("year") String year) {
        try {
            return finder.findIntersection(username, festival, year);
        } catch (FestivalConnectionException e) {
            throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("Unable to retrieve festival data").type("text/plain").build());
        } catch (LastFmException e) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).type("text/plain").build());
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/r/{festival}/{token}")
    public List<Act> getRecommendedActsForUsername(@PathParam("token") String token, @PathParam("festival") String festival, @QueryParam("year") String year) {
        try {
            return finder.findRIntersection(token, festival, year);
        } catch (FestivalConnectionException e) {
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Unable to retrieve festival data").type("text/plain").build());
        } catch (LastFmException e) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).type("text/plain").build());
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/s/{festival}/{username}")
    public Schedule getScheduleForUsername(@PathParam("username") String username, @PathParam("festival") String festival, @QueryParam("year") String year) {
        try {
            List<Event> intersection = finder.findSIntersection(username, festival, year);
            return scheduleBuilder.createSchedule(intersection);
        }  catch (LastFmException e) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).type("text/plain").build());
        }
    }
}
