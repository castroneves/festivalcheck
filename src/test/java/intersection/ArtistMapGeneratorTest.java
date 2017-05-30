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
        Map<String, Artist> result = generator.generateLastFmMap(new HashSet<>(), Arrays.asList(artist)).getArtistMap();

        assertTrue(result.containsKey("mike and the mechanics"));
        Artist resultArtist = result.get("mike and the mechanics");
        assertEquals(resultArtist.getPlaycount(),"2");
        assertEquals(resultArtist.getRankValue(), new Integer(10));
    }

    @Test
    public void ampersandToAndInteroperable() {
        Artist artist = new Artist("Mike and the Mechanics", "2", 10);
        Map<String, Artist> result = generator.generateLastFmMap(new HashSet<>(), Arrays.asList(artist)).getArtistMap();

        assertTrue(result.containsKey("mike & the mechanics"));
        Artist resultArtist = result.get("mike & the mechanics");
        assertEquals(resultArtist.getPlaycount(),"2");
        assertEquals(resultArtist.getRankValue(), new Integer(10));
    }

    @Test
    public void andToAmpersandInteroperable() {
        Artist artist = new Artist("Mike & the Mechanics", "2", 10);
        Map<String, Artist> result = generator.generateLastFmMap(new HashSet<>(), Arrays.asList(artist)).getArtistMap();

        assertTrue(result.containsKey("mike and the mechanics"));
        Artist resultArtist = result.get("mike and the mechanics");
        assertEquals(resultArtist.getPlaycount(),"2");
        assertEquals(resultArtist.getRankValue(), new Integer(10));
    }

    @Test
    public void partialMatchContainingSpace() {
        Show show = () -> "Peter Gabriel, Phil Collins and Mike Rutherford";
        Artist artist = new Artist("Phil Collins", "2", 10);
        Map<String, Artist> result = generator.generateLastFmMap(new HashSet<>(Arrays.asList(show)), Arrays.asList(artist)).getArtistMap();

        assertTrue(result.containsKey("peter gabriel, phil collins and mike rutherford"));
    }

    @Test
    public void partialMatchContainingSpaceBeginning() {
        Show show = () -> "Peter Gabriel, Phil Collins and Mike Rutherford";
        Artist artist = new Artist("Peter Gabriel", "2", 10);
        Map<String, Artist> result = generator.generateLastFmMap(new HashSet<>(Arrays.asList(show)), Arrays.asList(artist)).getArtistMap();

        assertTrue(result.containsKey("peter gabriel, phil collins and mike rutherford"));
    }

    @Test
    public void partialMatchContainingSpaceEnd() {
        Show show = () -> "Peter Gabriel, Phil Collins and Mike Rutherford";
        Artist artist = new Artist("Mike Rutherford", "2", 10);
        Map<String, Artist> result = generator.generateLastFmMap(new HashSet<>(Arrays.asList(show)), Arrays.asList(artist)).getArtistMap();

        assertTrue(result.containsKey("peter gabriel, phil collins and mike rutherford"));
    }

    @Test
    public void noPartialMatchWithoutSpace() {
        Show show = () -> "Peter Gabriel, Phil Collins and Mike Rutherford";
        Artist artist = new Artist("Rutherford", "2", 10);
        Map<String, Artist> result = generator.generateLastFmMap(new HashSet<>(Arrays.asList(show)), Arrays.asList(artist)).getArtistMap();

        assertFalse(result.containsKey("peter gabriel, phil collins and mike rutherford"));
    }

    @Test
    public void multiplePartialMatches() {
        Show show1 = () -> "Frank Turner";
        Show show = () -> "A Radical Round-up Special : Frank Turner With Billy Bragg: \"the Road Beneath My Feet\" -";
        Show show2 = () -> "David Cameron, Frank Turner and the Poll Tax";
        Artist artist = new Artist("Frank Turner", "2", 10);

        Map<String, Artist> result = generator.generateLastFmMap(new HashSet<>(Arrays.asList(show1,show, show2)), Arrays.asList(artist)).getArtistMap();

        assertTrue(result.containsKey("frank turner"));
        assertTrue(result.containsKey("a radical round-up special : frank turner with billy bragg: \"the road beneath my feet\" -"));
        assertTrue(result.containsKey("david cameron, frank turner and the poll tax"));
        assertEquals(result.get("david cameron, frank turner and the poll tax").getMatch(), "Frank Turner");
    }

    @Test
    public void bandNamePartialMatch() {
        Show show = () -> "Daryl Hall & John Oates";
        Artist artist = new Artist("Hall & Oates", "2", 10);

        Map<String, Artist> result = generator.generateLastFmMap(new HashSet<>(Arrays.asList(show)), Arrays.asList(artist)).getArtistMap();

        assertTrue(result.containsKey("daryl hall & john oates"));
    }

    @Test
    public void bandNamePartialMatchMixedAmpersand() {
        Show show = () -> "Daryl Hall & John Oates";
        Artist artist = new Artist("Hall and Oates", "2", 10);

        Map<String, Artist> result = generator.generateLastFmMap(new HashSet<>(Arrays.asList(show)), Arrays.asList(artist)).getArtistMap();

        assertTrue(result.containsKey("daryl hall & john oates"));
    }

    @Test
    public void tlaBandPartialMatch() {
        Show show = () -> "Jeff Lynne's ELO";
        Artist artist = new Artist("elo", "2", 10);
        Map<String, Artist> result = generator.generateLastFmMap(new HashSet<>(Arrays.asList(show)), Arrays.asList(artist)).getArtistMap();

        assertTrue(result.containsKey("jeff lynne's elo"));
    }

    @Test
    public void partialMatchesOfWordsDoNotMatch() {
        Show show = () -> "The beatbox";
        Artist artist = new Artist("The Beat", "2", 10);

        Map<String, Artist> result = generator.generateLastFmMap(new HashSet<>(Arrays.asList(show)), Arrays.asList(artist)).getArtistMap();

        assertTrue(!result.containsKey("the beatbox"));
    }

    @Test
    public void wholeMatchNotFlaggedAsPartial() {
        Show show = () -> "Mumford & Sons";
        Artist artist = new Artist("Mumford & Sons", "2", 10);

        Map<String, Artist> result = generator.generateLastFmMap(new HashSet<>(Arrays.asList(show)), Arrays.asList(artist)).getArtistMap();

        assertTrue(result.containsKey("mumford & sons"));
        assertNull(result.get("mumford & sons").getMatch());
    }

    @Test
    public void mapMerging(){
        Show show = () -> "Mumford & Sons";
        Artist artist = new Artist("Mumford & Sons", "2", 10);
        Artist artist1 = new Artist("mumford & sons", "2", 10);

        Map<String, Artist> result = generator.generateLastFmMap(new HashSet<>(Arrays.asList(show)), Arrays.asList(artist, artist1)).getArtistMap();

        assertTrue(result.containsKey("mumford & sons"));
    }
}