package schedule;

import clashfinder.domain.Event;
import clashfinder.domain.Schedule;
import org.joda.time.*;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.format.TextStyle;
import java.util.*;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

/**
 * Created by Adam on 10/07/2015.
 */
public class ScheduleBuilder {

    @Inject
    private ClashfinderUrlBuilder clashfinderUrlBuilder;

    public Schedule createSchedule(List<Event> events, String festival, String year) {
        events.stream().forEach(this::setUpTimeTableData);
        List<Event> schedule = new ArrayList<>();
        List<Event> clashes = new ArrayList<>();

        cleanData(events);

        events.stream().filter(e -> Double.parseDouble(e.getTtStart()) < 15.0).forEachOrdered(
                e -> {
                    if (canGoInSchedule(e, schedule)) {
                        schedule.add(e);
                    } else {
                        clashes.add(e);
                    }
                }
        );

        String url = clashfinderUrlBuilder.buildUrl(schedule, clashes, festival, year);

        return new Schedule(sortAndGroupData(schedule), sortAndGroupData(clashes), url);
    }

    private void setUpTimeTableData(Event e) {
        Duration d = new Duration(e.getStart(), e.getEnd());
        Minutes minutes = d.toStandardMinutes();
        BigDecimal hours = BigDecimal.valueOf(minutes.getMinutes()).divide(BigDecimal.valueOf(60), 4, BigDecimal.ROUND_HALF_UP);
        e.setTtDuration(hours.toString());

        DateTime offsetAdjusted = e.getStart().minusHours(11);
        int hourOfDay = offsetAdjusted.getHourOfDay();
        int minuteOfHour = offsetAdjusted.getMinuteOfHour();
        BigDecimal ttStart = BigDecimal.valueOf(hourOfDay).add(BigDecimal.valueOf(minuteOfHour).divide(BigDecimal.valueOf(60), 4, BigDecimal.ROUND_HALF_UP));
        e.setTtStart(ttStart.toString());
    }

    private Map<String, List<Event>> sortAndGroupData(List<Event> events) {
        List<Event> sorted = events.stream().sorted((x, y) -> {
            int day = Integer.valueOf(DayOfWeek.valueOf(x.getDay().toUpperCase()).getValue()).compareTo(DayOfWeek.valueOf(y.getDay().toUpperCase()).getValue());
            if (day != 0) {
                return day;
            }
            return Double.valueOf(x.getTtStart()).compareTo(Double.valueOf(y.getTtStart()));
        }).collect(toList());

        return sorted.stream().collect(groupingBy(
                        e -> e.getDay(), LinkedHashMap::new,
                        toList()
                )
        );
    }

    private void cleanData(List<Event> events) {
        events.stream().forEach(e -> {
            String day = java.time.DayOfWeek.of(e.getStart().getDayOfWeek()).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            e.setDay(day);
            DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm");
            e.setStartTime(formatter.print(e.getStart()));
            e.setEndTime(formatter.print(e.getEnd()));
        });

    }

    private boolean canGoInSchedule(Event e, List<Event> schedule) {
        return !schedule.stream().anyMatch(f -> new Interval(e.getStart(), e.getEnd()).overlaps(new Interval(f.getStart(), f.getEnd())));
    }
}
