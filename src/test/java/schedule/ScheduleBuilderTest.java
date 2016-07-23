package schedule;

import clashfinder.domain.Event;
import clashfinder.domain.Schedule;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

public class ScheduleBuilderTest {

    private final String festival = "festival";
    private final String year = "2016";
    private final String url = "/clashfinder/url";
    @Mock
    private ClashfinderUrlBuilder clashfinderUrlBuilder;

    @InjectMocks
    private ScheduleBuilder scheduleBuilder;

    private static final DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void everyEventInScheduleWithNoClashes() {
        when(clashfinderUrlBuilder.buildUrl(anyList(), anyList(), eq(festival), eq(year))).thenReturn(url);

        Event e1 = new Event();
        e1.setName("Genesis");
        e1.setStage("Pyramid");
        e1.setStart(formatter.parseDateTime("2015-06-26 13:00"));
        e1.setEnd(formatter.parseDateTime("2015-06-26 14:15"));

        Event e2 = new Event();
        e2.setName("Mike and the Mechanics");
        e2.setStage("Other");
        e2.setStart(formatter.parseDateTime("2015-06-26 12:00"));
        e2.setEnd(formatter.parseDateTime("2015-06-26 13:00"));

        Event e4 = new Event();
        e4.setName("Wilson Phillips");
        e4.setStage("John Peel");
        e4.setStart(formatter.parseDateTime("2015-06-28 12:00"));
        e4.setEnd(formatter.parseDateTime("2015-06-28 13:00"));

        Event e5 = new Event();
        e5.setName("Michael Bolton");
        e5.setStage("John Peel");
        e5.setStart(formatter.parseDateTime("2015-06-25 12:00"));
        e5.setEnd(formatter.parseDateTime("2015-06-25 13:00"));


        Event e3 = new Event();
        e3.setName("Clemie Fischer");
        e3.setStage("Other");
        e3.setStart(formatter.parseDateTime("2015-06-27 12:00"));
        e3.setEnd(formatter.parseDateTime("2015-06-27 13:00"));

        Schedule schedule = scheduleBuilder.createSchedule(Arrays.asList(e1, e2, e4, e5, e3), festival, year);

        assertEquals(schedule.getClash().size(), 0);
        assertTrue(schedule.getSched() instanceof LinkedHashMap);
        List<String> actualOrderedKeys = schedule.getSched().keySet().stream().collect(toList());
        assertEquals(actualOrderedKeys, Arrays.asList("Thursday","Friday","Saturday","Sunday"));
        List<Event> friday = schedule.getSched().get("Friday");
        Event mike = friday.get(0);
        assertSame(mike, e2);
        assertEquals(mike.getStartTime(), "12:00");
        assertEquals(mike.getEndTime(), "13:00");
        assertEquals(mike.getTtStart(), "1.0000");
        assertEquals(mike.getTtDuration(), "1.0000");

        Event genesis = friday.get(1);
        assertSame(genesis, e1);
        assertEquals(genesis.getStartTime(), "13:00");
        assertEquals(genesis.getEndTime(), "14:15");
        assertEquals(genesis.getTtStart(), "2.0000");
        assertEquals(genesis.getTtDuration(), "1.2500");
        assertEquals(schedule.getClashfinderUrl(), url);
    }

    @Test
    public void overlappingEventsMarkedAsClashOrderingPreserved() {
        when(clashfinderUrlBuilder.buildUrl(anyList(), anyList(), eq(festival), eq(year))).thenReturn(url);
        Event e1 = new Event();
        e1.setName("Genesis");
        e1.setStage("Pyramid");
        e1.setStart(formatter.parseDateTime("2015-06-26 13:00"));
        e1.setEnd(formatter.parseDateTime("2015-06-26 14:15"));

        Event e2 = new Event();
        e2.setName("Mike and the Mechanics");
        e2.setStage("Other");
        e2.setStart(formatter.parseDateTime("2015-06-26 12:00"));
        e2.setEnd(formatter.parseDateTime("2015-06-26 13:15"));

        Event e4 = new Event();
        e4.setName("Wilson Phillips");
        e4.setStage("John Peel");
        e4.setStart(formatter.parseDateTime("2015-06-26 14:00"));
        e4.setEnd(formatter.parseDateTime("2015-06-26 14:40"));

        Event e5 = new Event();
        e5.setName("Michael Bolton");
        e5.setStage("John Peel");
        e5.setStart(formatter.parseDateTime("2015-06-26 12:00"));
        e5.setEnd(formatter.parseDateTime("2015-06-26 13:00"));

        Schedule schedule = scheduleBuilder.createSchedule(Arrays.asList(e1, e2, e4, e5), festival, year);

        assertEquals(schedule.getClash().size(), 1);
        assertEquals(schedule.getSched().size(), 1);

        List<Event> friday = schedule.getSched().get("Friday");
        assertEquals(friday, Arrays.asList(e5,e1));

        List<Event> fridayClash = schedule.getClash().get("Friday");
        assertEquals(fridayClash, Arrays.asList(e2,e4));
        assertEquals(schedule.getClashfinderUrl(), url);
    }


}