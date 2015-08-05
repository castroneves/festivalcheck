package cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import pojo.Recommendations;
import pojo.Response;
import pojo.TopArtists;
import redis.clients.jedis.*;
import service.JedisConfig;

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

//    public CheckerCache() {
//        JedisShardInfo shardInfo = new JedisShardInfo("glasto.redis.cache.windows.net", 6379);
//        shardInfo.setPassword("Umf1pauU7ZBbB/WJltvE3RUgnz3GcgWVy6UBkem6JgU=");
//        List<JedisShardInfo> shards = Arrays.asList(shardInfo);
//        pool = new ShardedJedisPool(new GenericObjectPoolConfig(), shards);
//    }

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
                        e.printStackTrace();
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
        try(Jedis jedis = newJedis()) {
            String redisKey = "recco_" + key;
            System.out.println(jedis.ttl(redisKey));
            String json = jedis.get(redisKey);
            ObjectMapper mapper = new ObjectMapper();
            if (json != null && json != "") {
                try {
                    Response response = mapper.readValue(json, Response.class);
                    return response;
                } catch (IOException e) {
                    e.printStackTrace();
                    return fallback(redisKey, func, mapper, jedis);
                }
            }
            return fallback(redisKey, func, mapper, jedis);
        }
    }



    private Response fallback(String key, Supplier<Response> func, ObjectMapper mapper, Jedis jedis) {
        Response response = func.get();
        try {
            String inputJson = mapper.writeValueAsString(response);
            jedis.set(key, inputJson);
            jedis.expire(key, 300);
        } catch (IOException e) {
            // do nothing?
        }
        return response;
    }
}
