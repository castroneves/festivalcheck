package service;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Created by Adam on 21/07/2015.
 */
public class GlastoConfiguration extends Configuration {

    @Valid
    @NotNull
    private JedisConfig jedis = new JedisConfig();

    @Valid
    @NotNull
    private LastFmConfig lastFm = new LastFmConfig();

    @JsonProperty("jedis")
    public JedisConfig getJedis() {
        return jedis;
    }

    @JsonProperty("jedis")
    public void setJedis(JedisConfig jedis) {
        this.jedis = jedis;
    }

    @JsonProperty
    public LastFmConfig getLastFm() {
        return lastFm;
    }

    @JsonProperty
    public void setLastFm(LastFmConfig lastFm) {
        this.lastFm = lastFm;
    }
}
