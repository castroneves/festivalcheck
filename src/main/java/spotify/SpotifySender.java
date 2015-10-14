package spotify;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import service.config.SpotifyConfig;
import spotify.domain.*;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;


/**
 * Created by Adam on 23/08/2015.
 */
@Singleton
public class SpotifySender {

    private final Client client;

    private static final String baseUrl = "https://accounts.spotify.com/api/token";
    private static final String tracksUrl = "https://api.spotify.com/v1/me/tracks";

    private final String clientId;
    private final String secret;

    @Inject
    public SpotifySender(SpotifyConfig config) {
        clientId = config.getClientId();
        secret = config.getSecret();
        ClientConfig cc = new DefaultClientConfig();
        cc.getClasses().add(JacksonJsonProvider.class);
        client = Client.create(cc);
    }

    public AccessToken getAuthToken(final String authCode) {
        WebResource resource = client.resource(baseUrl);
        MultivaluedMap<String,String> request = new MultivaluedMapImpl();
        request.add("grant_type", "authorization_code");
        //TODO Remove occasional suffixed garbage before sending
        request.add("code", authCode);
        request.add("redirect_uri", "http://www.wellysplosher.com/schedule.html?source=spotify");
        request.add("client_id", clientId);
        request.add("client_secret", secret);
        return resource.accept(MediaType.APPLICATION_JSON_TYPE).
                type(MediaType.APPLICATION_FORM_URLENCODED_TYPE).post(AccessToken.class, request);
    }


    // Use limit and offset to paginate
    public List<SpotifyTracksResponse> getSavedTracks(final String accessCode) {
        return paginate(this::savedTracksRequest, new SpotifyDetails(accessCode));
    }


    private SpotifyTracksResponse savedTracksRequest(int retrieved, SpotifyDetails details) {
        WebResource resource = client.resource(tracksUrl)
        .queryParam("limit", "50")
        .queryParam("offset", String.valueOf(retrieved));
        return resource.header("Authorization", "Bearer " + details.getAccessCode()).accept(MediaType.APPLICATION_JSON_TYPE)
                .get(SpotifyTracksResponse.class);
    }

    private <T extends SpotifyResponse> List<T> paginate(BiFunction<Integer, SpotifyDetails, T> func, SpotifyDetails details) {
        List<T> responseList = new ArrayList<>();
        int total = 0;
        int retrieved = 0;
        do {
            T response = func.apply(retrieved, details);
            total = response.getTotal();
            retrieved += response.getItems().size();
            responseList.add(response);
        } while(retrieved < total);
        return responseList;
    }

    private UserProfile getUserId(final String accessCode) {
        WebResource resource = client.resource("https://api.spotify.com/v1/me");
        return resource.header("Authorization", "Bearer " + accessCode).accept(MediaType.APPLICATION_JSON_TYPE)
                .get(UserProfile.class);
    }


    public List<SpotifyPlaylistTracksResponse> getPlayListTracks(String accessCode) {
        UserProfile userId = getUserId(accessCode);
        List<SpotifyPlaylist> playlists = getPlaylists(accessCode, userId.getId());
        return getTracksFromPlaylists(accessCode,userId.getId(),playlists);
    }


    private List<SpotifyPlaylist> getPlaylists(final String accessCode, final String userId) {
        SpotifyPlaylistResponse playlistResponse = getPlaylistResponse(0, new SpotifyDetails(accessCode, userId));
//        List<SpotifyPlaylistResponse> responses = paginate(this::getPlaylistResponse, new SpotifyDetails(accessCode, userId));
//        return responses.stream().flatMap(x -> x.getItems().stream()).collect(toList());
        return playlistResponse.getItems();
    }

    private SpotifyPlaylistResponse getPlaylistResponse(int offset, SpotifyDetails details) {
        WebResource resource = client.resource("https://api.spotify.com/v1/users/" + details.getUserId() + "/playlists")
                .queryParam("limit", "50")
                .queryParam("offset", String.valueOf(offset));
        return resource.header("Authorization", "Bearer " + details.getAccessCode()).accept(MediaType.APPLICATION_JSON_TYPE)
                .get(SpotifyPlaylistResponse.class);
    }

    private List<SpotifyPlaylistTracksResponse> getTracksFromPlaylists(final String accessCode, final String userId,
                                                                       final List<SpotifyPlaylist> playlists) {
        List<SpotifyPlaylistTracksResponse> responses = new ArrayList<>();
        for (SpotifyPlaylist playlist : playlists) {
            List<SpotifyPlaylistTracksResponse> res = paginate(this::getSpotifyPlaylistTracksResponse, new SpotifyDetails(accessCode, playlist.getId(), userId));
            responses.addAll(res);
        }
        return responses;
    }

    private SpotifyPlaylistTracksResponse getSpotifyPlaylistTracksResponse(int retrieved, SpotifyDetails details) {
        WebResource resource =
                client.resource("https://api.spotify.com/v1/users/" + details.getUserId() + "/playlists/" + details.getPlaylistId() + "/tracks")
                        .queryParam("limit", "100")
                        .queryParam("offset", String.valueOf(retrieved));
        return resource.header("Authorization", "Bearer " + details.getAccessCode()).accept(MediaType.APPLICATION_JSON_TYPE)
                .get(SpotifyPlaylistTracksResponse.class);
    }

}
