package service;

import com.google.inject.Guice;
import com.google.inject.Injector;
import exception.FestivalNotFoundExceptionMapper;
import exception.LastFmExceptionMapper;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import module.GlastoCheckerModule;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import resource.LineupResource;
import resource.ScheduleResource;
import service.config.GlastoConfiguration;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import java.util.EnumSet;

/**
 * Created by Adam on 27/04/2015.
 */
public class GlastoService extends Application<GlastoConfiguration> {
    public static void main(String[] args) throws Exception {
        String[] calArgs = new String[]{"server", "default.yml"};
        new GlastoService().run(calArgs);
    }

    @Override
    public void initialize(Bootstrap<GlastoConfiguration> bootstrap) {
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(
                        bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor()
                )
        );
    }

    @Override
    public void run(GlastoConfiguration configuration, Environment environment) throws Exception {
        Injector injector = Guice.createInjector(new GlastoCheckerModule(configuration));
        ScheduleResource scheduleResource = injector.getInstance(ScheduleResource.class);
        LineupResource lineupResource = injector.getInstance(LineupResource.class);


        // Enable CORS headers
        final FilterRegistration.Dynamic cors =
                environment.servlets().addFilter("CORS", CrossOriginFilter.class);

        // Configure CORS parameters
        cors.setInitParameter("allowedOrigins", "*");
        cors.setInitParameter("allowedHeaders", "X-Requested-With,Content-Type,Accept,Origin");
        cors.setInitParameter("allowedMethods", "OPTIONS,GET,PUT,POST,DELETE,HEAD");

        // Add URL mapping
        cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");

        environment.jersey().register(new LastFmExceptionMapper());
        environment.jersey().register(new FestivalNotFoundExceptionMapper());
        environment.jersey().register(scheduleResource);
        environment.jersey().register(lineupResource);
    }
}
