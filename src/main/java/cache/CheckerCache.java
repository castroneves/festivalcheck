package cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import redis.clients.jedis.*;

import java.io.IOException;
import java.util.function.Supplier;

/**
 * Created by Adam on 07/06/2015.
 */
@Singleton
public class CheckerCache {

    @Inject
    private JedisFactory jedisFactory;

    @Inject
    private ObjectMapper mapper;

    public <T> T getOrLookup(String key, Supplier<T> func, CacheKeyPrefix prefix, Class<T> clazz) {
        try (Jedis jedis = jedisFactory.newJedis()) {
            String redisKey = prefix + key;
            System.out.println(jedis.ttl(redisKey));
            String json = jedis.get(redisKey);
            if (json != null && json != "") {
                return mapper.readValue(json, clazz);
            }
            return fallback(redisKey, func, jedis);
        } catch (Exception e) {
            return func.get();
        }
    }

    private <T> T fallback(String key, Supplier<T> func, Jedis jedis) {
        T response = func.get();
        try {
            String inputJson = mapper.writeValueAsString(response);
            jedis.set(key, inputJson);
            jedis.expire(key, 3000);
        } catch (IOException e) {
            System.out.println("Exception writing value to Redis");
        }
        return response;
    }

    public void setJedisFactory(JedisFactory jedisFactory) {
        this.jedisFactory = jedisFactory;
    }

    public void setMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }
}
