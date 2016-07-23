package schedule;

import clashfinder.ClashfinderSender;
import clashfinder.domain.Event;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by castroneves on 23/07/2016.
 */
public class ClashfinderUrlBuilder {

    private static final String BASE_URL = "http://clashfinder.com/s/";

    @Inject
    private ClashfinderSender clashfinderSender;


    public String buildUrl(List<Event> schedule, List<Event> clashes, String festival, String year) {
        String suffix = clashfinderSender.fetchClashfinderSuffix(festival, year);
        StringBuilder builder = new StringBuilder();
        builder.append(BASE_URL);
        builder.append(suffix);
        builder.append("/?");
        String listenedSchedule = schedule.stream().filter(x -> x.getScrobs() != 0).map(Event::getShortName).collect(Collectors.joining(","));
        appendArtists(builder, listenedSchedule, "hl1=");

        String listenedClash = clashes.stream().filter(x -> x.getScrobs() != 0).map(Event::getShortName).collect(Collectors.joining(","));
        appendArtists(builder, listenedClash, "hl2=");

        String reccoSchedule = schedule.stream().filter(x -> x.getReccorank() != -1).map(Event::getShortName).collect(Collectors.joining(","));
        appendArtists(builder, reccoSchedule, "hl3=");

        String reccoClash = clashes.stream().filter(x -> x.getReccorank() != -1).map(Event::getShortName).collect(Collectors.joining(","));
        appendArtists(builder, reccoClash, "hl4=");

        return builder.toString();
    }

    private void appendArtists(StringBuilder builder, String artists, String colour) {
        if (!artists.equals("")) {
            builder.append(colour);
            builder.append(artists);
            builder.append("&");
        }
    }
}
