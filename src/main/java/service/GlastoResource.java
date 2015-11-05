package service;

import clashfinder.domain.Event;
import clashfinder.domain.Schedule;
import com.google.inject.Inject;
import efestivals.domain.Act;
import intersection.RumourIntersectionFinder;
import intersection.ScheduleIntersectionFinder;
import schedule.ScheduleBuilder;
import strategy.ListenedFirstPreferenceStrategy;
import strategy.PreferenceStrategy;
import strategy.ReccoFirstPreferenceStrategy;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
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
        return rumourIntersectionFinder.findIntersection(username, festival, year);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/spotify/{festival}/{code}/{redirectUrl}")
    public List<Act> getActsForSpotify(@PathParam("code") String code, @PathParam("festival") String festival, @QueryParam("year") String year, @PathParam("redirectUrl") String redirectUrl) {
        String cleanedCode = code.endsWith("#_=_") ? code.replaceAll("#_=_", "") : code;
        return rumourIntersectionFinder.findSpotifyIntersection(cleanedCode, festival, year, redirectUrl);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/r/{festival}/{token}")
    public List<Act> getRecommendedActsForUsername(@PathParam("token") String token, @PathParam("festival") String festival, @QueryParam("year") String year) {
        return rumourIntersectionFinder.findRecommendedIntersection(token, festival, year);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/s/{festival}/{username}")
    public Schedule getScheduleForUsername(@PathParam("username") String username, @PathParam("festival") String festival, @QueryParam("year") String year) {
        List<Event> intersection = scheduleIntersectionFinder.findSIntersection(username, festival, year);
        return scheduleBuilder.createSchedule(intersection);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/s/spotify/{festival}/{authcode}/{redirectUrl}")
    public Schedule getScheduleSpotify(@PathParam("authcode") String code, @PathParam("festival") String festival, @QueryParam("year") String year, @PathParam("redirectUrl") String redirectUrl) {
        String cleanedCode = code.endsWith("#_=_") ? code.replaceAll("#_=_", "") : code;
        List<Event> intersection = scheduleIntersectionFinder.findSpotifyScheduleIntersection(cleanedCode, festival, year, redirectUrl);
        return scheduleBuilder.createSchedule(intersection);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/s/r/{festival}/{token}")
    public Schedule getReccomendedSchedule(@PathParam("token") String token, @PathParam("festival") String festival, @QueryParam("year") String year) {
        List<Event> intersection = scheduleIntersectionFinder.findReccoScheduleIntersection(token, festival, year);
        return scheduleBuilder.createSchedule(intersection);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/s/h/{strategy}/{festival}/{token}")
    public Schedule getHybridSchedule(@PathParam("token") String token, @PathParam("festival") String festival, @QueryParam("year") String year, @PathParam("strategy") String strategy) {
        PreferenceStrategy preferenceStrategy = getPreferenceStrategy(strategy);
        List<Event> intersection = scheduleIntersectionFinder.findHybridScheduleIntersection(token,festival,year, preferenceStrategy);
        return scheduleBuilder.createSchedule(intersection);
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
