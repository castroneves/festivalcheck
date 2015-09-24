package service;

import clashfinder.domain.Event;
import clashfinder.domain.Schedule;
import com.google.inject.Inject;
import exception.FestivalConnectionException;
import exception.LastFmException;
import intersection.RumourIntersectionFinder;
import intersection.ScheduleIntersectionFinder;
import pojo.Act;
import schedule.ScheduleBuilder;
import strategy.ListenedFirstPreferenceStrategy;
import strategy.PreferenceStrategy;
import strategy.ReccoFirstPreferenceStrategy;

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

    @Inject
    private RumourIntersectionFinder rumourIntersectionFinder;
    @Inject
    private ScheduleIntersectionFinder scheduleIntersectionFinder;
    @Inject
    private ScheduleBuilder scheduleBuilder;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{festival}/{username}")
    public List<Act> getActsForUsername(@PathParam("username") String username, @PathParam("festival") String festival, @QueryParam("year") String year) {
        try {
            return rumourIntersectionFinder.findIntersection(username, festival, year);
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
            return rumourIntersectionFinder.findRecommendedIntersection(token, festival, year);
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
            List<Event> intersection = scheduleIntersectionFinder.findSIntersection(username, festival, year);
            return scheduleBuilder.createSchedule(intersection);
        } catch (LastFmException e) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).type("text/plain").build());
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/s/spotify/{festival}/{authcode}")
    public Schedule getScheduleSpotify(@PathParam("authcode") String authcode, @PathParam("festival") String festival, @QueryParam("year") String year) {
        try {
            List<Event> intersection = scheduleIntersectionFinder.findSpotifyScheduleIntersection(authcode, festival, year);
            return scheduleBuilder.createSchedule(intersection);
        }  catch (LastFmException e) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).type("text/plain").build());
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/s/r/{festival}/{token}")
    public Schedule getReccomendedSchedule(@PathParam("token") String token, @PathParam("festival") String festival, @QueryParam("year") String year) {
        try {
            List<Event> intersection = scheduleIntersectionFinder.findReccoScheduleIntersection(token, festival, year);
            return scheduleBuilder.createSchedule(intersection);
        }  catch (LastFmException e) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).type("text/plain").build());
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/s/h/{strategy}/{festival}/{token}")
    public Schedule getHybridSchedule(@PathParam("token") String token, @PathParam("festival") String festival, @QueryParam("year") String year, @PathParam("strategy") String strategy) {
        try {
            PreferenceStrategy preferenceStrategy = getPreferenceStrategy(strategy);
            List<Event> intersection = scheduleIntersectionFinder.findHybridScheduleIntersection(token,festival,year, preferenceStrategy);
            return scheduleBuilder.createSchedule(intersection);
        }  catch (LastFmException e) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).type("text/plain").build());
        }
    }

    private PreferenceStrategy getPreferenceStrategy(String strategy) {
        if(strategy.equals("listened")) {
            return new ListenedFirstPreferenceStrategy();
        }
        else if(strategy.equals("recco")) {
            return new ReccoFirstPreferenceStrategy();
        }
        else {
            throw new RuntimeException("Preference Strategy not found");
        }
    }
}
