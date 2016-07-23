package resource;

import cache.CacheKeyPrefix;
import cache.CheckerCache;
import clashfinder.domain.Event;
import clashfinder.domain.Schedule;
import com.codahale.metrics.annotation.Metered;
import com.google.inject.Inject;
import intersection.ScheduleIntersectionFinder;
import lastfm.LastFmSender;
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
@Path("/s/")
@Produces({"application/json"})
public class ScheduleResource {
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
    @Path("/{festival}/{year}/{username}")
    @Metered
    public Schedule getScheduleForUsername(@PathParam("username") String username, @PathParam("festival") String festival, @PathParam("year") String year) {
        return cache.getOrLookup(username + festival + year, () -> {
            List<Event> intersection = scheduleIntersectionFinder.findSIntersection(username, festival, year);
            return scheduleBuilder.createSchedule(intersection, festival, year);
        }, CacheKeyPrefix.SCHEDULE, Schedule.class);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/rec/{festival}/{year}/{username}")
    @Metered
    public Schedule getReccomendedSchedule(@PathParam("username") String username, @PathParam("festival") String festival, @PathParam("year") String year) {
        List<Event> intersection = scheduleIntersectionFinder.findReccoScheduleIntersection(username, festival, year);
        return scheduleBuilder.createSchedule(intersection, festival, year);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/h/{strategy}/{festival}/{year}/{username}")
    @Metered
    public Schedule getHybridSchedule(@PathParam("username") String username, @PathParam("festival") String festival, @PathParam("year") String year, @PathParam("strategy") String strategy) {
        PreferenceStrategy preferenceStrategy = getPreferenceStrategy(strategy);
        List<Event> intersection = scheduleIntersectionFinder.findHybridScheduleIntersection(username,festival,year, preferenceStrategy);
        return scheduleBuilder.createSchedule(intersection, festival, year);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/spotify/{festival}/{year}/{authcode}/{redirectUrl}")
    @Metered
    public Schedule getScheduleSpotify(@PathParam("authcode") String code, @PathParam("festival") String festival, @PathParam("year") String year, @PathParam("redirectUrl") String redirectUrl) {
        String cleanedCode = code.endsWith("#_=_") ? code.replaceAll("#_=_", "") : code;
        List<Event> intersection = scheduleIntersectionFinder.findSpotifyScheduleIntersection(cleanedCode, festival, year, redirectUrl);
        return scheduleBuilder.createSchedule(intersection, festival, year);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/spotify/rec/{festival}/{year}/{authcode}/{redirectUrl}")
    @Metered
    public Schedule getRecommendedScheduleSpotify(@PathParam("authcode") String code, @PathParam("festival") String festival, @PathParam("year") String year, @PathParam("redirectUrl") String redirectUrl) {
        String cleanedCode = code.endsWith("#_=_") ? code.replaceAll("#_=_", "") : code;
        List<Event> intersection = scheduleIntersectionFinder.findSpotifyRecommendedScheduleIntersection(cleanedCode, festival, year, redirectUrl);
        return scheduleBuilder.createSchedule(intersection, festival, year);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/h/spotify/{strategy}/{festival}/{year}/{authcode}/{redirectUrl}")
    @Metered
    public Schedule getHybridSpotifySchedule(@PathParam("authcode") String authCode, @PathParam("festival") String festival, @PathParam("year") String year, @PathParam("redirectUrl") String redirectUrl, @PathParam("strategy") String strategy) {
        PreferenceStrategy preferenceStrategy = getPreferenceStrategy(strategy);
        List<Event> intersection = scheduleIntersectionFinder.findHybridSpotifyScheduleIntersection(authCode,festival,year, redirectUrl, preferenceStrategy);
        return scheduleBuilder.createSchedule(intersection, festival, year);
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
