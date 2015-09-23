package spotify;

import com.google.inject.Inject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import service.config.SpotifyConfig;
import spotify.domain.AccessToken;
import spotify.domain.SpotifyTracksResponse;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

/**
 * Created by Adam on 23/08/2015.
 */
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
    public SpotifyTracksResponse getSavedTracks(final String accessCode) {
        WebResource resource = client.resource(tracksUrl);
        return resource.header("Authorization", "Bearer " + accessCode).accept(MediaType.APPLICATION_JSON_TYPE).
                type(MediaType.APPLICATION_FORM_URLENCODED_TYPE).get(SpotifyTracksResponse.class);
    }

    // Get user id https://api.spotify.com/v1/me.

    // Get Users playlists https://api.spotify.com/v1/users/{user_id}/playlists

    // Get Tracks from said playlists https://api.spotify.com/v1/users/{user_id}/playlists/{playlist_id}/tracks
}
