package intersection;

import org.testng.annotations.Test;
import lastfm.domain.Artist;
import domain.Show;

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

    @Test
    public void multiplePartialMatches() {
        Show show1 = new Show() {
            @Override
            public String getName() {
                return "Frank Turner";
            }
        };
        Show show = new Show() {
            @Override
            public String getName() {
                return "A Radical Round-up Special : Frank Turner With Billy Bragg: \"the Road Beneath My Feet\" -";
            }
        };

        Show show2 = new Show() {
            @Override
            public String getName() {
                return "David Cameron, Frank Turner and the Poll Tax";
            }
        };
        Artist artist = new Artist("Frank Turner", "2", 10);
        Map<String, Artist> result = generator.generateLastFmMap(new HashSet<>(Arrays.asList(show1,show, show2)), Arrays.asList(artist));

        assertTrue(result.containsKey("frank turner"));
        assertTrue(result.containsKey("a radical round-up special : frank turner with billy bragg: \"the road beneath my feet\" -"));
        assertTrue(result.containsKey("david cameron, frank turner and the poll tax"));
    }

    @Test
    public void bandNamePartialMatch() {
        Show show = new Show() {
            @Override
            public String getName() {
                return "Daryl Hall & John Oates";
            }
        };
        Artist artist = new Artist("Hall & Oates", "2", 10);
        Map<String, Artist> result = generator.generateLastFmMap(new HashSet<>(Arrays.asList(show)), Arrays.asList(artist));

        assertTrue(result.containsKey("daryl hall & john oates"));
    }

    @Test
    public void bandNamePartialMatchMixedAmpersand() {
        Show show = new Show() {
            @Override
            public String getName() {
                return "Daryl Hall & John Oates";
            }
        };
        Artist artist = new Artist("Hall and Oates", "2", 10);
        Map<String, Artist> result = generator.generateLastFmMap(new HashSet<>(Arrays.asList(show)), Arrays.asList(artist));

        assertTrue(result.containsKey("daryl hall & john oates"));
    }

    @Test
    public void tlaBandPartialMatch() {
        Show show = new Show() {
            @Override
            public String getName() {
                return "Jeff Lynne's ELO";
            }
        };
        Artist artist = new Artist("elo", "2", 10);
        Map<String, Artist> result = generator.generateLastFmMap(new HashSet<>(Arrays.asList(show)), Arrays.asList(artist));

        assertTrue(result.containsKey("jeff lynne's elo"));
    }

    @Test
    public void partialMatchesOfWordsDoNotMatch() {
        Show show = new Show() {
            @Override
            public String getName() {
                return "The beatbox";
            }
        };
        Artist artist = new Artist("The Beat", "2", 10);
        Map<String, Artist> result = generator.generateLastFmMap(new HashSet<>(Arrays.asList(show)), Arrays.asList(artist));

        assertTrue(!result.containsKey("the beatbox"));
    }
}