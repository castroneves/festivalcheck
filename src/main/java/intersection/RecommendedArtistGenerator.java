package intersection;

import com.google.inject.Inject;
import lastfm.LastFmSender;
import lastfm.domain.Artist;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Created by Adam on 07/02/2016.
 */
public class RecommendedArtistGenerator {

    public static final int LIMIT = 500;
    @Inject
    private LastFmSender lastFmSender;
    @Inject
    private SpotifyOrderingCreator orderingCreator;

    public List<Artist> fetchRecommendations(List<Artist> actualArtists) {
        List<Artist> rawRecArtists = lastFmSender.fetchSimilarArtists(actualArtists.stream().map(Artist::getName).collect(toList()), LIMIT);
        List<Artist> recArtists = orderingCreator.artistListByFrequency(rawRecArtists);
        recArtists.removeAll(actualArtists);
        List<Artist> finalRecArtists = recArtists.stream().filter(x -> x.getPlaycountInt() > 1).collect(toList());
        enrichReccoRank(finalRecArtists);
        return finalRecArtists;
    }

    private void enrichReccoRank(List<Artist> artist) {
        for (int i = 0; i < artist.size(); i++) {
            artist.get(i).setReccoRank(i);
        }
    }
}
