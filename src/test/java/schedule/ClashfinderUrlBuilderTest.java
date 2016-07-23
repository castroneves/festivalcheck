package schedule;

import clashfinder.ClashfinderSender;
import clashfinder.domain.Event;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

/**
 * Created by castroneves on 23/07/2016.
 */
public class ClashfinderUrlBuilderTest {


    @Mock
    private ClashfinderSender clashfinderSender;

    @InjectMocks
    private ClashfinderUrlBuilder clashfinderUrlBuilder;

    private final String suffix = "suffix";
    private final String festival = "festival";
    private final String year = "2016";

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void urlCorrectlyFormedForSchedule() {
        Event e1 = eventWithScrobs("black");
        Event e2 = eventWithRank("toto");
        Event e3 = eventWithScrobs("genesis");
        Event e4 = eventWithRank("prince");
        Event e5 = eventWithScrobs("journey");
        Event e6 = eventWithRank("placebo");

        Event e7 = eventWithScrobs("blueoct");
        Event e8 = eventWithRank("roxette");
        Event e9 = eventWithScrobs("ultravox");
        Event e10 = eventWithRank("train");
        Event e11 = eventWithScrobs("travis");
        Event e12 = eventWithRank("keane");

        List<Event> schedule = Arrays.asList(e1, e2, e3, e4, e5, e6);
        List<Event> clashes = Arrays.asList(e7, e8, e9, e10, e11, e12);

        when(clashfinderSender.fetchClashfinderSuffix(festival, year)).thenReturn(suffix);

        String actualUrl = clashfinderUrlBuilder.buildUrl(schedule, clashes, festival, year);

        String expected = "http://clashfinder.com/s/suffix/?hl1=black,genesis,journey" +
                "&hl2=blueoct,ultravox,travis&hl3=toto,prince,placebo&hl4=roxette,train,keane&";
        assertEquals(actualUrl, expected);

    }

    private Event eventWithScrobs(String artist) {
        Event e1 = new Event();
        e1.setShortName(artist);
        e1.setScrobs(2);
        return e1;
    }

    private Event eventWithRank(String artist) {
        Event e1 = new Event();
        e1.setShortName(artist);
        e1.setReccorank(2);
        return e1;
    }
}