package cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import pojo.Recommendations;
import pojo.Response;
import pojo.TopArtists;
import redis.clients.jedis.*;
import service.JedisConfig;
import spotify.AccessToken;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * Created by Adam on 07/06/2015.
 */
public class CheckerCache {

    private final String password;
    private final String host;
    private final int port;

    public CheckerCache(JedisConfig jedis) {
        password = jedis.getPassword();
        host = jedis.getHost();
        port = jedis.getPort();
    }

    private Jedis newJedis() {
        JedisShardInfo shardInfo = new JedisShardInfo(host, port);
        shardInfo.setPassword(password);
        return new Jedis(shardInfo);
    }

    public TopArtists getOrLookupLastFm(String key, Supplier<Response> func) {
        try {
            try (Jedis jedis = newJedis()) {
                String redisKey = "lastfm_" + key;
                System.out.println(jedis.ttl(redisKey));
                String json = jedis.get(redisKey);
                ObjectMapper mapper = new ObjectMapper();
                if (json != null && json != "") {
                    try {
                        Response response = mapper.readValue(json, Response.class);
                        return response.getTopartists();
                    } catch (IOException e) {
                        return fallback(redisKey, func, mapper, jedis).getTopartists();
                    }
                }
                Response response = fallback(redisKey, func, mapper, jedis);
                return response.getTopartists();
            }
        } catch (Exception e) {
            return func.get().getTopartists();
        }
    }

    public Response getOrLookupRecLastFm(String key, Supplier<Response> func)  {
        try {
            try (Jedis jedis = newJedis()) {
                String redisKey = "recco_" + key;
                System.out.println(jedis.ttl(redisKey));
                String json = jedis.get(redisKey);
                ObjectMapper mapper = new ObjectMapper();
                if (json != null && json != "") {
                    try {
                        Response response = mapper.readValue(json, Response.class);
                        return response;
                    } catch (IOException e) {
                        return fallback(redisKey, func, mapper, jedis);
                    }
                }
                return fallback(redisKey, func, mapper, jedis);
            }
        } catch (Exception e) {
            return func.get();
        }
    }

    public AccessToken getOrFetchSpotifyToken(String code, Supplier<AccessToken> func) {
        try (Jedis jedis = newJedis()) {
            String redisKey = code;
            System.out.println(jedis.ttl(redisKey));
            String json = jedis.get(redisKey);
            ObjectMapper mapper = new ObjectMapper();
            if (json != null && json != "") {
                try {
                    AccessToken response = mapper.readValue(json, AccessToken.class);
                    return response;
                } catch (IOException e) {
                    return fallbackSpotify(redisKey, func, mapper, jedis);
                }
            }
            return fallbackSpotify(redisKey, func, mapper, jedis);
        }
    }


    private AccessToken fallbackSpotify(String key, Supplier<AccessToken> func, ObjectMapper mapper, Jedis jedis) {
        AccessToken response = func.get();
        try {
            String inputJson = mapper.writeValueAsString(response);
            jedis.set(key, inputJson);
            jedis.expire(key, 3600);
        } catch (IOException e) {
            System.out.println("Exception writing value to Redis");
        }
        return response;
    }


    private Response fallback(String key, Supplier<Response> func, ObjectMapper mapper, Jedis jedis) {
        Response response = func.get();
        try {
            String inputJson = mapper.writeValueAsString(response);
            jedis.set(key, inputJson);
            jedis.expire(key, 3000);
        } catch (IOException e) {
            System.out.println("Exception writing value to Redis");
        }
        return response;
    }
}
