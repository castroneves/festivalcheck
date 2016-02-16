package service;

import cache.CacheKeyPrefix;
import cache.CheckerCache;
import clashfinder.domain.Event;
import clashfinder.domain.Schedule;
import com.google.inject.Inject;
import domain.RumourResponse;
import efestivals.domain.Act;
import intersection.RumourIntersectionFinder;
import intersection.ScheduleIntersectionFinder;
import lastfm.LastFmSender;
import schedule.ScheduleBuilder;
import strategy.ListenedFirstPreferenceStrategy;
import strategy.PreferenceStrategy;
import strategy.ReccoFirstPreferenceStrategy;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

import static cache.CacheKeyPrefix.RUMOUR;

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
    @Inject
    private CheckerCache cache;

    @Inject
    private LastFmSender lastFmSender;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{festival}/{username}")
    public List<Act> getActsForUsername(@PathParam("username") String username, @PathParam("festival") String festival, @QueryParam("year") String year) {
        RumourResponse response = cache.getOrLookup(username + festival + year, () -> rumourIntersectionFinder.findIntersection(username, festival, year), RUMOUR, RumourResponse.class);
        return response.getActs();
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
    public List<Act> getLastFmRecommendedActsForUsername(@PathParam("token") String token, @PathParam("festival") String festival, @QueryParam("year") String year) {
        return rumourIntersectionFinder.findRecommendedIntersection(token, festival, year);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/rec/{festival}/{username}")
    public List<Act> getRecommendedActsForUsername(@PathParam("username") String username, @PathParam("festival") String festival, @QueryParam("year") String year) {
        return rumourIntersectionFinder.computeRecommendedIntersection(username, festival, year);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/s/{festival}/{username}")
    public Schedule getScheduleForUsername(@PathParam("username") String username, @PathParam("festival") String festival, @QueryParam("year") String year) {
        return cache.getOrLookup(username + festival + year, () -> {
            List<Event> intersection = scheduleIntersectionFinder.findSIntersection(username, festival, year);
            return scheduleBuilder.createSchedule(intersection);
        }, CacheKeyPrefix.SCHEDULE, Schedule.class);
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
    @Path("/s/rec/{festival}/{username}")
    public Schedule getReccomendedSchedule(@PathParam("username") String username, @PathParam("festival") String festival, @QueryParam("year") String year) {
        List<Event> intersection = scheduleIntersectionFinder.findReccoScheduleIntersection(username, festival, year);
        return scheduleBuilder.createSchedule(intersection);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/s/h/{strategy}/{festival}/{username}")
    public Schedule getHybridSchedule(@PathParam("username") String username, @PathParam("festival") String festival, @QueryParam("year") String year, @PathParam("strategy") String strategy) {
        PreferenceStrategy preferenceStrategy = getPreferenceStrategy(strategy);
        List<Event> intersection = scheduleIntersectionFinder.findHybridScheduleIntersection(username,festival,year, preferenceStrategy);
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
