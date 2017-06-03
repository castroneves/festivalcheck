package spotify;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.config.SpotifyConfig;
import spotify.domain.*;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.util.List;
import java.util.concurrent.Future;

import static java.util.stream.Collectors.toList;
import static spotify.AsyncPaginationUtils.paginateAsync;


/**
 * Created by Adam on 23/08/2015.
 */
@Singleton
public class SpotifySender {
    private static final Logger logger = LoggerFactory.getLogger(SpotifySender.class);

    private final Client client;

    private static final String baseUrl = "https://accounts.spotify.com/api/token";
    private static final String tracksUrl = "https://api.spotify.com/v1/me/tracks";

    private final String clientId;
    private final String secret;

    @Inject
    public SpotifySender(SpotifyConfig config) {
        clientId = config.getClientId();
        secret = config.getSecret();
        client = JerseyClientBuilder.newClient();
    }

    public AccessToken getAuthToken(final String authCode, final String redirectUrl) {
        WebTarget resource = client.target(baseUrl);
        MultivaluedMap<String,String> request = new MultivaluedHashMap<>();
        request.add("grant_type", "authorization_code");
        request.add("code", authCode);
        request.add("redirect_uri", redirectUrl);
        request.add("client_id", clientId);
        request.add("client_secret", secret);
        return resource.request(MediaType.APPLICATION_FORM_URLENCODED_TYPE).accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.form(request), AccessToken.class);
    }


    // Use limit and offset to paginate
    public List<SpotifyTracksResponse> getSavedTracks(final String accessCode) {
        return paginateAsync(this::savedTracksRequest, new SpotifyDetails(accessCode),50);
    }


    private Future<SpotifyTracksResponse> savedTracksRequest(int retrieved, SpotifyDetails details) {
        WebTarget resource = client.target(tracksUrl)
        .queryParam("limit", "50")
        .queryParam("offset", String.valueOf(retrieved));
        return resource.request().header("Authorization", "Bearer " + details.getAccessCode()).accept(MediaType.APPLICATION_JSON_TYPE).async()
                .get(SpotifyTracksResponse.class);
    }

    private UserProfile getUserId(final String accessCode) {
        WebTarget resource = client.target("https://api.spotify.com/v1/me");
        return resource.request().header("Authorization", "Bearer " + accessCode).accept(MediaType.APPLICATION_JSON_TYPE)
                .get(UserProfile.class);
    }


    public List<SpotifyPlaylistTracksResponse> getPlayListTracks(String accessCode, boolean externalPlaylistsIncluded) {
        UserProfile userId = getUserId(accessCode);
        List<SpotifyPlaylist> playlists = getPlaylists(accessCode, userId.getId());
        logger.info("Playlists for : " + userId.getId() + " :: " + playlists.stream().map(x -> x.getId()).collect(toList()));
        return getTracksFromPlaylists(accessCode,userId.getId(),playlists, externalPlaylistsIncluded);
    }


    private List<SpotifyPlaylist> getPlaylists(final String accessCode, final String userId) {
        SpotifyPlaylistResponse playlistResponse = getPlaylistResponse(0, new SpotifyDetails(accessCode, userId));
        return playlistResponse.getItems();
    }

    private SpotifyPlaylistResponse getPlaylistResponse(int offset, SpotifyDetails details) {
        WebTarget resource = client.target("https://api.spotify.com/v1/users/" + details.getUserId() + "/playlists")
                .queryParam("limit", "50")
                .queryParam("offset", String.valueOf(offset));
        return resource.request().header("Authorization", "Bearer " + details.getAccessCode()).accept(MediaType.APPLICATION_JSON_TYPE)
                .get(SpotifyPlaylistResponse.class);
    }

    private List<SpotifyPlaylistTracksResponse> getTracksFromPlaylists(final String accessCode, final String userId,
                                                                       final List<SpotifyPlaylist> playlists, boolean externalPlaylistsIncluded) {
        return playlists.stream()
                .filter(p -> shouldUsePlaylist(p, userId, externalPlaylistsIncluded))
                .flatMap(p -> paginateAsync(this::getSpotifyPlaylistTracksResponse,
                        new SpotifyDetails(accessCode, p, userId), 100)
                        .stream())
                .collect(toList());
    }

    private boolean shouldUsePlaylist(SpotifyPlaylist p, String userId, boolean externalPlaylistsIncluded) {
        if (externalPlaylistsIncluded) {
            return true;
        }
        return p.getOwner().getId().equals(userId);
    }

    private Future<SpotifyPlaylistTracksResponse> getSpotifyPlaylistTracksResponse(int retrieved, SpotifyDetails details) {
            WebTarget resource =
                    client.target("https://api.spotify.com/v1/users/" + details.getPlaylist().getOwner().getId() + "/playlists/" + details.getPlaylist().getId() + "/tracks")
                            .queryParam("limit", "100")
                            .queryParam("offset", String.valueOf(retrieved));

            return resource.request().header("Authorization", "Bearer " + details.getAccessCode()).accept(MediaType.APPLICATION_JSON_TYPE).async()
                    .get(SpotifyPlaylistTracksResponse.class);

    }
}
