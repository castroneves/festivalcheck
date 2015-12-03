package module;

import com.google.inject.AbstractModule;
import service.config.*;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;


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

        List<MappingTuple> mappingConfig = config.getMappingConfig();
        Map<String, String> collect = mappingConfig.stream().collect(toMap(a -> a.getInput(), b -> b.getTo()));

        bind(MappingConfig.class).toInstance(new MappingConfig(config.getMappingConfig()));
    }
}
