package intersection;

import org.testng.annotations.Test;
import pojo.Artist;
import spotify.domain.SpotifyArtist;

import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.*;

public class SpotifyOrderingCreatorTest {
    private SpotifyOrderingCreator spotifyOrderingCreator = new SpotifyOrderingCreator();

    @Test
    public void groupsAndOrdersArtistsByFrequency() {
        SpotifyArtist a1 = new SpotifyArtist("John Farnham");
        SpotifyArtist a2 = new SpotifyArtist("Blue October");
        SpotifyArtist a3 = new SpotifyArtist("Genesis");
        SpotifyArtist a4 = new SpotifyArtist("Blue October");
        SpotifyArtist a5 = new SpotifyArtist("Genesis");
        SpotifyArtist a6 = new SpotifyArtist("Genesis");
        SpotifyArtist a7 = new SpotifyArtist("John Parr");
        List<SpotifyArtist> spotifyArtists = Arrays.asList(a1, a2, a3, a4, a5, a6, a7);

        List<Artist> result = spotifyOrderingCreator.artistListByFrequency(spotifyArtists);

        assertEquals(result.get(0).getName(), "Genesis");
        assertEquals(result.get(0).getPlaycount(), "3");
        assertEquals(result.get(1).getName(), "Blue October");
        assertEquals(result.get(1).getPlaycount(), "2");
        assertEquals(result.get(2).getName(), "John Farnham");
        assertEquals(result.get(2).getPlaycount(), "1");
        assertEquals(result.get(3).getName(), "John Parr");
        assertEquals(result.get(3).getPlaycount(), "1");
    }
}