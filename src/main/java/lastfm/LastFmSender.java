package lastfm;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sun.jersey.api.client.AsyncWebResource;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import exception.LastFmException;
import lastfm.domain.Artist;
import lastfm.domain.AuthSession;
import lastfm.domain.Response;
import lastfm.domain.Session;
import org.apache.commons.codec.binary.Hex;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.config.LastFmConfig;

import javax.ws.rs.core.MediaType;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.toList;

/**
 * Created by Adam on 25/04/2015.
 */
@Singleton
public class LastFmSender {
    private static final Logger logger = LoggerFactory.getLogger(LastFmSender.class);
    private static final String baseUrl = "http://ws.audioscrobbler.com/2.0/";

    private final Client client;
    private final String apiKey;
    private final String apiSecret;

    @Inject
    public LastFmSender(LastFmConfig config) {
        apiKey = config.getApiKey();
        apiSecret = config.getSecret();
        ClientConfig cc = new DefaultClientConfig();
        cc.getClasses().add(JacksonJsonProvider.class);
        client = Client.create(cc);
    }

    public Response simpleRequest(String username) {
        logger.info("sending request for user " + username);
        WebResource webResource = getWebResource(username);
        Response response = webResource.accept(MediaType.APPLICATION_JSON_TYPE).
                type(MediaType.APPLICATION_JSON_TYPE).get(Response.class);
        if (response.getError() != null) {
            throw new LastFmException(response.getMessage());
        }
        logger.info("response recieved for user " + username);
        return response;
    }

    public Response recommendedRequest(String token) {
        Session session = getSession(token);
        WebResource resource = getRecommendedWebResource(token, session.getKey());

        Response response = resource.accept(MediaType.APPLICATION_JSON_TYPE).
                type(MediaType.APPLICATION_JSON_TYPE).get(Response.class);
        response.setSession(session);
        enrichReccoRank(response.getRecommendations().getArtist());
        return response;
    }

    public List<Artist> fetchSimilarArtists(List<String> actualArtists, int limit) {
        List<Future<Response>> collect = actualArtists.parallelStream().limit(limit).map(x -> similarArtistRequestAsync(x)).collect(toList());
        return collect.parallelStream().flatMap(x -> {
            try {
                return x.get(1500, TimeUnit.MILLISECONDS).getSimilarartists().getArtist().stream();
            } catch (Exception e) {
                return new ArrayList<Artist>().stream();
            }
        }).collect(toList());
    }

    private Future<Response> similarArtistRequestAsync(String artistName) {
        AsyncWebResource resource =  getWebResourceSimilarAsync(artistName);
        Future<Response> response = resource.accept(MediaType.APPLICATION_JSON_TYPE).
                type(MediaType.APPLICATION_JSON_TYPE).get(Response.class);
        return response;
    }

    private AsyncWebResource getWebResourceSimilarAsync(final String artistName){
        AsyncWebResource resource = client.asyncResource(baseUrl);
        return resource
                .queryParam("method", "artist.getsimilar")
                .queryParam("api_key", apiKey)
                .queryParam("artist", artistName)
                .queryParam("limit", "20")
                .queryParam("format", "json");
    }

    private void enrichReccoRank(List<Artist> artist) {
        for (int i = 0; i < artist.size(); i++) {
            artist.get(i).setReccoRank(i);
        }
    }

    private WebResource getWebResource(final String username){
        WebResource resource = client.resource(baseUrl);
        return resource
                .queryParam("method", "user.gettopartists")
                .queryParam("api_key", apiKey)
                .queryParam("user", username)
                .queryParam("format", "json")
                .queryParam("limit", "1000");
    }

    private WebResource getSessionWebResource(final String token) {
        WebResource resource = client.resource(baseUrl);
        String method = "auth.getSession";
        return resource
                .queryParam("api_key", apiKey)
                .queryParam("method", method)
                .queryParam("format", "json")
                .queryParam("token", token)
                .queryParam("api_sig", md5(generateSig(token, method, null,null)));
    }

    private WebResource getRecommendedWebResource(final String token, final String sk) {
        WebResource resource = client.resource(baseUrl);
        String method = "user.getrecommendedartists";
        String limit = "700";
        return resource
                .queryParam("api_key", "0ba3650498bb88d7328c97b461fc3636")
                .queryParam("method", method)
                .queryParam("format", "json")
                .queryParam("limit", limit)
                .queryParam("sk", sk)
                .queryParam("token", token)
                .queryParam("api_sig", md5(generateSig(token, method, sk, limit)));
    }

    private String md5(String sig) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] digest = md5.digest(sig.getBytes("UTF-8"));
            return Hex.encodeHexString(digest);
        } catch (NoSuchAlgorithmException e) {
            // fatal
        } catch (UnsupportedEncodingException e) {
            //equally fatal
        }
        return null;
    }

    private String generateSig(String token, String method, String sk, String limit) {
        StringBuilder builder = new StringBuilder();
        builder.append("api_key");
        builder.append(apiKey);
        if (limit != null) {
            builder.append("limit");
            builder.append(limit);
        }
        builder.append("method");
        builder.append(method);
        if(sk != null) {
            builder.append("sk");
            builder.append(sk);
        }
        builder.append("token");
        builder.append(token);
        builder.append(apiSecret);
        String s = builder.toString();
        return s;
    }


    private Session getSession(String token) {
        WebResource resource = getSessionWebResource(token);

        AuthSession authSession = resource.accept(MediaType.APPLICATION_JSON_TYPE).
                type(MediaType.APPLICATION_JSON_TYPE).get(AuthSession.class);

        if (authSession.getError() != null) {
            throw new LastFmException("error " + authSession.getError() + ":" + authSession.getMessage());
        }
        return authSession.getSession();
    }
}
