package module;

import com.google.inject.AbstractModule;
import service.config.GlastoConfiguration;
import service.config.JedisConfig;
import service.config.LastFmConfig;
import spotify.SpotifyConfig;

/**
 * Created by Adam on 15/09/2015.
 */
public class GlastoCheckerModule extends AbstractModule {

    private GlastoConfiguration config;

    public GlastoCheckerModule(GlastoConfiguration config) {
        this.config = config;
    }

    @Override
    protected void configure() {
        bind(LastFmConfig.class).toInstance(config.getLastFm());
        bind(SpotifyConfig.class).toInstance(config.getSpotify());
        bind(JedisConfig.class).toInstance(config.getJedis());
    }
}
