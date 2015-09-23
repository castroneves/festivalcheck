package intersection;

import org.testng.annotations.Test;
import pojo.Artist;
import pojo.Show;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import static org.testng.Assert.*;

public class ArtistMapGeneratorTest {

    private ArtistMapGenerator generator = new ArtistMapGenerator();

    @Test
    public void simpleArtistPropagadedtoMap() {
        Artist artist = new Artist("Mike and the Mechanics", "2", 10);
        Map<String, Artist> result = generator.generateLastFmMap(new HashSet<>(), Arrays.asList(artist));

        assertTrue(result.containsKey("mike and the mechanics"));
        Artist resultArtist = result.get("mike and the mechanics");
        assertEquals(resultArtist.getPlaycount(),"2");
        assertEquals(resultArtist.getRankValue(), new Integer(10));
    }

    @Test
    public void ampersandToAndInteroperable() {
        Artist artist = new Artist("Mike and the Mechanics", "2", 10);
        Map<String, Artist> result = generator.generateLastFmMap(new HashSet<>(), Arrays.asList(artist));

        assertTrue(result.containsKey("mike & the mechanics"));
        Artist resultArtist = result.get("mike & the mechanics");
        assertEquals(resultArtist.getPlaycount(),"2");
        assertEquals(resultArtist.getRankValue(), new Integer(10));
    }

    @Test
    public void andToAmpersandInteroperable() {
        Artist artist = new Artist("Mike & the Mechanics", "2", 10);
        Map<String, Artist> result = generator.generateLastFmMap(new HashSet<>(), Arrays.asList(artist));

        assertTrue(result.containsKey("mike and the mechanics"));
        Artist resultArtist = result.get("mike and the mechanics");
        assertEquals(resultArtist.getPlaycount(),"2");
        assertEquals(resultArtist.getRankValue(), new Integer(10));
    }

    @Test
    public void partialMatchContainingSpace() {
        Show show = new Show() {
            @Override
            public String getName() {
                return "Peter Gabriel, Phil Collins and Mike Rutherford";
            }
        };
        Artist artist = new Artist("Phil Collins", "2", 10);
        Map<String, Artist> result = generator.generateLastFmMap(new HashSet<>(Arrays.asList(show)), Arrays.asList(artist));

        assertTrue(result.containsKey("peter gabriel, phil collins and mike rutherford"));
    }

    @Test
    public void partialMatchContainingSpaceBeginning() {
        Show show = new Show() {
            @Override
            public String getName() {
                return "Peter Gabriel, Phil Collins and Mike Rutherford";
            }
        };
        Artist artist = new Artist("Peter Gabriel", "2", 10);
        Map<String, Artist> result = generator.generateLastFmMap(new HashSet<>(Arrays.asList(show)), Arrays.asList(artist));

        assertTrue(result.containsKey("peter gabriel, phil collins and mike rutherford"));
    }

    @Test
    public void partialMatchContainingSpaceEnd() {
        Show show = new Show() {
            @Override
            public String getName() {
                return "Peter Gabriel, Phil Collins and Mike Rutherford";
            }
        };
        Artist artist = new Artist("Mike Rutherford", "2", 10);
        Map<String, Artist> result = generator.generateLastFmMap(new HashSet<>(Arrays.asList(show)), Arrays.asList(artist));

        assertTrue(result.containsKey("peter gabriel, phil collins and mike rutherford"));
    }

    @Test
    public void noPartialMatchWithoutSpace() {
        Show show = new Show() {
            @Override
            public String getName() {
                return "Peter Gabriel, Phil Collins and Mike Rutherford";
            }
        };
        Artist artist = new Artist("Rutherford", "2", 10);
        Map<String, Artist> result = generator.generateLastFmMap(new HashSet<>(Arrays.asList(show)), Arrays.asList(artist));

        assertFalse(result.containsKey("peter gabriel, phil collins and mike rutherford"));
    }


}