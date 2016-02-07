package spotify;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;
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

import static java.util.stream.Collectors.toList;


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

    public AccessToken getAuthToken(final String authCode, final String redirectUrl) {
        WebResource resource = client.resource(baseUrl);
        MultivaluedMap<String,String> request = new MultivaluedMapImpl();
        request.add("grant_type", "authorization_code");
        //TODO Remove occasional suffixed garbage before sending
        request.add("code", authCode);
        request.add("redirect_uri", redirectUrl);
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
            // TODO Async requests like LastFm similar artists
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
        System.out.println("Playlists for : " + userId.getId() + " :: " + playlists.stream().map(x -> x.getId()).collect(toList()));
        return getTracksFromPlaylists(accessCode,userId.getId(),playlists);
    }


    private List<SpotifyPlaylist> getPlaylists(final String accessCode, final String userId) {
        SpotifyPlaylistResponse playlistResponse = getPlaylistResponse(0, new SpotifyDetails(accessCode, userId));
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
        try {
            WebResource resource =
                    client.resource("https://api.spotify.com/v1/users/" + details.getUserId() + "/playlists/" + details.getPlaylistId() + "/tracks")
                            .queryParam("limit", "100")
                            .queryParam("offset", String.valueOf(retrieved));
            return resource.header("Authorization", "Bearer " + details.getAccessCode()).accept(MediaType.APPLICATION_JSON_TYPE)
                    .get(SpotifyPlaylistTracksResponse.class);
        } catch (UniformInterfaceException e) {
            // Only until this is solved
            if(!e.getMessage().contains("404 Not Found")) {
                throw e;
            }
            System.out.println("404 ERROR: " +  e.getMessage()  + e.getResponse());
            return emptySpotifyPlaylistTracksResponse();
        }
    }

    private SpotifyPlaylistTracksResponse emptySpotifyPlaylistTracksResponse() {
        SpotifyPlaylistTracksResponse response = new SpotifyPlaylistTracksResponse();
        response.setItems(new ArrayList<>());
        response.setTotal(0);
        return response;
    }

}
